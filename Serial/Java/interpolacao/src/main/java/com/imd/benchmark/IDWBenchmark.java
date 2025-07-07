package com.imd.benchmark;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException; // Certifique-se de que o pacote está correto para todas as suas classes
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import com.imd.InversoDistanciaPonderada;
import com.imd.LeitorCSV;
import com.imd.Ponto;
// import java.util.concurrent.Future; // Não é mais explicitamente necessário aqui

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 4, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 2, jvmArgsAppend = { "-Xms1G", "-Xmx1G" })
public class IDWBenchmark {

    @State(Scope.Benchmark)
    public static class EstadoDoBenchmark {
        public String caminhoArquivoCSVTemporario = "temp_benchmark_input.csv";
        public Ponto pontoAlvoOriginal;
        public List<Ponto> listaDeCandidatosGeral;
        public List<Ponto> pontosAlvoParaLoteBenchmark;
        public String dataParaLoteBenchmark;
        public int kVizinhos = 5;
        public int pExpoente = 2;
        public InversoDistanciaPonderada idw;
        private Random random = new Random(12345);

        // Instância de LeitorCSV gerenciada pelo JMH
        public LeitorCSV leitor; // Mova a declaração aqui

        @Setup(Level.Trial)
        public void setupTrial() throws IOException {
            idw = new InversoDistanciaPonderada();
            leitor = new LeitorCSV(); // Inicialize LeitorCSV uma vez por trial para reutilizar executores
            listaDeCandidatosGeral = new ArrayList<>();
            String dataComum = "2024-01-01";
            String horaComum = "12:00:00";
            dataParaLoteBenchmark = dataComum;

            for (int i = 0; i < 100; i++) {
                listaDeCandidatosGeral.add(new Ponto(
                        -20 + random.nextDouble() * 10,
                        -50 + random.nextDouble() * 10,
                        random.nextDouble() * 30 + 5,
                        dataComum, horaComum));
            }
            pontoAlvoOriginal = new Ponto(-15, -45, -9999.0, dataComum, horaComum);
            pontosAlvoParaLoteBenchmark = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                pontosAlvoParaLoteBenchmark.add(new Ponto(
                        -19 + random.nextDouble() * 8,
                        -49 + random.nextDouble() * 8,
                        -9999.0,
                        dataComum, horaComum));
            }
            try (PrintWriter writer = new PrintWriter(new FileWriter(caminhoArquivoCSVTemporario))) {
                writer.println("ID,Data,Hora,Temperatura,Latitude,Longitude");
                int idCounter = 1;
                for (int i = 0; i < 20; i++) {
                    Ponto p = listaDeCandidatosGeral.get(i);
                    writer.printf("%d,%s,%s,%.2f,%.4f,%.4f%n",
                            idCounter++, p.getData(), p.getHora(), p.getTemperatura(), p.getLatitude(),
                            p.getLongitude());
                }
                Ponto pInterpol = pontosAlvoParaLoteBenchmark.get(0);
                writer.printf("%d,%s,%s,%.2f,%.4f,%.4f%n",
                        idCounter++, pInterpol.getData(), pInterpol.getHora(), -9999.00, pInterpol.getLatitude(),
                        pInterpol.getLongitude());
                writer.printf("%d,%s,%s,%.2f,%.4f,%.4f%n",
                        idCounter++, "2024-01-02", "10:00:00", 25.5, -12.34, -45.67);
                writer.printf("%d,%s,%s,%.2f,%.4f,%.4f%n",
                        idCounter++, "2024-01-02", "10:00:00", -9999.00, -12.30, -45.60);
            }
        }

        @TearDown(Level.Trial)
        public void tearDownTrial() {
            File tempFile = new File(caminhoArquivoCSVTemporario);
            if (tempFile.exists())
                tempFile.delete();
            File outputInterpolationFile = new File("saida_interpolacao.txt");
            if (outputInterpolationFile.exists())
                outputInterpolationFile.delete();

            // NOTA: Como não podemos modificar LeitorCSV, não há um método explícito
            // para desligar seus ExecutorServices aqui. Eles continuarão ativos
            // até o fim da JVM do benchmark. Isso pode impactar medições subsequentes
            // se o JMH decidir reutilizar a JVM para outros benchmarks, mas para um único
            // fork como configurado (@Fork(value = 1)), o impacto é limitado à própria
            // execução.
            // Em cenários de produção, o gerenciamento de ciclos de vida de
            // ExecutorServices
            // deve ser explícito.
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            // Limpa o arquivo de saída para cada invocação do benchmark.
            // Isso é importante para isolar as medições, mas note que a escrita
            // concorrente do LeitorCSV original ainda pode causar variabilidade
            // se o arquivo não for sincronizado internamente em LeitorCSV.
            File arquivoDeSaida = new File("saida_interpolacao.txt");
            if (arquivoDeSaida.exists()) {
                arquivoDeSaida.delete();
            }
        }
    }

    @Benchmark
    public void benchmarkLerEInterpolar(EstadoDoBenchmark estado, Blackhole bh) {
        // Usa a instância de leitor do estado.
        estado.leitor.lerEInterpolar(estado.caminhoArquivoCSVTemporario);
        File arquivoDeSaidaReal = new File("saida_interpolacao.txt");
        if (arquivoDeSaidaReal.exists()) {
            bh.consume(arquivoDeSaidaReal.length());
        } else {
            bh.consume(0L);
        }
    }

    @Benchmark
    public void benchmarkProcessarLote(EstadoDoBenchmark estado, Blackhole bh) {
        // @Setup(Level.Invocation) limpou "saida_interpolacao.txt"
        // Usa a instância de leitor do estado, que é reutilizada e seus executores não
        // são recriados.
        List<Ponto> alvosParaLote = new ArrayList<>(estado.pontosAlvoParaLoteBenchmark);
        List<Ponto> validosParaLote = new ArrayList<>(estado.listaDeCandidatosGeral);

        // O método processarLote realiza interpolações (síncronas devido a Future.get()
        // interno)
        // e submete a escrita em arquivo de forma assíncrona.
        estado.leitor.processarLote(estado.dataParaLoteBenchmark, alvosParaLote, validosParaLote);

        // Consumimos as entradas para evitar DCE na parte computacional.
        bh.consume(alvosParaLote);
        bh.consume(validosParaLote);

        // NOTA IMPORTANTE: Como não podemos modificar LeitorCSV para sincronizar
        // a escrita do arquivo ou esperar pela sua conclusão, este benchmark
        // medirá o tempo para SUBMETER a tarefa de escrita assíncrona.
        // A variabilidade da operação real de I/O pode ainda ser observada
        // na margem de erro, pois a escrita não é sincronizada dentro do LeitorCSV
        // e não há garantia de que as Virtual Threads de I/O de um LeitorCSV
        // não interfiram com as de outro LeitorCSV (se houvesse múltiplos forks
        // ou instâncias de LeitorCSV escrevendo simultaneamente) ou com o próprio
        // sistema de arquivos.
    }

    @Benchmark
    public Ponto benchmarkAtualizarVizinhos(EstadoDoBenchmark estado, Blackhole bh) {
        Ponto alvo = new Ponto(estado.pontoAlvoOriginal);
        Ponto resultado = estado.idw.atualizarVizinhosMaisProximos(alvo, estado.listaDeCandidatosGeral,
                estado.kVizinhos);
        bh.consume(resultado);
        return resultado;
    }

    @Benchmark
    public double benchmarkInterpolarComVizinhos(EstadoDoBenchmark estado, Blackhole bh) {
        Ponto alvoComVizinhos = new Ponto(estado.pontoAlvoOriginal);
        estado.idw.atualizarVizinhosMaisProximos(alvoComVizinhos, estado.listaDeCandidatosGeral, estado.kVizinhos);
        double valorInterpolado = estado.idw.interpolarComVizinhos(alvoComVizinhos, estado.pExpoente);
        bh.consume(valorInterpolado);
        return valorInterpolado;
    }

    @Benchmark
    public double benchmarkDistanciaGeografica(EstadoDoBenchmark estado, Blackhole bh) {
        if (estado.listaDeCandidatosGeral.size() < 2)
            return 0.0;
        Ponto p1 = estado.listaDeCandidatosGeral.get(0);
        Ponto p2 = estado.listaDeCandidatosGeral.get(1);
        double distancia = InversoDistanciaPonderada.distanciaGeografica(
                p1.getLatitude(), p1.getLongitude(), p2.getLatitude(), p2.getLongitude());
        bh.consume(distancia);
        return distancia;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(IDWBenchmark.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}