# APM Woche 1: Einführung Performance-Analyse & Parallelisierung

## Vorlesungsfolien

[Einführung Performance-Analyse](Einführung%20Performance-Analyse.pdf)


## Übung


Klonen Sie dieses Git-Repository um die Übungsvorlage zu erhalten. Um neues
Material zu erhalten, pullen Sie einfach wieder von diesem Repository. Wenn Sie
Ihre eigenen Änderungen auf GitHub pushen wollen, forken Sie das Repository
stattdessen.


### 1. App builden und starten

Im Ordner 'doc-finder' befindet sich der Code für den _Doc Finder_, eine 
Java-Komponente, welche Dokumente auf dem Dateisystem nach bestimmten 
Suchbegriffen durchsucht. Anhand dieser Komponente sollen Sie erste 
Erfahrungen mit dem Messen und Optimieren von Performance machen.

Der Code ist als Maven-Projekt organisiert. Es setzt Java in der Version 17 
voraus. Es ist empfehlenswert, das Projekt in eine Entwicklungsumgebung wie 
IntelliJ oder Eclipse zu importieren. Wenn Sie die entsprechende 
Java-Version installiert haben (IntelliJ macht automatisch entsprechende 
Vorschläge) sollte Ihre IDE den Code nach dem Import automatisch builden.
Führen Sie die mitgelieferte Kommandozeilen-Applikation `DocFinderCli` aus, 
um die Komponente zu testen. Geben Sie `help` ein, um die verfügbaren 
Befehle anzuzeigen. Mit `quit` beenden Sie die App.

Das Projekt enthält ebenfalls eine Sammlung von Dokumenten, mit welchen Sie 
die Suche testen und später evaluieren können. Sie befindet sich im Ordner 
'perf-tests/books'. Wechseln Sie innerhalb der Kommandozeilen-App mittels 
`cd <Ordner>` in diesen Ordner und suchen Sie z. B. nach "wonderland": 
`seach wonderland`. Sie sollten mehrere Ergebnisse erhalten.


### 2. Performance beobachten

Im Ordner 'src/test/java' (im entsprechenden Unterordner für das Package) 
befindet sich ein weiteres Programm namens `DocFinderPerfTester`, welches 
Suchanfragen an den Doc Finder stellt und die Antwortzeit misst. Führen Sie 
das Programm einmal aus und schauen Sie die gemeldete Performance an.

Studieren Sie nun den `DocFinderPerfTester` im Detail. Machen Sie Änderungen 
an der Konfiguration (z. B. Anzahl Wiederholungen oder Suchtext) und 
beobachten Sie deren Einfluss auf die Performance. Führen mehr Suchbegriffe 
(duch Leerzeichen getrennt) zu einer längeren Antwortzeit? Speichern Sie ein 
paar der Performance-Resultate für später.

Berechnen Sie den durchschnittlichen Durchsatz des Doc Finders, in Suchanfragen 
pro Sekunde. Beobachten Sie ausserdem mal den Ressourcenverbrauch 
(CPU-Auslastung und Speicherverbrauch) auf Ihrem Computer, auf Windows zum 
Beispiel ganz einfach mit dem Task Manager.


### 3. Parallelisierung

Sie haben vermutlich festgestellt, dass die CPU-Auslastung während der 
Ausführung des `DocFinderPerfTester`s relativ hoch ist – allerdings wird nur 
ein einziger Core der CPU verwendet. Der Doc Finder ist _single-threaded_.

Studieren Sie den Code der Klasse `DocFinder`. Wo sehen Sie Möglichkeiten 
für Parallelisierung? Welche Teile des Codes brauchen wohl am meisten Zeit? 
Sie können auch ein paar weitere Zeitmessungen einbauen, indem Sie an 
verschiedenen Orten `System.nanoTime()` aufrufen und die Differenzen auf der 
Konsole ausgeben. Falls Sie bereits andere Werkzeuge (z. B. Profiler) kennen,
können Sie auch diese ausprobieren.

Entscheiden Sie sich für ein Stück Code, das Sie parallelisieren möchten, und 
ändern Sie den Code entsprechend ab. Sie können entweder explizit mit 
`Thread`-Objekten arbeiten, mit einem Thread-Pool, z. B. durch
`Executors.newFixedThreadPool(n)` oder mit einem parallelen Stream: 
`myList.parallelStream()`.

Achten Sie darauf, dass Sie nicht aus Versehen einen Concurrency-Bug
einbauen. Das gleichzeitige Verändern von Objekten (wie z. B. Collections) aus 
mehreren Threads ist per se nicht sicher – egal ob die Threads von Hand 
erstellt wurden, durch einen Thread-Pool oder durch einem parallelen Stream.
Der einfachste (aber nicht unbedingt performanteste) Weg, aus mehreren 
Threads sicher auf ein Objekt zuzugreifen, ist mittels eines 
`synchronized`-Blocks:

```java
synchronized (myList) {
    myList.add(object);
}
```

Solange Sie nicht mehrere (verschachtelte) `synchronized`-Blöcke in Ihrem 
Programm haben, sollten Sie dadurch keine weiteren Probleme (Deadocks) bekommen.


### 4. Speedups berechnen

Wenn die Ihre Parallelisierung erfolgreich war, sollten Sie jetzt eine 
bessere Performance des Doc Finders beobachten können. Führen Sie den
`DocFinderPerfTester` erneut aus und vergleichen Sie die Resultate mit jenen,
die Sie vor der Parallelisierung erhalten haben. Berechnen Sie die
(durchschnittlichen) Speedups im Vergleich zur originalen
Single-Threaded-Version, am besten für verschiedene Suchbegriffe. Beobachten 
Sie ebenfalls nochmals den Ressourcenverbrauch. Werden jetzt alle CPUs voll 
ausgelastet?

Wenn Sie Threads von Hand erstellen oder einen Thread-Pool verwenden, bei 
dem man die Anzahl Threads konfigurieren kann (siehe oben), dann führen Sie ein 
paar Messungen mit unterschiedlicher Anzahl Threads durch. Versuchen Sie, 
ein Speedup-Diagramm zu erstellen, wie in der Vorlesung gezeigt.
