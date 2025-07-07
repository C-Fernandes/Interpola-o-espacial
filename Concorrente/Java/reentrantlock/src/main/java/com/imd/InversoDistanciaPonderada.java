package com.imd;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

public class InversoDistanciaPonderada {

    public Ponto atualizarVizinhosMaisProximos(Ponto alvo,
            List<Ponto> candidatos,
            int k) {
        // 1) Cria um max-heap de tamanho k (usa Comparator que compara distância
        // decrescente)
        PriorityQueue<Ponto> heap = new PriorityQueue<>(k,
                (p1, p2) -> {
                    double d1 = distanciaGeografica(alvo.getLatitude(), alvo.getLongitude(),
                            p1.getLatitude(), p1.getLongitude());
                    double d2 = distanciaGeografica(alvo.getLatitude(), alvo.getLongitude(),
                            p2.getLatitude(), p2.getLongitude());
                    return Double.compare(d2, d1); // inverte para manter o maior no topo
                });

        // 2) Percorre todos os candidatos, mantendo no máximo k no heap
        for (Ponto candidato : candidatos) {
            if (heap.size() < k) {
                heap.offer(candidato);
            } else {
                Ponto pior = heap.peek();
                double dPior = distanciaGeografica(alvo.getLatitude(), alvo.getLongitude(),
                        pior.getLatitude(), pior.getLongitude());
                double dCand = distanciaGeografica(alvo.getLatitude(), alvo.getLongitude(),
                        candidato.getLatitude(), candidato.getLongitude());
                if (dCand < dPior) {
                    heap.poll();
                    heap.offer(candidato);
                }
            }
        }

        // 3) Converte o heap em List sem ordenação adicional
        List<Ponto> vizinhosK = new ArrayList<>(heap);

        // 4) Ajusta no próprio objeto 'alvo'
        alvo.setPontosProximos(vizinhosK);
        return alvo;
    }

    public double interpolarComVizinhos(Ponto alvo, int p) {
        double eps = 1e-9;
        double somaIguais = 0.0;
        int contIguais = 0;

        for (Ponto viz : alvo.getPontosProximos()) {
            double d = distanciaGeografica(
                    alvo.getLatitude(), alvo.getLongitude(),
                    viz.getLatitude(), viz.getLongitude());
            if (d < eps) {
                somaIguais += viz.getTemperatura();
                contIguais++;
            }
        }

        if (contIguais > 0) {
            return somaIguais / contIguais;
        }

        double numerador = 0.0;
        double denominador = 0.0;
        for (Ponto viz : alvo.getPontosProximos()) {
            double d = distanciaGeografica(
                    alvo.getLatitude(), alvo.getLongitude(),
                    viz.getLatitude(), viz.getLongitude());
            double peso = 1.0 / Math.pow(d, p);
            numerador += peso * viz.getTemperatura();
            denominador += peso;
        }
        return numerador / denominador;
    }

    public static double distanciaGeografica(double lat1, double lon1,
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
