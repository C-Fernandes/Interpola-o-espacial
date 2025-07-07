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

    private final ExecutorService executor = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private final InversoDistanciaPonderada idw = new InversoDistanciaPonderada();
    private final int p = 2; // expoente
    private final int k = 5; // número de vizinhos

    public void lerEInterpolar(String caminhoCSV) {
        List<Ponto> precisaInterpolar = new ArrayList<>();
        List<Ponto> pontosValidos = new ArrayList<>();
        String dataMomento = "";

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoCSV))) {

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
                    processarLote(dataMomento, precisaInterpolar, pontosValidos);
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
                processarLote(dataMomento, precisaInterpolar, pontosValidos);
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo CSV: " + e.getMessage());
        } finally {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(60, TimeUnit.MINUTES)) {
                    System.err.println("Pool de threads não terminou no tempo esperado. Forçando o desligamento.");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                System.err.println("A espera pelo término das threads foi interrompida.");
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public void processarLote(
            String data,
            List<Ponto> listaInterp,
            List<Ponto> listaValidos) {

        // 1) Submete cada interpolação em platform thread (pool fixo)
        List<Future<String>> futures = new ArrayList<>(listaInterp.size());
        for (Ponto alvo : listaInterp) {
            Callable<String> tarefaInterp = () -> {
                List<Ponto> candidatos = listaValidos.stream()
                        .filter(v -> v.getHora().equals(alvo.getHora()))
                        .collect(Collectors.toList());

                // Atualiza vizinhos e faz interpolação
                Ponto comVizinhos = idw.atualizarVizinhosMaisProximos(alvo, candidatos, k);
                double valorInterp = idw.interpolarComVizinhos(comVizinhos, p);

                // Monta uma linha de saída
                return String.format(
                        "Ponto: %s %s (%.4f, %.4f)%n | Temperatura interpolada: %.2f°C%n",
                        comVizinhos.getData(),
                        comVizinhos.getHora(),
                        comVizinhos.getLatitude(),
                        comVizinhos.getLongitude(),
                        valorInterp);
            };

            futures.add(executor.submit(tarefaInterp));
        }

        // 2) Coleta todas as strings, em ordem de submissão
        List<String> todasLinhas = new ArrayList<>(listaInterp.size());
        for (Future<String> f : futures) {
            try {
                todasLinhas.add(f.get());
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Erro ao processar interpolação: " + e.getMessage());
                Thread.currentThread().interrupt();
                todasLinhas.add(">> Erro na interpolação deste ponto.\n");
            }
        }

        executor.submit(() -> {
            try (BufferedWriter bw = new BufferedWriter(
                    new FileWriter("saida_interpolacao.txt", true))) {

                for (String linhaParaGravar : todasLinhas) {
                    bw.write(linhaParaGravar);
                }
                bw.flush();
            } catch (IOException e) {
                System.err.println("Erro ao gravar dados do dia " + data + ": " + e.getMessage());
            }
        });
    }
}