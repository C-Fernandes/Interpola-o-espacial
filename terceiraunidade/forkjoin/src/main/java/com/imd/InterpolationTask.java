package com.imd;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class InterpolationTask extends RecursiveTask<List<String>> {

    private final List<Ponto> pointsToInterpolate;
    private final List<Ponto> validPoints;
    private final InversoDistanciaPonderada idw;
    private final int p;
    private final int k;

    private static final int SEQUENTIAL_THRESHOLD = 100;

    public InterpolationTask(List<Ponto> pointsToInterpolate, List<Ponto> validPoints, int k, int p,
            InversoDistanciaPonderada idw) {
        this.pointsToInterpolate = pointsToInterpolate;
        this.validPoints = validPoints;
        this.k = k;
        this.p = p;
        this.idw = idw;
    }

    @Override
    protected List<String> compute() {
        if (pointsToInterpolate.size() <= SEQUENTIAL_THRESHOLD) {
            return computeDirectly();
        } else {
            int mid = pointsToInterpolate.size() / 2;

            InterpolationTask leftTask = new InterpolationTask(
                    pointsToInterpolate.subList(0, mid), validPoints, k, p, idw);

            InterpolationTask rightTask = new InterpolationTask(
                    pointsToInterpolate.subList(mid, pointsToInterpolate.size()), validPoints, k, p, idw);

            // Envia a primeira subtarefa para execução assíncrona. [cite: 84, 85, 87]
            leftTask.fork();

            // Executa a segunda subtarefa na thread atual.
            List<String> rightResult = rightTask.compute();

            // Aguarda o resultado da primeira subtarefa e o obtém. [cite: 93, 94, 95]
            List<String> leftResult = leftTask.join();

            // Combina os resultados.
            leftResult.addAll(rightResult);
            return leftResult;
        }
    }

    private List<String> computeDirectly() {
        List<String> results = new ArrayList<>();
        for (Ponto alvo : pointsToInterpolate) {
            List<Ponto> candidatos = new ArrayList<>();
            candidatos.addAll(validPoints);

            Ponto comVizinhos = idw.atualizarVizinhosMaisProximos(alvo, candidatos, k);
            double valorInterp = idw.interpolarComVizinhos(comVizinhos, p);

            String resultado = String.format(
                    "Ponto: %s %s (%.4f, %.4f)%n" +
                            " | Temperatura interpolada: %.2f°C%n",
                    comVizinhos.getData(),
                    comVizinhos.getHora(),
                    comVizinhos.getLatitude(),
                    comVizinhos.getLongitude(),
                    valorInterp);
            results.add(resultado);
        }
        return results;
    }
}