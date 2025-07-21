package com.imd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UncheckedIOException;
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

        // Lista para guardar os CompletableFutures de cada LOTE
        List<CompletableFuture<Void>> batchFutures = new ArrayList<>();

        try (BufferedWriter bw = new BufferedWriter(new FileWriter("saida_interpolacao.txt", true));
                BufferedReader br = new BufferedReader(new FileReader(caminhoCSV))) {

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
                    String dataDoLote = dataMomento;
                    List<Ponto> finalInterp = new ArrayList<>(precisaInterpolar);
                    List<Ponto> finalValidos = new ArrayList<>(pontosValidos);

                    CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                        processarLote(dataDoLote, finalInterp, finalValidos, bw);
                    }, ioExecutor);
                    batchFutures.add(future);

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

            if (!precisaInterpolar.isEmpty()) {
                String dataDoLote = dataMomento;
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    processarLote(dataDoLote, new ArrayList<>(precisaInterpolar), new ArrayList<>(pontosValidos), bw);
                }, ioExecutor);
                batchFutures.add(future);
            }

            CompletableFuture.allOf(batchFutures.toArray(new CompletableFuture[0])).join();

        } catch (Exception e) {
            System.err.println("Erro no processamento principal: " + e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            // Desliga ambos os executores de forma segura
            shutdownExecutor(ioExecutor, "ioExecutor");
            shutdownExecutor(interpExecutor, "interpExecutor");
        }
    }

    public void processarLote(String data, List<Ponto> listaInterp, List<Ponto> listaValidos, BufferedWriter bw) {
        Map<String, List<Ponto>> mapaPorHora = new HashMap<>();
        for (Ponto pv : listaValidos) {
            mapaPorHora.computeIfAbsent(pv.getHora(), h -> new ArrayList<>()).add(pv);
        }

        List<CompletableFuture<String>> pointFutures = listaInterp.stream()
                .map(alvo -> CompletableFuture.supplyAsync(() -> {
                    List<Ponto> candidatos = mapaPorHora.getOrDefault(alvo.getHora(), Collections.emptyList());
                    Ponto comVizinhos = idw.atualizarVizinhosMaisProximos(alvo, candidatos, k);
                    double valorInterp = idw.interpolarComVizinhos(comVizinhos, p);
                    return String.format(
                            "Ponto: %s %s (%.4f, %.4f)%n | Temperatura interpolada: %.2f°C%n",
                            data, comVizinhos.getHora(), comVizinhos.getLatitude(), comVizinhos.getLongitude(),
                            valorInterp);
                }, interpExecutor))
                .toList(); // .toList() é do Java 16+, use .collect(Collectors.toList()) para versões
                           // anteriores

        // PONTO DE SINCRONIZAÇÃO INTERNO: Espera todos os pontos do lote terminarem
        CompletableFuture.allOf(pointFutures.toArray(new CompletableFuture[0])).join();

        // Após a conclusão de todos, escreve os resultados (que já estão prontos)
        pointFutures.forEach(future -> {
            try {
                String resultado = future.getNow(null); // .getNow() pois já sabemos que completou
                synchronized (lockArquivo) {
                    bw.write(resultado);
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private void shutdownExecutor(ExecutorService executor, String name) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

}