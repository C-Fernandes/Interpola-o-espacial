package com.imd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class LeitorCSV {
    private final InversoDistanciaPonderada idw = new InversoDistanciaPonderada();

    // pool de platform threads
    private final int numThreads = Runtime.getRuntime().availableProcessors();
    private final ExecutorService executor = Executors.newFixedThreadPool(numThreads);

    // fila que carrega as Strings a serem escritas
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private static final String POISON_PILL = "##FIM##";

    public void lerEInterpolar(String caminhoCSV) {
        int p = 2; // expoente do IDW
        int k = 5; // número de vizinhos a considerar

        // Listas de pontos por dia
        List<Ponto> precisaInterpolar = new ArrayList<>();
        List<Ponto> pontosValidos = new ArrayList<>();
        String dataMomento = "";

        // 1) Cria e inicia a thread de escrita
        Thread writerThread = new Thread(() -> {
            try (BufferedWriter bw = new BufferedWriter(new FileWriter("saida_interpolacao.txt"))) {
                while (true) {
                    String texto = queue.take(); // aguarda próximo bloco
                    if (POISON_PILL.equals(texto)) { // fim do processamento
                        break;
                    }
                    bw.write(texto);
                }
                bw.flush();
            } catch (IOException | InterruptedException e) {
                System.err.println("Erro na writer thread: " + e.getMessage());
            }
        });
        writerThread.start();

        // 2) Leitura e criação das tasks de interpolação
        try (BufferedReader br = new BufferedReader(new FileReader(caminhoCSV))) {
            br.readLine(); // ignora o cabeçalho

            String linha;
            while ((linha = br.readLine()) != null) {
                String[] campos = linha.split(",");
                String data = campos[1];
                String hora = campos[2];
                double temp = Double.parseDouble(campos[3]);
                double lat = Double.parseDouble(campos[4]);
                double lon = Double.parseDouble(campos[5]);

                Ponto ponto = new Ponto(lat, lon, temp, data, hora);

                // Se é o primeiro registro ou se a data mudou, processa o lote anterior
                if (dataMomento.isEmpty() || !dataMomento.equals(data)) {
                    if (!precisaInterpolar.isEmpty()) {
                        // Processa em paralelo, mas envia resultados para a fila
                        processarLoteDoDia(precisaInterpolar, pontosValidos, p, k);
                    }
                    pontosValidos.clear();
                    precisaInterpolar.clear();
                    dataMomento = data;
                }

                // Armazena o ponto na lista correta
                if (temp == -9999.00) {
                    precisaInterpolar.add(ponto);
                } else {
                    pontosValidos.add(ponto);
                }
            }

            // Após o fim do while, pode existir um último dia pendente
            if (!precisaInterpolar.isEmpty()) {
                processarLoteDoDia(precisaInterpolar, pontosValidos, p, k);
            }

            // 3) Coloca o poison pill para sinalizar que não há mais blocos de texto
            queue.put(POISON_PILL);

        } catch (IOException | InterruptedException e) {
            System.err.println("Erro ao ler CSV ou colocar poison pill: " + e.getMessage());
        } finally {
            // 4) Espera a writer thread encerrar e depois encerra o pool
            try {
                writerThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.err.println("InterruptedException ao aguardar writer thread: " + e.getMessage());
            }
            executor.shutdown();
        }
    }

    private void processarLoteDoDia(
            List<Ponto> precisaInterpolar,
            List<Ponto> pontosValidos,
            int p,
            int k) {
        try {
            // 1) Monta a lista de Callables<String>
            List<Callable<String>> tarefas = new ArrayList<>();
            for (Ponto alvo : precisaInterpolar) {
                tarefas.add(() -> {
                    String horaAlvo = alvo.getHora();
                    List<Ponto> candidatos = pontosValidos.stream()
                            .filter(v -> v.getHora().equals(horaAlvo))
                            .collect(Collectors.toList());

                    Ponto alvoComVizinhos = idw.atualizarVizinhosMaisProximos(alvo, candidatos, k);
                    double valorInterpolado = idw.interpolarComVizinhos(alvoComVizinhos, p);

                    StringBuilder sb = new StringBuilder();
                    sb.append(String.format(
                            ">>> Interpolando ponto em %s %s (%.4f, %.4f)%n",
                            alvoComVizinhos.getData(),
                            alvoComVizinhos.getHora(),
                            alvoComVizinhos.getLatitude(),
                            alvoComVizinhos.getLongitude()));
                    sb.append(String.format(
                            "Temperatura original: %.2f | Temperatura interpolada: %.2f°C%n",
                            alvoComVizinhos.getTemperatura(),
                            valorInterpolado));
                    sb.append("Pontos usados na interpolação:\n");
                    for (Ponto viz : alvoComVizinhos.getPontosProximos()) {
                        sb.append(String.format(
                                "- (%.4f, %.4f) | Temp: %.2f | Data: %s Hora: %s%n",
                                viz.getLatitude(),
                                viz.getLongitude(),
                                viz.getTemperatura(),
                                viz.getData(),
                                viz.getHora()));
                    }
                    sb.append("\n");
                    return sb.toString();
                });
            }

            // 2) Executa todas as tasks em paralelo e aguarda término
            List<Future<String>> futures = executor.invokeAll(tarefas);

            // 3) Para cada futuro, obtém a String de saída e coloca na fila
            for (Future<String> fut : futures) {
                try {
                    String texto = fut.get();
                    queue.put(texto);
                } catch (ExecutionException | InterruptedException ex) {
                    System.err.println("Erro em tarefa de interpolação: " + ex.getMessage());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Thread interrompida durante processarLoteDoDia: " + e.getMessage());
        }
    }
}
