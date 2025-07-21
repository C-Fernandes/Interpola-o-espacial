package com.imd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import com.imd.InterpolationTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ForkJoinPool;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.io.UncheckedIOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LeitorCSV {

     private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    private final Object lockArquivo = new Object();
    private final InversoDistanciaPonderada idw = new InversoDistanciaPonderada();
    private final int p = 2; // expoente de IDW
    private final int k = 5; // número de vizinhos

    public void lerEInterpolar(String caminhoCSV) {
        List<Ponto> precisaInterpolar = new ArrayList<>();
        List<Ponto> pontosValidos = new ArrayList<>();
        String dataMomento = "";

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
                    processarLote(dataMomento, precisaInterpolar, pontosValidos, bw);
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
                processarLote(dataMomento, precisaInterpolar, pontosValidos, bw);
            }

        } catch (IOException e) {
            System.err.println("Erro ao processar o arquivo: " + e.getMessage());
        } finally {
            shutdownExecutor(forkJoinPool, "forkJoinPool");
        }
    }

    public void processarLote(String data, List<Ponto> listaInterp, List<Ponto> listaValidos, BufferedWriter bw) {

        Map<String, List<Ponto>> mapaPorHora = new HashMap<>();
        for (Ponto pv : listaValidos) {
            mapaPorHora.computeIfAbsent(pv.getHora(), h -> new ArrayList<>()).add(pv);
        }
        InterpolationTask mainTask = new InterpolationTask(new ArrayList<>(listaInterp), mapaPorHora, data, idw, p, k);
        List<String> resultados = forkJoinPool.invoke(mainTask);
        try {
            synchronized (lockArquivo) {
                for (String resultado : resultados) {
                    bw.write(resultado);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void shutdownExecutor(ExecutorService executor, String name) {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.MINUTES)) {
                System.err.println(name + " não terminou a tempo, forçando desligamento.");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.err.println("Interrupção ao aguardar o término de " + name);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}