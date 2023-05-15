package ch.fhnw.apm.io;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.SampleTime)
@Warmup(iterations = 1)
@Measurement(iterations = 5)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
public class Locality {

    byte[][] array = randomArray(4000, 4000);

    private static byte[][] randomArray(int m, int n) {
        var random = new Random();
        var array = new byte[m][n];
        for (int i = 0; i < m; i++) {
            random.nextBytes(array[i]);
        }
        return array;
    }

    @Benchmark
    public int sumByRows() {
        int m = array.length;
        int n = array[0].length;
        int sum = 0;
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                sum += array[i][j];
            }
        }
        return sum;
    }

    @Benchmark
    public int sumByCols() {
        int m = array.length;
        int n = array[0].length;
        int sum = 0;
        for (int j = 0; j < n; j++) {
            for (int i = 0; i < m; i++) {
                sum += array[i][j];
            }
        }
        return sum;
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(Locality.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
