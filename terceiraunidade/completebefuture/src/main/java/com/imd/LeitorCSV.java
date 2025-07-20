package com.imd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LeitorCSV {

    private final ExecutorService interpExecutor = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private final ExecutorService ioExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private final Object lockArquivo = new Object();

    private final InversoDistanciaPonderada idw = new InversoDistanciaPonderada();
    private final int p = 2; // expoente de IDW
    private final int k = 5; // número de vizinhos

    public void lerEInterpolar(String caminhoCSV) {
        List<Ponto> precisaInterpolar = new ArrayList<>();
        List<Ponto> pontosValidos = new ArrayList<>();
        String dataMomento = "";

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoCSV));
                BufferedWriter bw = new BufferedWriter(
                        new FileWriter("saida_interpolacao.txt", true))) {
            br.readLine();
            String linha;

            while ((linha = br.readLine()) != null) {
                String[] campos = linha.split(",");

                String data = campos[1];
                String hora = campos[2];
                double temp = Double.parseDouble(campos[3]);
                double lat = Double.parseDouble(campos[4]);
                double lon = Double.parseDouble(campos[5]);

                Ponto ponto = new Ponto(lat, lon, temp, data, hora);

                if (dataMomento.isEmpty()) {
                    dataMomento = data;
                } else if (!dataMomento.equals(data)) {
                    String finalData = dataMomento;
                    List<Ponto> finalInterp = new ArrayList<>(precisaInterpolar);
                    List<Ponto> finalValidos = new ArrayList<>(pontosValidos);

                    ioExecutor.submit(() -> {
                        try {
                            processarLote(finalData, finalInterp, finalValidos, bw);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                    precisaInterpolar.clear();
                    pontosValidos.clear();
                    dataMomento = data;
                }

                if (temp == -9999.00) {
                    precisaInterpolar.add(ponto);
                } else {
                    pontosValidos.add(ponto);
                }
            }

            if (!precisaInterpolar.isEmpty() && !pontosValidos.isEmpty()) {
                String finalData = dataMomento;
                List<Ponto> finalInterp = new ArrayList<>(precisaInterpolar);
                List<Ponto> finalValidos = new ArrayList<>(pontosValidos);
                processarLote(finalData, finalInterp, finalValidos, bw);
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo CSV: " + e.getMessage());
        } finally {
            interpExecutor.shutdown();
            try {
                if (!interpExecutor.awaitTermination(2, TimeUnit.MINUTES)) {
                    interpExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void processarLote(
            String data, List<Ponto> listaInterp, List<Ponto> listaValidos, BufferedWriter bw) throws IOException {

        Map<String, List<Ponto>> mapaPorHora = new HashMap<>();
        for (Ponto pv : listaValidos) {
            mapaPorHora.computeIfAbsent(pv.getHora(), h -> new ArrayList<>()).add(pv);
        }
        List<CompletableFuture<String>> interpolationTasks = new ArrayList<>(listaInterp.size());
        for (Ponto alvo : listaInterp) {

            CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> {
                List<Ponto> candidatos = mapaPorHora.getOrDefault(alvo.getHora(), Collections.emptyList());

                Ponto comVizinhos = idw.atualizarVizinhosMaisProximos(alvo, candidatos, k);
                double valorInterp = idw.interpolarComVizinhos(comVizinhos, p);

                return String.format(
                        "Ponto: %s %s (%.4f, %.4f)%n" +
                                " | Temperatura interpolada: %.2f°C%n",
                        comVizinhos.getData(),
                        comVizinhos.getHora(),
                        comVizinhos.getLatitude(),
                        comVizinhos.getLongitude(),
                        valorInterp);
            }, interpExecutor) // Indica que esta parte será executada pelo interpExecutor
                    .exceptionally(ex -> {
                        // Trata exceções da interpolação aqui, se desejar retornar um valor padrão ou
                        // logar
                        System.err.println("Erro durante a interpolação para o ponto " + alvo.getLatitude() + ","
                                + alvo.getLongitude() + ": " + ex.getMessage());
                        return "Ponto: " + alvo.getData() + " " + alvo.getHora() + " (Erro na interpolação)\n";
                    });
            interpolationTasks.add(future);
        }

        CompletableFuture<Void> allInterpolationsDone = CompletableFuture.allOf(
                interpolationTasks.toArray(new CompletableFuture[0]));

        allInterpolationsDone.thenRunAsync(() -> {
            for (CompletableFuture<String> future : interpolationTasks) {
                try {
                    String resultado = future.join();
                    synchronized (lockArquivo) {
                        bw.write(resultado);
                    }
                } catch (CompletionException e) {
                    System.err.println("Erro ao obter resultado final da interpolação: " + e.getCause().getMessage());
                } catch (IOException e) {
                    System.err.println("Erro de IO ao escrever resultado: " + e.getMessage());

                }
            }
        }, ioExecutor);
    }
}