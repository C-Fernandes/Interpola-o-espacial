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

    // 1) Executor para escrita em arquivo: cada tarefa em uma virtual thread
    // (I/O-bound)
    private final ExecutorService ioExecutor = Executors.newVirtualThreadPerTaskExecutor();

    // 2) Pool Fork/Join para interpolação (CPU-bound)
    private final ForkJoinPool forkJoinPool = new ForkJoinPool();

    private final InversoDistanciaPonderada idw = new InversoDistanciaPonderada();
    private final int p = 2; // expoente
    private final int k = 5; // número de vizinhos

    /**
     * Classe interna que representa a tarefa de interpolação no modelo Fork/Join.
     * Ela retorna uma lista de strings formatadas com os resultados.
     */
    private class InterpolacaoTask extends RecursiveTask<List<String>> {
        private static final int THRESHOLD = 10;
        private final List<Ponto> pontosParaInterpolar;
        private final List<Ponto> pontosValidos;

        public InterpolacaoTask(List<Ponto> pontosParaInterpolar, List<Ponto> pontosValidos) {
            this.pontosParaInterpolar = pontosParaInterpolar;
            this.pontosValidos = pontosValidos;
        }

        @Override
        protected List<String> compute() {
            if (pontosParaInterpolar.size() <= THRESHOLD) { // Caso base
                List<String> resultados = new ArrayList<>();
                for (Ponto alvo : pontosParaInterpolar) {
                    try {
                        List<Ponto> candidatos = pontosValidos.stream()
                                .filter(v -> v.getHora().equals(alvo.getHora()))
                                .collect(Collectors.toList());
                        if (candidatos.isEmpty()) {
                            resultados.add(String.format(
                                    "Ponto: %s %s (%.4f, %.4f)%n | Não há pontos de referência no mesmo horário para interpolar.%n",
                                    alvo.getData(), alvo.getHora(), alvo.getLatitude(), alvo.getLongitude()));
                            continue;
                        }

                        Ponto comVizinhos = idw.atualizarVizinhosMaisProximos(alvo, candidatos, k);
                        double valorInterp = idw.interpolarComVizinhos(comVizinhos, p);

                        String resultadoFormatado = String.format(
                                "Ponto: %s %s (%.4f, %.4f)%n | Temperatura interpolada: %.2f°C%n",
                                comVizinhos.getData(), comVizinhos.getHora(),
                                comVizinhos.getLatitude(), comVizinhos.getLongitude(), valorInterp);
                        resultados.add(resultadoFormatado);
                    } catch (Exception e) {
                        System.err
                                .println("Erro ao processar interpolação para o ponto " + alvo + ": " + e.getMessage());
                        resultados.add(">> Erro na interpolação de um ponto.\n");
                    }
                }
                return resultados;
            } else { // Caso recursivo
                int meio = pontosParaInterpolar.size() / 2;
                InterpolacaoTask tarefaEsquerda = new InterpolacaoTask(pontosParaInterpolar.subList(0, meio),
                        pontosValidos);
                InterpolacaoTask tarefaDireita = new InterpolacaoTask(
                        pontosParaInterpolar.subList(meio, pontosParaInterpolar.size()), pontosValidos);

                tarefaDireita.fork();
                List<String> resultadoEsquerda = tarefaEsquerda.compute();
                List<String> resultadoDireita = tarefaDireita.join();

                resultadoEsquerda.addAll(resultadoDireita);
                return resultadoEsquerda;
            }
        }
    }

    public void lerEInterpolar(String caminhoCSV) {
        List<Ponto> precisaInterpolar = new ArrayList<>();
        List<Ponto> pontosValidos = new ArrayList<>();
        String dataMomento = "";

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoCSV))) {
            br.readLine(); // Pula o cabeçalho

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
                    // Processa o lote do dia anterior antes de limpar as listas
                    processarLote(dataMomento, new ArrayList<>(precisaInterpolar), new ArrayList<>(pontosValidos));
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

            // Processa o último lote de dados que sobrou no final do arquivo
            if (!precisaInterpolar.isEmpty() || !pontosValidos.isEmpty()) {
                processarLote(dataMomento, new ArrayList<>(precisaInterpolar), new ArrayList<>(pontosValidos));
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo CSV: " + e.getMessage());
        } finally {
            // Desligamento gracioso de todos os pools de threads
            shutdownPools();
        }
    }

    public void processarLote(String data, List<Ponto> listaInterp, List<Ponto> listaValidos) {
        if (listaInterp.isEmpty()) {
            System.out.println("Nenhum ponto para interpolar para o dia " + data);
            return;
        }
        System.out.println("\nProcessando lote para o dia: " + data);

        InterpolacaoTask tarefaPrincipal = new InterpolacaoTask(listaInterp, listaValidos);
        List<String> todasLinhas = forkJoinPool.invoke(tarefaPrincipal);

        ioExecutor.execute(() -> {
            System.out.println("Gravando resultados do dia " + data + " no arquivo...");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter("saida_interpolacao.txt", true))) {
                bw.write("--- Resultados para " + data + " ---\n");
                for (String linhaParaGravar : todasLinhas) {
                    bw.write(linhaParaGravar);
                }
                bw.flush();
                System.out.println("Gravação para o dia " + data + " concluída.");
            } catch (IOException e) {
                System.err.println("Erro ao gravar dados do dia " + data + ": " + e.getMessage());
            }
        });
    }

    private void shutdownPools() {
        System.out.println("\nIniciando desligamento dos pools de threads...");

        // Desligando o ForkJoinPool
        System.out.println("Desligando o pool Fork/Join...");
        forkJoinPool.shutdown();
        try {
            if (!forkJoinPool.awaitTermination(60, TimeUnit.SECONDS)) {
                System.err.println("Pool Fork/Join não terminou no tempo. Forçando desligamento.");
                forkJoinPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.err.println("Espera pelo término do pool Fork/Join interrompida.");
            forkJoinPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Pool Fork/Join desligado.");

        // Desligando o I/O Executor
        System.out.println("Desligando o pool de I/O...");
        ioExecutor.shutdown();
        try {
            if (!ioExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                System.err.println("Pool de I/O não terminou no tempo. Forçando desligamento.");
                ioExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            System.err.println("Espera pelo término do pool de I/O interrompida.");
            ioExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Pool de I/O desligado.");
    }
}