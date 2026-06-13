# AI Handoff

Stand: 13. Juni 2026

## Abgeschlossene Arbeit

Issue 010 "HTTP API Call Service" ist vollständig implementiert, geprüft,
veröffentlicht und nach `main` gemergt.

- GitHub-Issue: #19
- Issue-URL: https://github.com/spi43984/SwitchWerk/issues/19
- Pull Request: #20
- PR-URL: https://github.com/spi43984/SwitchWerk/pull/20
- Merge-Commit: `557ae168d96eedd01b1a74f5b084d82312781294`
- Merge-Methode: Squash
- Commit-Titel: `feat: implement http api call service (#20)`

Der Feature-Branch `http-api-call-service` wurde lokal und remote gelöscht.

## Implementierter Umfang

- suspendierendes `HttpApiCallService`-Interface
- GET- und POST-Aufrufe über OkHttp 5.4.0
- optionaler POST-Body und konfigurierbarer Content-Type
- standardmäßiger und pro Aufruf konfigurierbarer Timeout
- Coroutine-Abbruch beendet den OkHttp-Call
- optionale Bindung an ein `android.net.Network`
- DNS und Socket-Erzeugung über das ausgewählte Android-Netzwerk
- strukturierte erfolgreiche Responses mit Statuscode, Headern und Body
- strukturierte HTTP-, Timeout-, Netzwerk- und Validierungsfehler
- keine automatischen Retries oder Redirects
- Koin-Registrierung
- `INTERNET`-Berechtigung
- Klartext-HTTP für lokale Geräteendpunkte
- MockWebServer-Tests

Zusätzlich wurde `AI_SESSION_PROMPT.md` im Repository-Root eingeführt.
`AGENTS.md` und `ai-context.md` schreiben die Lektüre dieser Datei und des
Handoffs für neue Sessions vor.

## Verifikation

Vom Benutzer auf dem Host erfolgreich bestätigt:

```bash
./gradlew testDebugUnitTest
./gradlew clean assembleDebug
./gradlew installDebug
```

Ergebnis:

- alle neun JVM-Tests erfolgreich
- Debug-Build erfolgreich
- Installation erfolgreich
- manuelle Regressionstests der installierten App erfolgreich
- `git diff --check` erfolgreich

Eine echte Shelly-Schaltaktion ist weiterhin nicht möglich. Die Verbindung von
Dashboard, Geräteaktion, WLAN-Verbindungsdienst und HTTP-Dienst gehört zu Issue
011.

## Bekannte Warnungen

- Room-Schemaexport ist nicht konfiguriert.
- `EncryptedSharedPreferences` und `MasterKey` sind veraltet.

Diese Punkte waren nicht Bestandteil von Issue 010.

## Entwicklungsumgebung

Die Docker-Entwicklungsumgebung speichert folgende Verzeichnisse persistent auf
dem Host:

- `/home/ubuntu/.gradle`
- `/opt/android-sdk`
- `/home/ubuntu/.android`

Verifiziert wurden:

- Gradle 9.4.1 und Maven-Abhängigkeiten im persistenten Gradle-Cache
- Android-Plattformen 36 und 36.1
- Android Build Tools 36.0.0
- persistente Platform Tools mit ADB 37.0.0
- persistenter Debug-Keystore
- korrektes `local.properties` mit `sdk.dir=/opt/android-sdk`

Der Offline-Test war erfolgreich:

```bash
./gradlew --offline testDebugUnitTest
```

Damit sind für den aktuellen Build keine erneuten Downloads erforderlich.
ADB-Geräteschlüssel werden bei Bedarf erzeugt und anschließend ebenfalls über
`/home/ubuntu/.android` persistent gespeichert.

## Nächster fachlicher Schritt

Das nächste offene Issue ist:

```text
docs/issues/011-device-action-with-wifi-fallback.md
```

Issue 011 verbindet die bestehenden Bausteine:

- gespeichertes Gerät und API-Konfiguration
- Abgleich des aktuellen WLANs mit den zugeordneten WLAN-Profilen
- pragmatische Zuordnung über übereinstimmende SSID
- direkter, an das konkrete WLAN-`Network` gebundener HTTP-Aufruf nur in einem
  zugeordneten aktuellen WLAN
- andernfalls WLAN-Fallback in gespeicherter Reihenfolge
- WLAN-Verbindungsdienst
- HTTP-Aufruf über das gelieferte `Network`
- nächstes WLAN nach API-Fehler nur bei eindeutigem DNS- oder
  Verbindungsfehler vor Ausführung der Aktion
- kein erneuter Schaltversuch nach API-Timeout
- strukturierte Status- und Fehleranzeige

Vor Beginn einer neuen Implementierung müssen `AGENTS.md`, `ai-context.md`,
`AI_SESSION_PROMPT.md`, diese Datei sowie die dort vorgeschriebenen
Projektdokumente vollständig gelesen werden.

Zusätzlich wurde das zukünftige Issue 019 zur konfigurierbaren Sortierung der
WLAN-Verwaltungsliste und zur festen alphabetischen Sortierung der
Geräteverwaltungsliste dokumentiert. Die manuelle Dashboard-Reihenfolge aus
Issue 014 bleibt davon unberührt.
