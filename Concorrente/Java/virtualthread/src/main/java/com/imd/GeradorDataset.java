package com.imd;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;

public class GeradorDataset {
    public static void main(String[] args) throws IOException {
        int dias = 36500; // 10 anos
        int leiturasPorDia = 24; // 24 leituras por dia, 1 por hora
        int pontosGeograficos = 24; // 24 pontos geográficos distintos
        double probFaltante = 0.10; // 10% dos valores como -9999.0
        String nomeArquivo = "Serial/java/interpolacao/src/main/resources/dataset.csv";

        Random random = new Random();

        // Definindo 12 pontos fixos distintos (latitude e longitude)
        double[][] localizacoes = new double[pontosGeograficos][2];
        for (int i = 0; i < pontosGeograficos; i++) {
            double lat = -10.0 + i * 0.5; // espaçamento geográfico
            double lon = -40.0 + i * 0.5;
            localizacoes[i][0] = lat;
            localizacoes[i][1] = lon;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(nomeArquivo))) {
            writer.write("indice,data,hora,temperatura,latitude,longitude\n");
            int indice = 1;

            for (int dia = 1; dia <= dias; dia++) {
                String data = String.format("2023-%02d-%02d", (dia - 1) / 30 + 1, (dia - 1) % 30 + 1);

                for (int horaIndex = 0; horaIndex < leiturasPorDia; horaIndex++) {
                    String hora = String.format("%02d:00", horaIndex * (24 / leiturasPorDia));

                    for (int ponto = 0; ponto < pontosGeograficos; ponto++) {
                        double temperatura = random.nextDouble() * 35;
                        if (random.nextDouble() < probFaltante) {
                            temperatura = -9999;
                        }

                        double lat = localizacoes[ponto][0];
                        double lon = localizacoes[ponto][1];

                        writer.write(String.format(Locale.US, "%d,%s,%s,%.2f,%.6f,%.6f\n",
                                indice++, data, hora, temperatura, lat, lon));
                    }
                }
            }
        }

        System.out.println("Arquivo gerado com sucesso: " + nomeArquivo);
    }
}
