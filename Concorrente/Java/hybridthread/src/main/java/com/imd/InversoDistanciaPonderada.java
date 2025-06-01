package com.imd;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InversoDistanciaPonderada {

    public Ponto atualizarVizinhosMaisProximos(
            Ponto alvo,
            List<Ponto> candidatos,
            int k) {
        // 1) Criamos uma lista temporária para ordenar (não mexer na lista original)
        List<Ponto> copia = new ArrayList<>(candidatos);

        // 2) Ordena 'copia' pelo critério de distância geográfica ao 'alvo'
        copia.sort(Comparator.comparingDouble(
                v -> distanciaGeografica(
                        alvo.getLatitude(), alvo.getLongitude(),
                        v.getLatitude(), v.getLongitude())));

        // 3) Se houver menos ou exatamente k candidatos, usamos todos; senão, pegamos
        // subList(0, k)
        List<Ponto> vizinhosK;
        if (copia.size() <= k) {
            vizinhosK = new ArrayList<>(copia);
        } else {
            // pega apenas os primeiros k, que são os mais próximos após a ordenação
            vizinhosK = new ArrayList<>(copia.subList(0, k));
        }

        // 4) Ajusta no próprio objeto 'alvo'
        alvo.setPontosProximos(vizinhosK);

        return alvo;
    }

    public double interpolarComVizinhos(Ponto alvo, int p) {
        double eps = 1e-9;
        List<Double> iguais = new ArrayList<>();
        for (Ponto viz : alvo.getPontosProximos()) {
            double d = distanciaGeografica(alvo.getLatitude(), alvo.getLongitude(), viz.getLatitude(),
                    viz.getLongitude());
            if (d < eps) {
                iguais.add(viz.getTemperatura());
            }
        }

        if (!iguais.isEmpty()) {
            return iguais.stream().mapToDouble(Double::doubleValue).average().orElse(-9999.0);
        }

        double numerador = 0, denominador = 0;
        for (Ponto viz : alvo.getPontosProximos()) {
            double d = distanciaGeografica(alvo.getLatitude(), alvo.getLongitude(), viz.getLatitude(),
                    viz.getLongitude());
            double peso = 1.0 / Math.pow(d, p);
            numerador += peso * viz.getTemperatura();
            denominador += peso;
        }
        return numerador / denominador;
    }

    private double distanciaGeografica(double lat1, double lon1,
            double lat2, double lon2) {
        double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                        * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
