package ch.fhnw.apm.docfinder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class DocFinderCli {

    public static void main(String[] args) throws IOException {
        new DocFinderCli().run();
    }

    private Path path;
    private DocFinder finder;

    private void run() throws IOException {
        System.out.println("APM Doc Finder");
        System.out.println("--------------");

        path = Path.of("").toAbsolutePath();
        finder = new DocFinder(path);

        var in = new Scanner(System.in);
        System.out.print(path + "> ");
        while (in.hasNextLine()) {
            var line = in.nextLine().strip();
            if (line.equals("help")) {
                help();
            } else if (line.startsWith("cd ")) {
                changeDir(line);
            } else if (line.startsWith("search ")) {
                search(line);
            } else if (line.equals("quit") || line.equals("exit")) {
                return;
            } else {
                System.out.println("Error: no such command.\n");
                help();
            }
            System.out.print(path + "> ");
        }
    }

    private void help() {
        System.out.println("""
                Available commands:
                help            show this help
                cd <dir>        change to the given directory
                search <terms>  search the current directory for
                                documents that contain the given
                                search terms
                quit / exit     quit the program
                """);
    }

    private void changeDir(String line) {
        var dir = line.substring("cd ".length()).strip();
        var resolved = path.resolve(dir).normalize();
        if (Files.isDirectory(resolved)) {
            path = resolved;
            finder.setRootDir(path);
        } else {
            System.out.println("Error: no such directory: " + resolved);
        }
    }

    private void search(String line) throws IOException {
        var searchTerms = line.substring("search ".length()).strip();
        var results = finder.findDocs(searchTerms);
        for (var res : results) {
            System.out.println(res.getDoc());
            System.out.format("  Relevance: %.1f\n", res.getRelevance());
            for (var e : res.getSearchHits().entrySet()) {
                System.out.println("  " + e.getKey() + ": " + e.getValue().size() + " hits");
            }
            System.out.println();
        }
    }
}
