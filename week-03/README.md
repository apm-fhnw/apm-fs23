# APM Woche 3: Experiment-Design & Benchmarking

## Vorlesungsfolien

[Experiment-Design & Benchmarking](Experiment-Design%20und%20Benchmarking.pdf)


## Übung

Als Grundlage für die Übungen dient wieder der Doc Finder. Sie können Ihre 
eigene optimierte Version oder die Musterlösung von Woche 1 verwenden.

Sie haben bereits in den ersten beiden Wochen einfache Benchmarks 
durchgeführt, um die Performance des Doc Finders zu analysieren; dabei kam 
die handgeschriebene Klasse `DocFinderPerfTester` zum Einsatz. Das Ziel 
dieser Übung ist es, den Doc Finder mithilfe von systematischen und 
technisch sauberen Benchmarks zu analysieren. Einerseits können Sie dadurch die 
_Effekte_ von verschiedenen Parametern auf die Performance evaluieren, 
andererseits erhalten Sie durch den Einsatz eines Benchmark-Werkzeugs 
zuverlässigere Resultate.


### 1. JMH aufsetzen

Um Herausforderungen beim Benchmarking von Java-Code wie Varianz, Warmup, 
usw. anzugehen, kommt oft das
[Java Microbenchmark Harness (JMH)](https://github.com/openjdk/jmh) zum 
Einsatz. Der empfohlene Weg JMH einzusetzen, ist ein separates (Maven-)Projekt 
für die Benchmarks zu erstellen; der Einfachheit halber man kann JMH jedoch
auch direkt als Dependency zum eigentlichen Projekt hinzufügen.

Fügen Sie als ersten in der Datei 'pom.xml' folgende Dependencies hinzu:

```xml
<dependencies>
    <dependency>
        <groupId>org.openjdk.jmh</groupId>
        <artifactId>jmh-core</artifactId>
        <version>1.36</version>
    </dependency>
    <dependency>
        <groupId>org.openjdk.jmh</groupId>
        <artifactId>jmh-generator-annprocess</artifactId>
        <version>1.36</version>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

Falls Sie in einer IDE arbeiten, vergessen Sie nicht, das Maven-Projekt zu 
aktualisieren, in IntelliJ zum Beispiel durch einen Klick auf den kleinen 
Reload-Knopf, der oben rechts im Code erscheint.

Erstellen Sie eine neue Klasse `DocFinderBenchmarks`, z. B. im 'test'-Ordner 
und fügen Sie eine Benchmark-Methode hinzu:

```java
@Benchmark
public void helloJmh() {
    System.out.println("Hello, JMH!");
}
```

Um diesen Benchmark auszuführen, brauchen Sie noch eine `main`-Methode, 
welche Sie in dieselbe Klasse einfügen können:

```java
public static void main(String[] args) throws RunnerException {
    Options opt = new OptionsBuilder()
            .include(DocFinderBenchmarks.class.getSimpleName())
            .forks(1)
            .build();
    new Runner(opt).run();
}
```

Wenn Sie das Projekt jetzt kompilieren und diese Klasse ausführen, wird JMH 
automatisch Benchmark-Code generieren, der von der `main`-Methode aufgerufen 
wird und seinerseits die `helloJmh`-Methode aufruft – wiederholt, in mehreren 
Durchgängen, in separaten Java-Prozessen und unter Berücksichtigung von 
Warmup. Am Ende sollte ungefähr folgende Ausgabe stehen:

```
Benchmark                      Mode  Cnt      Score      Error  Units
DocFinderBenchmarks.helloJmh  thrpt    5  18417.440 ± 2228.921  ops/s
```

Die `println`-Methode konnte also im Durchschnitt etwa 18'400 mal pro Sekunde 
aufgerufen werden. Weiter oben in der Ausgabe finden Sie weitere Daten, 
Hinweise und Warnungen.


### 2. `findDocs` Benchmarken

Entfernen Sie die `helloJmh`-Methode und erstellen Sie einen Benchmark, der 
die `findDocs`-Methode von `DocFinder` aufruft. Legen Sie vorerst die 
Parameter (Suchtext, Suchordner, evtl. Anzahl Threads, usw.) einmalig fest; 
später werden diese von Ausführung zu Ausführung variiert.

Beachten Sie, dass der Benchmark nur die `findDocs`-Methode umfassen soll, 
nicht auch noch das Erstellen und Initialisieren des `DocFinder`s. Dazu 
brauchen Sie eine separate Methode, welche mit `@Setup` annotiert wird. 
Diese Methode kann den `DocFinder`, und evtl. weitere Objekt in Attribute 
(_fields_) speichern, auf welche man in der Benchmark-Methode zugreifen kann.
Damit das funktioniert, muss die Klasse `DocFinderBenchmarks` als 
"Benchmark-Zustand" deklariert werden; fügen Sie die Annotation
`@State(Scope.Benchmark)` oberhalb des Klassennamens ein.

Ein weiteres Problem könnte sein, dass der JIT-Compiler von Java zum Schluss 
kommt, dass das Resultat von `findDocs` gar nicht verwendet wird, und Teile 
vom Code weg-"optimiert". Um dies zu verhindern, sollte die 
Benchmark-Methode das Resultat mittels `return` zurückgeben. Der von JMH 
generierte Code verwendet dieses dann so, dass der JIT-Compiler keine 
ungewünschten Optimierungen macht.

Führen Sie den Benchmark einmal durch – Sie sollten Resultate für den 
Durchsatz erhalten. Prüfen Sie, ob diese mit früheren Resultaten vom 
`DocFinderPerfTester` übereinstimmen.

Interessanter als der (durchschnittliche) Durchsatz ist wohl die Antwort-, 
bzw. Ausführungszeit. Um diese zu messen, ändern Sie den Benchmark-Modus; 
fügen Sie dazu folgende Annotation zur Benchmark-Methode hinzu:
`@BenchmarkMode(Mode.SampleTime)`. Mit zusätzlichen Annotation, z. B.
`@Warmup(iterations = 2)` oder `@Measurement(iterations = 3, time = 5)` 
können Sie weitere Aspekte des Benchmarks steuern.


### 3. Effekt-Analyse

Ein wichtiger Aspekt einer Performance-Analyse besteht darin, die _Effekte_ 
von verschiedenen Faktoren abzuschätzen. Im Falle des Doc Finders sind 
mögliche Faktoren:
* Die Dokumentensammlung (Anzahl Dokumente, Grösse der Dokumente, ...)
* Der Suchtext, insbesondere die Anzahl der Suchbegriffe
* Die Anzahl verwendeter Threads pro Anfrage
* Berücksichtigung von Gross-/Kleinschreibung (siehe Methode `setIgnoreCase`)
* ...

Zusätzlich zu den individuellen Effekten der einzelnen Faktoren könnten auch 
_Interaktionen_ zwischen Faktoren, sogenannte _Sekundär-Effekte_ auftreten. Um 
all diese Effekte abzuschätzen, kann man ein _Factorial Design_ verwenden.

Entscheiden Sie sich für mindestens drei konkrete Faktoren, deren Effekte und 
Interaktionen Sie anhand eines faktoriellen Experiments bestimmen möchten. 
Einer davon sollte die Anzahl verwendeter Threads sein. Falls Ihre 
Implementation es nicht zulässt, diese Anzahl zu konfigurieren, erweitern 
Sie sie entsprechend, oder verwenden Sie die Musterlösung von Woche 1.

Entwerfen Sie ein 2<sup>k</sup>-Experiment, um die Effekte der Faktoren (auf 
die Antwortzeit) zu bestimmen. Für gewisse Faktoren kann man mit grosser 
Sicherheit sagen, dass ihr Einfluss _unidirektional_ ist, d. h. dass durch 
Erhöhen des Levels die Performance stetig steigen oder fallen wird; für 
diese Faktoren können Sie einfach ein Minimum und Maximum wählen. Für andere 
Faktoren, wie die Anzahl Threads, ist im Vornherein nicht unbedingt klar, 
welche Anzahl zur besten Performance führt. Entwerfen Sie dazu ein weiteres 
"Einen Faktor aufs Mal"-Experiment, das sie als Vorbereitung durchführen und 
mit dem Sie die ideale Anzahl Threads bestimmen.

Führen Sie die Experimente mithilfe von JMH durch. JMH unterstützt Faktoren 
und faktorielle Experimente "out of the box". Mit folgendem Attribut und der 
`@Param`-Annotation können Sie z. B. die Anzahl Threads als variablen Faktor 
des Experiments definieren:

```java
@Param({"1", "2", "4", "8"})
public int threads;
```

JMH sorgt automatisch dafür, dass die Benchmark-Methode für jedes Level 
dieses Faktors einmal durchgeführt wird und das `threads`-Attribut den 
entsprechenden Wert enthält. Wenn Sie mehrere Faktoren definieren, werden 
alle Kombinationen durchgetestet.

Wenn Sie alle Resultate haben, führen Sie die Effekt-Analyse durch. Stellen 
Sie eine Vorzeichentabelle auf, füllen Sie die durchschnittlichen 
Antwortzeiten für jede Kombination der Levels ein und berechnen Sie die 
Effekte und Interaktionen. Beachten Sie, dass jeder Faktor mit jedem anderen 
interagieren kann und auch alle drei (oder noch mehr).