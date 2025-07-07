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

/**
 * Esta classe é responsável por ler um arquivo CSV de dados meteorológicos,
 * identificar pontos que necessitam de interpolação e processá-los em lotes
 * diários usando Parallel Streams para a computação e um executor separado
 * para as operações de escrita em arquivo.
 */
class LeitorCSV {

    // Executor para escrita em arquivo (I/O-bound). Usar Virtual Threads é ideal
    // para não bloquear threads da plataforma com tarefas de I/O.
    private final ExecutorService ioExecutor = Executors.newVirtualThreadPerTaskExecutor();

    // Não há necessidade de um ForkJoinPool customizado. Parallel Streams usam o
    // pool comum.

    private final InversoDistanciaPonderada idw = new InversoDistanciaPonderada();
    private final int p = 2; // Expoente para o cálculo de peso do IDW
    private final int k = 5; // Número de vizinhos a serem considerados na interpolação

    /**
     * Método principal que orquestra a leitura do arquivo e o disparo do
     * processamento.
     * 
     * @param caminhoCSV O caminho para o arquivo de dados.
     */
    public void lerEInterpolar(String caminhoCSV) {
        List<Ponto> precisaInterpolar = new ArrayList<>();
        List<Ponto> pontosValidos = new ArrayList<>();
        String dataMomento = "";

        try (BufferedReader br = new BufferedReader(new FileReader(caminhoCSV))) {
            br.readLine(); // Pula a linha do cabeçalho do CSV

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

            if (!precisaInterpolar.isEmpty() || !pontosValidos.isEmpty()) {
                processarLote(dataMomento, new ArrayList<>(precisaInterpolar), new ArrayList<>(pontosValidos));
            }

        } catch (IOException e) {
            System.err.println("Erro ao ler arquivo CSV: " + e.getMessage());
        } finally {
            shutdownIoExecutor();
        }
    }

    public void processarLote(String data, List<Ponto> listaInterp, List<Ponto> listaValidos) {
        if (listaInterp.isEmpty()) {
            System.out.println("Nenhum ponto para interpolar para o dia " + data);
            return;
        }
        System.out.println("\nProcessando lote para o dia: " + data + " usando Parallel Stream...");

        // 1. Converte a lista para um stream paralelo, permitindo que a operação 'map'
        // seja executada em múltiplas threads do pool comum ForkJoin.
        // 2. A operação 'map' transforma cada ponto-alvo em uma string com o resultado
        // da interpolação.
        // 3. 'collect' agrega todos os resultados em uma nova lista de strings.
        List<String> todasLinhas = listaInterp.parallelStream().map(alvo -> {
            try {
                // Filtra os pontos de referência para usar apenas os do mesmo horário
                List<Ponto> candidatos = listaValidos.stream()
                        .filter(v -> v.getHora().equals(alvo.getHora()))
                        .collect(Collectors.toList());

                if (candidatos.isEmpty()) {
                    return String.format(
                            "Ponto: %s %s (%.4f, %.4f)%n | Não há pontos de referência no mesmo horário para interpolar.%n",
                            alvo.getData(), alvo.getHora(), alvo.getLatitude(), alvo.getLongitude());
                }

                // Realiza a interpolação
                Ponto comVizinhos = idw.atualizarVizinhosMaisProximos(alvo, candidatos, k);
                double valorInterp = idw.interpolarComVizinhos(comVizinhos, p);

                // Formata a string de saída
                return String.format(
                        "Ponto: %s %s (%.4f, %.4f)%n | Temperatura interpolada: %.2f°C%n",
                        comVizinhos.getData(), comVizinhos.getHora(),
                        comVizinhos.getLatitude(), comVizinhos.getLongitude(), valorInterp);
            } catch (Exception e) {
                // Tratamento de erro para uma falha em um único ponto
                System.err.println("Erro na interpolação do ponto " + alvo + ": " + e.getMessage());
                return ">> Erro na interpolação deste ponto.\n";
            }
        }).collect(Collectors.toList());

        // Submete a tarefa de escrita (I/O) ao executor dedicado, para não
        // bloquear as threads de computação do pool ForkJoin.
        ioExecutor.execute(() -> {
            System.out.println("Gravando resultados do dia " + data + " no arquivo...");
            try (BufferedWriter bw = new BufferedWriter(new FileWriter("saida_interpolacao.txt", true))) {
                bw.write("--- Resultados para " + data + " (via Parallel Stream) ---\n");
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

    /**
     * Realiza o desligamento seguro do executor de I/O.
     */
    private void shutdownIoExecutor() {
        System.out.println("\nIniciando desligamento do pool de I/O...");
        ioExecutor.shutdown();
        try {
            // Aguarda um tempo para que as tarefas pendentes terminem
            if (!ioExecutor.awaitTermination(60, TimeUnit.SECONDS)) {
                System.err.println("Pool de I/O não terminou no tempo. Forçando desligamento.");
                ioExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            // Se a thread atual for interrompida enquanto espera, força o desligamento
            System.err.println("A espera pelo término do pool de I/O foi interrompida.");
            ioExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Pool de I/O desligado.");
    }
}