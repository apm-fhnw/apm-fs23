# APM Woche 5: I/O & Buffering

## Vorlesungsfolien

[I/O & Buffering](IO%20und%20Buffering.pdf)


## Übung

In dieser Übung optimieren Sie die Performance von Datei-I/O-Operationen in 
Java. Ausgangslage ist der in der Vorlesung gezeigte Benchmark, der mittels
`InputStream.read` einzelne Bytes einliest. Dieser befindet sich im Projekt
'io-benchmarks' in diesem Repository.

Um die Performance von verschiedenen Implementationen zu vergleichen, 
kopieren Sie eine vorhandene Benchmark-Methode, benennen Sie sie um und 
füllen Sie die neue Implementation ein. Mit der aktuellen Konfiguration 
führt JMH nacheinander alle Benchmark-Methoden in der Klasse aus.


### 1. Buffering in Java

Um zu verhindern, dass für jedes Byte teure `native`-Aufrufe und vor allem 
System Calls gemacht werden, ändern Sie den Lese-Code so ab, dass die Daten 
in einem `byte`-Array gebuffert werden.

Probieren Sie zwei Varianten aus: Einerseits indem Sie den `InputStream` in 
einen `BufferedInputStream` packen, der intern einen Buffer verwendet und 
auf den man weiterhin mit einzelnen `read`-Aufrufe zugreifen kann. Und 
andererseits, indem Sie den unveränderten `InputStream` verwenden, aber selber 
ein `byte`-Array erstellen und mittels `read(array)` immer mehrere Bytes auf 
Mal einlesen.

Welche Implementation ist schneller? Warum wohl? Probieren Sie auch mal 
verschiedene Dateigrössen aus, indem Sie den Benchmark mittels `@Param(...)` 
parametrisieren (siehe [Woche 3](../week-03))


### 2. Buffergrösse tunen

Wo ein Buffer ist, ist ein tune-barer Parameter. Fügen Sie Ihrem/Ihren 
Benchmarks einen Parameter für die Buffergrösse hinzu und vergleichen Sie 
verschiedene Grössen. Gibt es eine ideale Buffergrösse?


### 3. Java NIO

Die Klassen `InputStream`, `OutputStream` usw. bieten zwar ein einfaches 
Modell für I/O, dieses ist aber weit davon entfernt, wie das Betriebssystem 
effektiv mit Dateien arbeitet. Im Package `java.nio` gibt es neuere Klassen, 
welche enger mit dem Betriebssystem bzw. der JVM zusammenarbeiten und 
dadurch teilweise bessere Performance bieten.

Informieren Sie sich im
[Skript von Zoltán Majó und Christoph Denzler](script_nio.pdf) über diese 
neuen Klassen und schreiben Sie den Benchmark so um (bzw. fügen Sie einen 
neuen hinzu), dass er statt einem `InputStream` einen `Channel` und einen 
`ByteBuffer` verwendet. Interessant ist vor allem die Möglichkeit, mittels
`ByteBuffer.allocateDirect(bufferSize)` einen Buffer zu erzeugen, der nicht 
auf dem Java-Heap, sondern im "nativen" Speicher der JVM angelegt wird.

Wie verhält sich die Performance dieser NIO-Klassen im Vergleich zu einem 
normalen `byte`-Array-Buffer?
