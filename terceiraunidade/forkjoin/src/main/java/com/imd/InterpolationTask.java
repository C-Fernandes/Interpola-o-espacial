package com.imd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.RecursiveTask;
import java.util.Map;

public class InterpolationTask extends RecursiveTask<List<String>> {

    private static final int SEQUENTIAL_THRESHOLD = 5;

    private final List<Ponto> pointsToInterpolate;
    private final Map<String, List<Ponto>> validPointsByHour;
    private final InversoDistanciaPonderada idw;
    private final String batchDate;
    private final int p;
    private final int k;

    public InterpolationTask(List<Ponto> pointsToInterpolate, Map<String, List<Ponto>> validPointsByHour,
            String batchDate, InversoDistanciaPonderada idw, int p, int k) {
        this.pointsToInterpolate = pointsToInterpolate;
        this.validPointsByHour = validPointsByHour;
        this.batchDate = batchDate;
        this.idw = idw;
        this.p = p;
        this.k = k;
    }

    @Override
    protected List<String> compute() {
        if (pointsToInterpolate.size() <= SEQUENTIAL_THRESHOLD) {
            return computeDirectly();
        }
        int middle = pointsToInterpolate.size() / 2;
        InterpolationTask subtask1 = new InterpolationTask(
                pointsToInterpolate.subList(0, middle),
                validPointsByHour, batchDate, idw, p, k);
        InterpolationTask subtask2 = new InterpolationTask(
                pointsToInterpolate.subList(middle, pointsToInterpolate.size()),
                validPointsByHour, batchDate, idw, p, k);
        subtask1.fork();
        List<String> result2 = subtask2.compute();
        List<String> result1 = subtask1.join();
        List<String> combinedResult = new ArrayList<>(result1);
        combinedResult.addAll(result2);
        return combinedResult;
    }

    private List<String> computeDirectly() {
        List<String> results = new ArrayList<>();
        for (Ponto target : pointsToInterpolate) {
            List<Ponto> candidates = validPointsByHour.getOrDefault(target.getHora(), Collections.emptyList());
            Ponto pointWithNeighbors = idw.atualizarVizinhosMaisProximos(target, candidates, k);
            double interpolatedValue = idw.interpolarComVizinhos(pointWithNeighbors, p);

            String formattedResult = String.format(
                    "Ponto: %s %s (%.4f, %.4f)%n | Temperatura interpolada: %.2f°C%n",
                    batchDate, pointWithNeighbors.getHora(), pointWithNeighbors.getLatitude(),
                    pointWithNeighbors.getLongitude(),
                    interpolatedValue);
            results.add(formattedResult);
        }
        return results;
    }
}