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

    // 1) Executor para interpolação: pool fixo de platform threads
    private final ExecutorService interpExecutor = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    // 2) Executor para escrita em arquivo: cada tarefa em uma virtual thread
    private final ExecutorService ioExecutor = Executors.newVirtualThreadPerTaskExecutor();

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
            System.out.println("Iniciando desligamento do pool de interpolação...");
            interpExecutor.shutdown();
            try {
                if (!interpExecutor.awaitTermination(60, TimeUnit.MINUTES)) {
                    System.err.println("Pool de interpolação não terminou no tempo esperado. Forçando o desligamento.");
                    interpExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                System.err.println("A espera pelo término do pool de interpolação foi interrompida.");
                interpExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("Pool de interpolação desligado.");

            System.out.println("Iniciando desligamento do pool de I/O...");
            ioExecutor.shutdown();
            try {
                if (!ioExecutor.awaitTermination(60, TimeUnit.MINUTES)) {
                    System.err.println("Pool de I/O não terminou no tempo esperado. Forçando o desligamento.");
                    ioExecutor.shutdownNow(); // Tenta cancelar as tarefas em execução.
                }
            } catch (InterruptedException e) {
                System.err.println("A espera pelo término do pool de I/O foi interrompida.");
                ioExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            System.out.println("Pool de I/O desligado.");
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

            futures.add(interpExecutor.submit(tarefaInterp));
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

        // 3) Escreve em arquivo usando virtual thread
        ioExecutor.submit(() -> {
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