package com.imd.benckmark;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import com.imd.InversoDistanciaPonderada;
import com.imd.Ponto;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class IDWBenchmark {

    private InversoDistanciaPonderada idw;
    private List<Ponto> vizinhos;
    private Ponto pontoAlvo;
    private Ponto novoPonto;

    @Setup(Level.Iteration)
    public void setup() {
        idw = new InversoDistanciaPonderada();

        pontoAlvo = new Ponto(-5.18, -37.33, -9999, "2023-12-25", "15:00");
        novoPonto = new Ponto(-5.20, -37.35, 29.5, "2023-12-25", "15:00");

        vizinhos = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            vizinhos.add(new Ponto(-5.18 + i * 0.01, -37.33 + i * 0.01, 25 + i, "2023-12-25", "15:00"));
        }
    }

    @Benchmark
    public void benchmarkAtualizarVizinhos(Blackhole bh) {
        Ponto result = idw.atualizarVizinhosMaisProximos(pontoAlvo, novoPonto, new ArrayList<>(vizinhos), 5);
        bh.consume(result);
    }

    @Benchmark
    public void benchmarkInterpolar(Blackhole bh) {
        double result = idw.interpolarComVizinhos(pontoAlvo.getLatitude(), pontoAlvo.getLongitude(), vizinhos, 2);
        bh.consume(result);
    }
}