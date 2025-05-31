package com.imd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
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

                // Inicializa dataMomento na primeira iteração
                if (dataMomento.isEmpty()) {
                    dataMomento = data;
                }

                // Se mudou de data, processa o lote do dia anterior
                if (!dataMomento.equals(data)) {
                    if (!precisaInterpolar.isEmpty() && !pontosValidos.isEmpty()) {
                        processaEDisparaGravacao(
                                dataMomento,
                                new ArrayList<>(precisaInterpolar),
                                new ArrayList<>(pontosValidos));
                    }
                    // Limpa listas para o novo dia
                    pontosValidos.clear();
                    precisaInterpolar.clear();
                    dataMomento = data;
                }

                // Classifica o ponto atual
                if (temp == -9999.00) {
                    precisaInterpolar.add(ponto);
                } else {
                    pontosValidos.add(ponto);
                }
            }

            // Processa o último dia remanescente
            if (!precisaInterpolar.isEmpty() && !pontosValidos.isEmpty()) {
                processaEDisparaGravacao(
                        dataMomento,
                        new ArrayList<>(precisaInterpolar),
                        new ArrayList<>(pontosValidos));
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo CSV: " + e.getMessage());
        } finally {
            // Aguarda a conclusão de todas as tarefas de interpolação
            executor.shutdown();
        }
    }

    private void processaEDisparaGravacao(
            String data,
            List<Ponto> listaInterp,
            List<Ponto> listaValidos) {

        ExecutorCompletionService<String> completionService = new ExecutorCompletionService<>(executor);

        // 1) Submete todas as tarefas individualmente
        for (Ponto alvo : listaInterp) {
            completionService.submit(() -> {
                String horaAlvo = alvo.getHora();
                List<Ponto> candidatos = listaValidos.stream()
                        .filter(v -> v.getHora().equals(horaAlvo))
                        .collect(Collectors.toList());

                Ponto comVizinhos = idw.atualizarVizinhosMaisProximos(alvo, candidatos, k);
                double valorInterp = idw.interpolarComVizinhos(comVizinhos, p);

                StringBuilder sb = new StringBuilder();
                sb.append(String.format(
                        ">>> Interpolando ponto em %s %s (%.4f, %.4f)%n",
                        comVizinhos.getData(),
                        comVizinhos.getHora(),
                        comVizinhos.getLatitude(),
                        comVizinhos.getLongitude()));
                sb.append(String.format(
                        "Temperatura original: %.2f | Temperatura interpolada: %.2f°C%n",
                        comVizinhos.getTemperatura(),
                        valorInterp));
                sb.append("Pontos usados na interpolação:\n");
                for (Ponto viz : comVizinhos.getPontosProximos()) {
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

        // 2) Coleta os resultados à medida que vão ficando prontos
        List<String> todasLinhas = new ArrayList<>();
        for (int i = 0; i < listaInterp.size(); i++) {
            try {
                Future<String> futuro = completionService.take(); // bloqueia até terminar uma tarefa
                todasLinhas.add(futuro.get());
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Erro ao processar tarefa de interpolação: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }

        // 3) Grava os resultados com o mesmo pool
        executor.submit(() -> {
            try (BufferedWriter bw = new BufferedWriter(
                    new FileWriter("saida_interpolacao.txt", true))) {
                bw.write(String.format("=== RESULTADOS DO DIA %s ===%n", data));
                for (String linhaParaGravar : todasLinhas) {
                    bw.write(linhaParaGravar);
                }
                bw.flush();
            } catch (IOException e) {
                System.err.println("Erro ao gravar dados do dia " + data + ": " + e.getMessage());
            }
        });

        // Se você não for reutilizar mais o executor, pode encerrar aqui:
        // executor.shutdown();
    }
}