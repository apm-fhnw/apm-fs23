# APM Woche 4: Datenauswertung & Präsentation

## Vorlesungsfolien

[Datenauswertung & Präsentation](Datenauswertung%20und%20Präsentation.pdf)


## Übung

Als Grundlage für diese Übung dienen Ihre Resultate aus den bisherigen 
Übungen. Er geht darum, diese statistisch auszuwerten und visuell zu 
präsentieren.


### 1. Konfidenzintervalle in JMH

Führen Sie nochmals Ihre Benchmarks von letzter Woche aus und schauen Sie 
sich die statistischen Informationen an, welche JMH ausgibt. Verstehen Sie 
diese?

Ändern Sie den Benchmark-Modus auf `@BenchmarkMode(Mode.Throughput)`; JMH 
berechnet automatisch Standardabweichungen und Konfidenzintervalle. Wie 
beurteilen Sie diese?

Versuchen Sie, die Konfidenzintervalle zu reduzieren, indem Sie die Anzahl 
Iterationen erhöhen. Um Zeit zu sparen, können Sie den Warmup und die Zeit 
pro Iteration reduzieren. Verwenden Sie die `@Warmup` und 
`@Measurement`-Annotationen.


### 2. Präsentation von Performance-Resultaten

Versuchen Sie, die wichtigsten Resultate, die Sie letzte Woche erhalten 
haben, als Diagramme darzustellen (oder auch in den vorherigen Wochen, z. B. 
für Optimierungsversuche). Entscheiden Sie sich für ein Hilfsmittel, z. B. 
Excel, und probieren Sie, anhand der in der Vorlesung besprochenen 
Richtlinien, möglichst "gute" Diagramme zu entwerfen.
