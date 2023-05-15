# APM Woche 11: Performance Testing im Web


[Vorlesungsfolien](Performance%20Testing%20im%20Web.pdf)


## Übung

Nachdem es in den letzten Übungen vor allem um Infrastruktur-Aspekte
(Load Balancing, Clustering, High Availability) ging, geht es in dieser
Übung einerseits um Applikations-Aspekte und andererseits wieder um
das Durchführen von Performance-Messungen.


### 1. Cluster-Speicher – Vorbereitung

Ein Problem, das wir in den letzten Woche ignoriert haben, ist die 
Synchronisation zwischen den Instanzen der Key–Value-Store-App.
Wenn die App auf mehreren Servern "balanciert" wird, ergibt sich natürlich
das Problem, dass Werte, die auf dem einen Server gespeichert wurden, auf
dem anderen Server nicht sichtbar sind. Mit der NGINX-Loadbalancing-Methode
`ip_hash` (IP-basierte Persistenz) wird dieses Problem zwar teilweise
umgangen, aber realistischerweise müssen Applikationsdaten irgendwann
zwischen den Maschinen synchronisiert werden. Dafür erweitern Sie die App in
dieser Übung um einen "In-Memory-Datagrid" (IMDG), einem Datenspeicher für
Cluster, welcher von den Cluster-Knoten vollständig im Hauptspeicher
gehalten und automatisch synchronisiert wird.

Um den Effekt dieses verteilten Speichers zu sehen, müssen Sie aber auf die
einzelnen Web-Server zugreifen können (quasi das Loadbalancing umgehen).
Ersetzen Sie in der Docker-Compose-Datei dazu den 'web-app'-Service mit den
zwei Replicas durch zwei separate Services 'web-app-1' und 'web-app-2',
denen Sie jeweils einen Port auf der Host-Maschine zuweisen, z. B. mit
`8081:8080` und `8082:8080`. Damit die Loadbalancers weiterhin funktionieren,
müssen Sie auch die NGINX-Konfiguration anpassen: die Web-Server sind mit
dieser Konfiguration nicht mehr unter `key-val-store_web-app_1` und `..._2`
erreichbar, sondern unter `key-val-store_web-app-1_1` und `...-2_1`.

Überprüfen Sie, dass Sie auf beide Web-Server zugreifen können, indem Sie
[localhost:8081](http://localhost:8080) und
[localhost:8082](http://localhost:8080) in Ihrem Browser öffnen. Die
Hostnamen sollten unterschiedlich sein. Wenn Sie auf einem der beiden Server
einen Wert speichern, wird er auf dem anderen Server nicht erscheinen.


### 2. Ein Cluster-Speicher mit Hazelcast

Wenn Sie den Quellcode der Web-App anschauen, sehen Sie in der Hauptklasse
`KeyValStoreApp`, dass als `Storage`-Implementation die Klasse `LocalStorage`
verwendet wird. Diese speichert die Key–Value-Paare ganz einfach in einer
lokalen Hash Map. Das Projekt enthält aber bereits auch die Implementation des
IMDG-basierten Speichers; Sie finden Sie in der Klasse `ClusterStorage`.
Diese Klasse verwendet die [Hazelcast-Bibliothek](https://hazelcast.org/),
eine Open-Source-Bibliothek, welche in Java geschrieben ist (aber auch
Bindings für viele andere Sprachen bietet).

Wie Sie sehen, ist die Konfiguration trivial; als einziger Parameter
verlangt die Klasse einen Namen für die verteilte Map. Ohne weitere
Konfiguration "entdecken sich" die Hazelcast-Instanzen automatisch über
Broadcast-Messages im lokalen Netzwerk und synchronisieren sämtliche
Änderungen an der Map. Ändern Sie die `KeyValStoreApp`-Klasse also so, dass die
`ClusterStorage`-Implementation verwendet wird (wählen Sie einen Namen für die
verteilte Map).

Deployen Sie die gesamte App mittels Docker Compose erneut (vergessen Sie
nicht, vorher das `package`-Goal von Maven auszuführen; siehe
[Woche 6](../week-06)). Im Output der Web-Server sollten Sie jetzt die 
Log-Messages von Hazelcast sehen und beobachten können, wie sich die beiden 
Instanzen zu einem Cluster zusammenschliessen:

    Members {size:2, ver:2} [
        Member [172.19.0.5]:5701 - 6deb58a7-9b68-4f9a-8f22-8ac547fc1a50 this
        Member [172.19.0.3]:5701 - b831bc0f-bf14-446c-b411-1deb827fd220
    ]

Die Details der Ausgabe können natürlich variieren, aber nach einer kurzen
Zeit sollten in beiden Logs die beiden Instanzen erscheinen. Stellen Sie
sicher, dass der Speicher richtig funktioniert, indem Sie direkt auf das
Web-Interface des einen Servers zugreifen, dort einen Wert speichern, und
mit dem Web-Interface des anderen Servers den Wert für denselben Schlüssel
laden. Sie sollten die Änderung praktisch sofort sehen können.


### 3. Direkt auf den REST-Dienst zugreifen

Das Web-Interface der Applikation greift mittels `XMLHttpRequest` auf den
Key–Value-Store zu (siehe 'script.js' im Ordner 'src/main/resources/static').
Der Server stellt dafür unter der URL `/store/{id}` einen einfachen REST-Dienst
zur Verfügung (siehe Klasse `StoreController`).

Sie können den REST-Dienst testen, indem Sie z. B. mit dem
Kommandozeilenprogramm `curl` HTTP-Anfragen machen. (`curl` ist auf Linux oft
schon vorinstalliert; auf anderen Systemen müssen Sie es separat installieren.)
Der Befehl:

    curl -X PUT -H "Content-Type: text/plain" -d Hello localhost:8080/store/42

sendet einen PUT-Request an den Server und speichert unter dem Schlüssel `42`
den Wert `Hello`. Danach können Sie den Wert mit einem GET-Request wieder
abfragen:

    curl localhost:8080/store/42

(Beachten Sie, dass die Ausgabe möglicherweise keinen Zeilenumbruch enthält
und deshalb am Anfang der nächsten Eingabeaufforderung erscheint.) Sie können
zusätzlich die Option `-v` verwenden,um die HTTP-Header zu sehen, falls sich
die Befehle z. B. nicht so verhalten, wie Sie denken.


### 4. Load Testing mit JMeter

Jetzt sind Sie bereit, Performance-Tests durchzuführen. Laden Sie dafür
[JMeter](https://jmeter.apache.org/download_jmeter) herunter. Es muss Java 8
installiert sein, um das Programm auszuführen.

Erstellen Sie eine Serie von Lasttests mit JMeter, welche die 
Performance-Merkmale Ihrer Web-Applikation unter verschiedenen Szenarien 
ausmessen, vor allem die Lese- und Schreibperformance des REST-Dienstes. 
Überlegen Sie sich zuerst gute Test-Szenarien (Sie dürfen beliebige Annahmen 
treffen, wenn Sie sie vernünftig begründen können) und fragen Sie sich, 
welche Resultate Sie erwarten.

Als Hilfestellung können Sie sich zu folgenden Aspekten Gedanken machen:

* **Zugriffs-Art**: Mehrere Szenarien sind möglich: Ein Benutzer schreibt und
  liest immer auf der selben Node; ein Benutzer schreibt immer auf derselben
  Node, aber seine Änderungen müssen auf den anderen Nodes sichtbar sein; oder
  ein Benutzer schreibt und liest eventuell auf verschiedenen Nodes.
* **Daten**: Die Datenmenge kann ein Unterschied für die Performance machen.
  Was wird gemessen, wenn ein Benutzer statt kleiner Werte grosse
  Datenmengen speichert?
* **Lese-/Schreibverhältnis**: Wie definieren Sie ein vernünftiges
  Lese-/Schreib-Verhältnis?

Wenn Sie gerne Feedback zur Ihrer Lösung möchten, können Sie die JMX-Datei 
von JMeter per E-Mail an mich senden.


### 5. Stress Testing

Verwenden Sie JMeter nun, um einen _Stresstest_ für Ihre Applikation zu 
erstellen. Sie können natürlich die Testpläne, die Sie für die letzte Übung 
entwickelt haben, zu einem Stresstest umbauen. Verwenden Sie den Stresstest, 
um folgende Fragen zu beantworten:
* Ab welcher Last wird die Applikation unzuverlässig?
* Ab welcher Last wird die Applikation unbenutzbar?
* Welche Komponente der Applikation (Loadbalancer, Web-Server,
  Cluster-Speicher) wird als Erstes zum Bottleneck? Um diese Frage zu
  beantworten, sollten Sie auch die Logs der beteiligten Container zur Rate
  ziehen. Versuchen Sie, das erste identifizierte Bottleneck zu beheben.
  Falls Sie hier nicht weiterkommen, finden Sie in einem zusätzlichen
  Dokument ein paar Hilfestellungen (Vorsicht Spoiler):
  [Bottleneck beheben](Bottleneck.md)

Die Antworten zu diesen Fragen hängen von verschiedenen Umständen und Parameter
ab.
Untersuchen Sie das Verhalten, wenn Sie folgende Variablen ändern:
* Die Art der Last: nur lesend, nur schreibend oder gemischt.
* Die Anzahl App-Server. Vergleichen Sie das Verhalten für 1, 2 und 4 Server.
  Dazu ändern Sie die Docker-Compose- und NGINX-Konfiguration und deployen die
  App jeweils vor dem Testen neu. Sie können auch mehrere Compose-Dateien erstellen.
* Die Leistungsfähigkeit der Loadbalancer oder der App-Server.

Docker ermöglicht es, einzelnen Containern mehr oder weniger CPU-Zeit oder
RAM zur Verfügung zu stellen. In der Docker-Compose-Datei können Sie das
steuern, indem Sie z. B. folgendes Konfigurationselement zu einem Service
hinzufügen:

    deploy:
      resources:
        limits:
          cpus: '0.5'

Dies führt dazu, dass für den entsprechenden Container nur eine "halbe" CPU
zur Verfügung steht. Sie können den Wert auch weiter reduzieren. Verwenden
Sie z. B. die Aufstartzeit der Spring-Boot-Applikation (wird im Log
angezeigt) als Hinweis dafür, wie stark die App unter der Einschränkung
"leidet".

Beachten Sie, dass es verschiedene mögliche Kriterien für "unzuverlässig" und
"unbenutzbar" gibt. Beobachten Sie sowohl Antwortzeiten als auch fehlerhafte
Server-Antworten.
