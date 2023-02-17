package ch.fhnw.apm.docfinder;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public class Result {

    private final Path doc;
    private final Map<String, List<Integer>> searchHits;
    private final double relevance;

    public Result(Path doc, Map<String, List<Integer>> searchHits, double relevance) {
        this.doc = requireNonNull(doc);
        this.searchHits = requireNonNull(searchHits);
        this.relevance = relevance;
    }

    public Path getDoc() {
        return doc;
    }

    public Map<String, List<Integer>> getSearchHits() {
        return searchHits;
    }

    public double getRelevance() {
        return relevance;
    }

    public int totalHits() {
        return searchHits.values().stream()
                .mapToInt(List::size)
                .sum();
    }
}
