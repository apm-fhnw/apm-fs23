package ch.fhnw.apm.docfinder;

import java.io.IOException;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static java.util.Collections.synchronizedList;
import static java.util.Comparator.comparing;
import static java.util.Comparator.reverseOrder;
import static java.util.Objects.requireNonNull;

public class DocFinder {

    private Path rootDir;

    private int maxDepth = Integer.MAX_VALUE;
    private long sizeLimit = 1_000_000_000; // 1 GB
    private boolean ignoreCase = true;

    private final ExecutorService pool;

    public DocFinder(Path rootDir) {
        this(rootDir, Runtime.getRuntime().availableProcessors());
    }

    public DocFinder(Path rootDir, int parallelism) {
        this.rootDir = requireNonNull(rootDir);
        pool = Executors.newFixedThreadPool(parallelism, r -> {
            var thread = new Thread(r);
            thread.setDaemon(true);
            return thread;
        });
    }

    public List<Result> findDocs(String searchText) throws IOException {
        var allDocs = collectDocs();

        var results = synchronizedList(new ArrayList<Result>());

        var tasks = new ArrayList<Callable<Void>>();
        for (var doc : allDocs) {
            tasks.add(() -> {
                var res = findInDoc(searchText, doc);
                if (res.totalHits() > 0) {
                    results.add(res);
                }
                return null;
            });
        }
        try {
            pool.invokeAll(tasks);
        } catch (InterruptedException e) {
            throw new AssertionError(e);
        }

        results.sort(comparing(Result::getRelevance, reverseOrder()));

        return results;
    }

    private List<Path> collectDocs() throws IOException {
        try (var docs = Files.find(rootDir, maxDepth, this::include)) {
            return docs.toList();
        }
    }

    private boolean include(Path path, BasicFileAttributes attr) {
        return attr.isRegularFile()
                && attr.size() <= sizeLimit;
    }

    private Result findInDoc(String searchText, Path doc) throws IOException {
        String text;
        try {
            text = Files.readString(doc);
        } catch (MalformedInputException e) {
            // in case doc cannot be read as UTF-8, ignore and use empty dummy text
            text = "";
        }

        // normalize text: collapse whitespace and convert to lowercase
        var collapsed = text.replaceAll("\\p{javaWhitespace}+", " ");
        var normalized = collapsed;
        if (ignoreCase) {
            normalized = collapsed.toLowerCase(Locale.ROOT);
            searchText = searchText.toLowerCase(Locale.ROOT);
        }

        var searchTerms = parseSearchText(searchText);
        var searchHits = findInText(searchTerms, normalized);
        var relevance = computeRelevance(searchHits, normalized);
        return new Result(doc, searchHits, relevance);
    }

    private List<String> parseSearchText(String searchText) {
        if (searchText.isBlank()) {
            throw new IllegalArgumentException();
        }

        var parts = searchText
                .replaceFirst("^\\p{javaWhitespace}", "") // prevent leading empty part
                .split("\\p{javaWhitespace}+");
        return Stream.of(parts)
                .distinct() // eliminate duplicates
                .toList();
    }

    private Map<String, List<Integer>> findInText(List<String> searchTerms, String text) {
        var searchHits = new HashMap<String, List<Integer>>();
        for (var term : searchTerms) {
            var hits = new ArrayList<Integer>();
            var index = 0;
            while (index >= 0) {
                index = text.indexOf(term, index);
                if (index >= 0) {
                    hits.add(index);
                    index += term.length();
                }
            }
            searchHits.put(term, hits);
        }
        return searchHits;
    }

    private double computeRelevance(Map<String, List<Integer>> searchHits,
                                    String text) {
        if (text.isBlank()) {
            return 0;
        }
        var avgHits = searchHits.values().stream()
                .mapToDouble(List::size)
                .average().orElse(1);
        var termsWithHits = searchHits.values().stream()
                .filter(list -> list.size() > 0)
                .count();
        var termHitRatio = (double) termsWithHits / searchHits.size();
        return Math.pow(avgHits + 1, termHitRatio) / text.length() * 1_000_000;
    }

    public Path getRootDir() {
        return rootDir;
    }

    public void setRootDir(Path rootDir) {
        this.rootDir = rootDir;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public long getSizeLimit() {
        return sizeLimit;
    }

    public void setSizeLimit(long sizeLimit) {
        this.sizeLimit = sizeLimit;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public void setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
    }
}
