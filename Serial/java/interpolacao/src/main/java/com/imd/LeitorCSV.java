package com.imd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LeitorCSV {
    InversoDistanciaPonderada idw = new InversoDistanciaPonderada();

    public void lerEInterpolar(String caminhoCSV) {
        int p = 2; // expoente do IDW
        int k = 5; // número de vizinhos a considerar

        List<Ponto> precisaInterpolar = new ArrayList<>();
        List<Ponto> pontosValidos = new ArrayList<>();
        String dataMomento = "";

        try (
                BufferedReader br = new BufferedReader(new FileReader(caminhoCSV));
                BufferedWriter bw = new BufferedWriter(new FileWriter("saida_interpolacao.txt"))) {
            br.readLine(); // Ignora cabeçalho

            String linha;
            while ((linha = br.readLine()) != null) {
                String[] campos = linha.split(",");

                String data = campos[1];
                String hora = campos[2];
                double temp = Double.parseDouble(campos[3]);
                double lat = Double.parseDouble(campos[4]);
                double lon = Double.parseDouble(campos[5]);

                Ponto ponto = new Ponto(lat, lon, temp, data, hora);

                // Se for a primeira iteração (dataMomento == "") ou se a data mudou
                if (dataMomento.isEmpty() || !dataMomento.equals(data)) {
                    // Processa todos os pontos pendentes do dia anterior
                    if (!precisaInterpolar.isEmpty()) {
                        for (Ponto alvo : precisaInterpolar) {
                            // 1) filtra apenas pontos válidos com a mesma hora do "alvo"
                            final String horaAlvo = alvo.getHora();
                            List<Ponto> candidatos = pontosValidos.stream()
                                    .filter(v -> v.getHora().equals(horaAlvo))
                                    .collect(Collectors.toList());

                            // 2) obtém os k vizinhos mais próximos de uma só vez
                            alvo = idw.atualizarVizinhosMaisProximos(alvo, candidatos, k);

                            // 3) faz a interpolação e IMPRIME imediatamente em seguida
                            double valorInterpolado = idw.interpolarComVizinhos(alvo, p);

                            bw.write(String.format(
                                    ">>> Interpolando ponto em %s %s (%.4f, %.4f)%n",
                                    alvo.getData(),
                                    alvo.getHora(),
                                    alvo.getLatitude(),
                                    alvo.getLongitude()));
                            bw.write(String.format(
                                    "Temperatura original: %.2f | Temperatura interpolada: %.2f°C%n",
                                    alvo.getTemperatura(),
                                    valorInterpolado));
                            bw.write("Pontos usados na interpolação:\n");
                            for (Ponto viz : alvo.getPontosProximos()) {
                                bw.write(String.format(
                                        "- (%.4f, %.4f) | Temp: %.2f | Data: %s Hora: %s%n",
                                        viz.getLatitude(),
                                        viz.getLongitude(),
                                        viz.getTemperatura(),
                                        viz.getData(),
                                        viz.getHora()));
                            }
                            bw.write("\n");
                        }
                    }

                    // Depois de imprimir o dia anterior, limpa as listas e atualiza dataMomento
                    pontosValidos.clear();
                    precisaInterpolar.clear();
                    dataMomento = data;
                }

                // A seguir, armazena o ponto atual no lugar correto
                if (temp == -9999.00) {
                    precisaInterpolar.add(ponto);
                } else {
                    pontosValidos.add(ponto);
                }
            } // fim do while

            // NÃO há mais bloco de impressão aqui: todas as interpolações
            // já foram escritas assim que cada ponto foi calculado.

            bw.close();
            br.close();
        } catch (IOException e) {
            System.err.println("Erro ao ler ou escrever arquivo: " + e.getMessage());
        }
    }
}
