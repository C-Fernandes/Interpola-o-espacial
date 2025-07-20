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

public class LeitorCSV {
    private final ForkJoinPool forkJoinPool = ForkJoinPool.commonPool();
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
                    processarLote(new ArrayList<>(pontosValidos), new ArrayList<>(precisaInterpolar), bw);

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
                processarLote(new ArrayList<>(pontosValidos), new ArrayList<>(precisaInterpolar), bw);
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

    public void processarLote(List<Ponto> listaValidos, List<Ponto> listaInterp, BufferedWriter bw) throws IOException {

        Map<String, List<Ponto>> mapaPorHora = new HashMap<>();
        for (Ponto pv : listaValidos) {
            mapaPorHora.computeIfAbsent(pv.getHora(), h -> new ArrayList<>()).add(pv);
        }
        List<Ponto> allValidPointsForBatch = new ArrayList<>(listaValidos);
        InterpolationTask mainTask = new InterpolationTask(listaInterp, allValidPointsForBatch, k, p, idw);

        List<String> resultados = forkJoinPool.invoke(mainTask);

        synchronized (lockArquivo) {
            for (String resultado : resultados) {
                bw.write(resultado);
            }
        }
    }

}