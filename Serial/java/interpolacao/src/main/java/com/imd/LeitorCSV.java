package com.imd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;

public class LeitorCSV {

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
        }
    }

    public void processarLote(
            String data,
            List<Ponto> listaInterp,
            List<Ponto> listaValidos) {

        List<String> todasLinhas = new ArrayList<>();

        for (Ponto alvo : listaInterp) {
            List<Ponto> candidatos = new ArrayList<>();
            for (Ponto v : listaValidos) {
                if (v.getHora().equals(alvo.getHora())) {
                    candidatos.add(v);
                }
            }

            Ponto comVizinhos = idw.atualizarVizinhosMaisProximos(alvo, candidatos, k);
            double valorInterp = idw.interpolarComVizinhos(comVizinhos, p);
            StringBuilder sb = new StringBuilder(512);
            Formatter fmt = new Formatter(sb, Locale.ROOT);

            fmt.format(
                    ">>> Interpolando ponto em %s %s (%.4f, %.4f)%n",
                    comVizinhos.getData(),
                    comVizinhos.getHora(),
                    comVizinhos.getLatitude(),
                    comVizinhos.getLongitude());

            fmt.format(
                    "Temperatura original: %.2f | Temperatura interpolada: %.2f°C%n",
                    comVizinhos.getTemperatura(),
                    valorInterp);

            fmt.format("%n");

            String resultado = sb.toString();
            fmt.close();

            todasLinhas.add(resultado);
        }

        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter("saida_interpolacao.txt", true))) {

            for (String linhaParaGravar : todasLinhas) {
                bw.write(linhaParaGravar);
            }
            bw.flush();
        } catch (IOException e) {
            System.err.println("Erro ao gravar dados do dia " + data + ": " + e.getMessage());
        }
    }
}
