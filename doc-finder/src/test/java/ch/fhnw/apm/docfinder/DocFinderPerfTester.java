package ch.fhnw.apm.docfinder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.DoubleStream;

public class DocFinderPerfTester {

    private static final int REPETITIONS = 30;
    public static final String SEARCH_TEXT = "woman friend cat";

    public static void main(String[] args) throws IOException {
        var booksDir = Path.of("perf-tests/books").toAbsolutePath();
        if (!Files.isDirectory(booksDir)) {
            System.err.println("Directory perf-tests/books not found. " +
                    "Make sure to run this program in the doc-finder directory.");
            System.exit(1);
        }

        var finder = new DocFinder(booksDir);

        var latencies = new double[REPETITIONS];
        for (int i = 0; i < REPETITIONS; i++) {
            var startTime = System.nanoTime();

            finder.findDocs(SEARCH_TEXT);

            var latency = System.nanoTime() - startTime;
            latencies[i] = latency / 1_000_000.0; // convert to ms

            // print progress to err
            if ((i + 1) % 10 == 0) {
                System.err.println(i + 1 + "/" + REPETITIONS + " repetitions");
            }
        }
        System.err.println();

        for (int i = 0; i < REPETITIONS; i++) {
            System.out.printf("%.1f\n", latencies[i]);
        }
        System.out.println();

        var stats = DoubleStream.of(latencies).summaryStatistics();
        System.out.printf("Average: %.1f ms\n", stats.getAverage());
        System.out.printf("Min: %.1f ms\n", stats.getMin());
        System.out.printf("Max: %.1f ms\n", stats.getMax());
    }
}
