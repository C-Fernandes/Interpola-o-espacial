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

        // Lista para guardar os Futures de cada LOTE
        List<Future<?>> batchFutures = new ArrayList<>();

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
                    List<Ponto> finalInterp = new ArrayList<>(precisaInterpolar);
                    List<Ponto> finalValidos = new ArrayList<>(pontosValidos);
                    String dataDoLote = dataMomento;
                    Future<?> future = ioExecutor.submit(() -> {
                        try {
                            processarLote(dataDoLote, finalInterp, finalValidos, bw);
                        } catch (IOException | InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    });
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

            // Trata o último lote da mesma forma assíncrona
            if (!precisaInterpolar.isEmpty()) {
                String dataDoLote = dataMomento;
                Future<?> future = ioExecutor.submit(() -> {
                    try {
                        processarLote(dataDoLote, new ArrayList<>(precisaInterpolar), new ArrayList<>(pontosValidos),
                                bw);
                    } catch (IOException | InterruptedException | ExecutionException e) {
                        e.printStackTrace();
                    }
                });
                batchFutures.add(future);
            }

            for (Future<?> future : batchFutures) {
                future.get(); // Espera a conclusão de cada 'processarLote'
            }

        } catch (Exception e) {
            System.err.println("Erro no processamento principal: " + e.getMessage());
            Thread.currentThread().interrupt();
        } finally {
            shutdownExecutor(ioExecutor, "ioExecutor");
            shutdownExecutor(interpExecutor, "interpExecutor");
        }
    }

    public void processarLote(
            String data, List<Ponto> listaInterp, List<Ponto> listaValidos, BufferedWriter bw)
            throws IOException, InterruptedException, ExecutionException { // Adicione as exceções

        Map<String, List<Ponto>> mapaPorHora = new HashMap<>();
        for (Ponto pv : listaValidos) {
            mapaPorHora.computeIfAbsent(pv.getHora(), h -> new ArrayList<>(16)).add(pv);
        }

        List<Future<String>> pointFutures = new ArrayList<>();
        for (Ponto alvo : listaInterp) {
            Callable<String> tarefaInterp = () -> {
                List<Ponto> candidatos = mapaPorHora.getOrDefault(alvo.getHora(), Collections.emptyList());
                Ponto comVizinhos = idw.atualizarVizinhosMaisProximos(alvo, candidatos, k);
                double valorInterp = idw.interpolarComVizinhos(comVizinhos, p);
                return String.format(
                        "Ponto: %s %s (%.4f, %.4f)%n | Temperatura interpolada: %.2f°C%n",
                        data, comVizinhos.getHora(), comVizinhos.getLatitude(), comVizinhos.getLongitude(),
                        valorInterp);
            };
            pointFutures.add(interpExecutor.submit(tarefaInterp));
        }

        for (Future<String> future : pointFutures) {
            String resultado = future.get(); // Espera cada ponto ser calculado e obtém a string
            synchronized (lockArquivo) {
                bw.write(resultado);
            }
        }
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