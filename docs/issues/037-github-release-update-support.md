# Issue 037: GitHub Release Update Support

## Metadaten

* Status: Offen
* Priorität: P2
* Typ: Feature
* Bereich: Release / Updates

## Ziel

SwitchWerk soll Anwender über neue verfügbare Versionen informieren und die Aktualisierung direkt aus der App heraus ermöglichen.

Die App soll die installierte Version mit der neuesten veröffentlichten GitHub-Release-Version vergleichen, verfügbare Updates anzeigen, Release Notes bereitstellen sowie die APK herunterladen und zur Installation bereitstellen.

## Hintergrund

Bei einer Verteilung über GitHub Releases müssen Anwender derzeit manuell prüfen, ob eine neue Version verfügbar ist, die passende APK suchen und herunterladen.

Eine integrierte Update-Funktion verbessert die Wartbarkeit und Benutzerfreundlichkeit und erhöht die Wahrscheinlichkeit, dass Anwender aktuelle Versionen verwenden.

## Scope

### Versionsanzeige

* Anzeige der aktuell installierten App-Version in den Einstellungen.
* Anzeige der zuletzt geprüften verfügbaren Version, sofern vorhanden.

### Automatische Update-Prüfung

* Prüfung auf neue Versionen über die GitHub Releases API.
* Die Prüfung erfolgt beim App-Start.
* Zusätzlich gibt es eine manuelle Prüfung über einen Button `Auf Updates prüfen`.
* Das Ergebnis wird lokal zwischengespeichert, um unnötige Netzwerkanfragen zu vermeiden.
* Eine automatische Update-Prüfung wird höchstens einmal pro Kalendertag durchgeführt.
* Wurde am aktuellen Tag bereits erfolgreich geprüft, wird das zwischengespeicherte Ergebnis verwendet.
* Die manuelle Prüfung über `Auf Updates prüfen` umgeht diese Begrenzung.

### Versionsvergleich

* Vergleich der installierten Version mit der neuesten veröffentlichten Release-Version.
* Pre-Releases werden nicht berücksichtigt.
* Debug-Builds werden nicht als updatefähige Release-Version behandelt.

### Update-Hinweis

Falls eine neuere Version verfügbar ist:

* Anzeige eines Hinweises im Dashboard und/oder in den Einstellungen.
* Anzeige der verfügbaren Versionsnummer.
* Anzeige einer Aktion zum Herunterladen der APK.

### Release Notes

* Anzeige der Release Notes der neuesten Version.
* Lange Release Notes bleiben scrollbar und dürfen die Bedienung nicht blockieren.

### APK Download

* Download des APK-Assets der neuesten GitHub Release-Version.
* Das passende Asset wird anhand eines eindeutig definierten Dateinamens erkannt.
* Fehlende oder mehrdeutige APK-Assets werden verständlich gemeldet.

### Download-Fortschritt

* Anzeige von Download gestartet.
* Anzeige des Download-Fortschritts.
* Anzeige von Download abgeschlossen.
* Anzeige von Download fehlgeschlagen.

### APK Speicherung

* Speicherung der APK in einem Android-konformen Bereich.
* Der Anwender muss die Datei nicht manuell suchen.

### Installation

* Nach erfolgreichem Download kann der Android Package Installer gestartet werden.
* Die Installation wird weiterhin durch den Anwender bestätigt.
* Die App versucht keine automatische Installation ohne Benutzerinteraktion.

### Fehlerbehandlung

* Fehlende Internetverbindung wird verständlich gemeldet.
* GitHub-Fehler werden verständlich gemeldet.
* Ungültige Release-Daten werden verständlich gemeldet.
* Downloadfehler werden verständlich gemeldet.
* Installationsfehler führen nicht zu einem Absturz.

## Datenschutz und Sicherheit

* Es werden keine personenbezogenen Daten an GitHub übertragen.
* Es werden keine WLAN-Daten, Gerätebefehle oder gespeicherten Konfigurationen übertragen.
* Es wird keine Cloud-, Tracking- oder Analytics-Abhängigkeit ergänzt.
* Die Update-Prüfung darf die Schaltfunktionen der App nicht beeinträchtigen.
* Heruntergeladene APKs werden nur aus dem konfigurierten offiziellen SwitchWerk-GitHub-Repository akzeptiert.

## Architekturhinweise

* Die Update-Prüfung wird außerhalb der Compose-UI gekapselt.
* Der ViewModel-Zustand wird über `StateFlow` bereitgestellt.
* Compose rendert ausschließlich den bereitgestellten Update-Status.
* Zeitpunkt und Ergebnis der letzten erfolgreichen Update-Prüfung werden lokal gespeichert.
* Die Implementierung soll die bestehende Architektur beibehalten:

  * Service Layer
  * Repository Layer
  * ViewModel Layer
  * Compose UI
* Android-spezifisch sind insbesondere zu prüfen:

  * FileProvider
  * Package Installer Intent
  * Scoped Storage
  * aktuelle Android-Sicherheitsanforderungen

## Technische Hinweise

Mögliche Datenquelle:

`https://api.github.com/repos/<organisation>/<repository>/releases/latest`

Beispiel für ein Release Asset:

`https://github.com/<organisation>/<repository>/releases/download/<version>/SwitchWerk.apk`

Der konkrete Repository-Pfad und Asset-Name sollen im Rahmen der Implementierung eindeutig festgelegt werden.

## Nicht im Scope

* Google Play Store Integration
* automatische Hintergrund-Updates
* automatische Installation ohne Benutzerinteraktion
* alternative Update-Quellen
* Beta- oder Testkanäle
* erzwungene Updates
* In-App-Pflicht zur Aktualisierung

## Akzeptanzkriterien

* [ ] Die installierte App-Version wird angezeigt.
* [ ] Die neueste GitHub Release-Version kann abgerufen werden.
* [ ] Pre-Releases werden nicht als reguläres Update angezeigt.
* [ ] Der Versionsvergleich funktioniert zuverlässig.
* [ ] Verfügbare Updates werden angezeigt.
* [ ] Die Release Notes können angezeigt werden.
* [ ] Eine manuelle Update-Prüfung ist möglich.
* [ ] Eine automatische Update-Prüfung findet maximal einmal pro Kalendertag statt.
* [ ] Mehrfache App-Starts am selben Tag verwenden das zwischengespeicherte Ergebnis.
* [ ] Die manuelle Update-Prüfung ignoriert die tägliche Begrenzung.
* [ ] Das passende APK-Asset kann erkannt werden.
* [ ] Die APK kann heruntergeladen werden.
* [ ] Der Download-Fortschritt wird angezeigt.
* [ ] Die APK wird lokal Android-konform gespeichert.
* [ ] Der Android Installationsdialog kann gestartet werden.
* [ ] Netzwerkfehler werden sauber behandelt.
* [ ] Fehlerhafte Release-Daten führen nicht zu App-Abstürzen.
* [ ] Bestehende Schaltfunktionen bleiben unabhängig von der Update-Funktion nutzbar.
* [ ] Es werden keine sensiblen App-, WLAN- oder Gerätedaten übertragen.
* [ ] Relevante Unit- und UI-Tests sind ergänzt.
* [ ] Build und Installation wurden auf dem Ubuntu-Host erfolgreich geprüft.

## Testhinweise

* App ist auf aktueller Version installiert.
* App ist auf älterer Version installiert.
* Kein GitHub Release vorhanden.
* GitHub Release ohne APK-Asset.
* GitHub Release mit mehreren Assets.
* GitHub Release ist als Pre-Release markiert.
* Keine Internetverbindung.
* GitHub API nicht erreichbar.
* Download wird unterbrochen.
* Download erfolgreich.
* Android Installationsdialog startet.
* Anwender bricht Installation ab.
* App-Start mit gecachtem Update-Ergebnis.
* Mehrere App-Starts am selben Kalendertag.
* Manuelle Prüfung über Einstellungen.

