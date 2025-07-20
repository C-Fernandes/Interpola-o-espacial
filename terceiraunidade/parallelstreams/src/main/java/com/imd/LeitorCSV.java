package com.imd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
                    processarLote(new ArrayList<>(precisaInterpolar), new ArrayList<>(pontosValidos), bw);
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
                processarLote(precisaInterpolar, pontosValidos, bw);
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
            List<Ponto> listaInterp, List<Ponto> listaValidos, BufferedWriter bw) {

        Map<String, List<Ponto>> mapaPorHora = listaValidos.stream()
                .collect(Collectors.groupingBy(Ponto::getHora));
        listaInterp.parallelStream().forEach(alvo -> {
            List<Ponto> candidatos = mapaPorHora.getOrDefault(alvo.getHora(), Collections.emptyList());

            Ponto comVizinhos = idw.atualizarVizinhosMaisProximos(alvo, candidatos, k);
            double valorInterp = idw.interpolarComVizinhos(comVizinhos, p);

            String resultado = String.format(
                    "Ponto: %s %s (%.4f, %.4f)%n" +
                            " | Temperatura interpolada: %.2f°C%n",
                    comVizinhos.getData(),
                    comVizinhos.getHora(),
                    comVizinhos.getLatitude(),
                    comVizinhos.getLongitude(),
                    valorInterp);

            synchronized (lockArquivo) {
                try {
                    bw.write(resultado);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        });
    }

}