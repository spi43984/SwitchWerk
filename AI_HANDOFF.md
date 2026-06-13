# AI Handoff

Stand: 13. Juni 2026

## Aktueller Auftrag

Issue 010 "HTTP API Call Service" wurde gemäß Phase 1 des Projekt-Workflows
implementiert.

- GitHub-Issue: #19
- URL: https://github.com/spi43984/SwitchWerk/issues/19
- Status: offen
- Branch: `http-api-call-service`
- Ausgangspunkt: `main` bei Commit `6475635`
- Implementierungscommit: `b1043a7 feat: implement http api call service`
- Dokumentationscommit: `aa4e4aa docs: add reusable ai session prompt`
- Pull Request: #20
- PR-URL: https://github.com/spi43984/SwitchWerk/pull/20
- PR-Status: Draft, offen

Phase 2 wurde vom Benutzer ausdrücklich angefordert. Der Implementierungscommit
und der Dokumentationscommit wurden erstellt, der Branch wurde gepusht und der
Draft-PR wurde geöffnet. Das Issue bleibt bis zum Merge offen.

## Implementierter Umfang

- suspendierendes `HttpApiCallService`-Interface
- GET-Aufrufe
- POST-Aufrufe mit optionalem Body und konfigurierbarem Content-Type
- OkHttp 5.4.0
- standardmäßiger Call-Timeout von 10 Sekunden
- pro Aufruf konfigurierbarer Timeout
- Coroutine-Abbruch bricht den OkHttp-Call ab
- optionale Bindung an ein `android.net.Network`
- DNS-Auflösung und Socket-Erzeugung über das ausgewählte Android-Netzwerk
- strukturierte erfolgreiche Responses mit Statuscode, Headern und Body
- strukturierte HTTP-Fehler mit vollständiger Response
- strukturierte Timeout-, Netzwerk- und Validierungsfehler
- keine automatische Wiederholung fehlgeschlagener Requests
- keine automatische Weiterleitung von Requests
- Koin-Registrierung für `OkHttpClient` und `HttpApiCallService`
- `INTERNET`-Berechtigung
- Freigabe von Klartext-HTTP für lokale Geräteendpunkte
- JVM-Tests mit MockWebServer für GET, POST, HTTP-Fehler, Redirect,
  Timeout, Verbindungsfehler und ungültige URL

Nicht Bestandteil dieses Issues:

- Geräteaktionen
- Auswahl eines WLAN-Profils
- WLAN-Fallback
- UI- oder ViewModel-Anbindung
- Import/Export

Diese Punkte bleiben insbesondere Issue 011 und späteren Issues vorbehalten.

## Architekturentscheidung

Der Dienst liegt wie der WLAN-Verbindungsdienst unter `data/network`.
`HttpApiCallService` kapselt die öffentliche Coroutine-API,
`OkHttpApiCallService` die konkrete OkHttp-Implementierung.

Ein von `WifiConnectionResult.Success` geliefertes `Network` kann direkt an
GET oder POST übergeben werden. Der OkHttp-Client verwendet dann sowohl
`Network.socketFactory` als auch `Network.getAllByName(...)`, damit Verbindung
und DNS-Auflösung über das angeforderte Geräte-WLAN erfolgen.

2xx-Antworten werden als `Success` geliefert. Andere HTTP-Statuscodes werden
nicht als Transportfehler behandelt, sondern als `HttpError` einschließlich
Statuscode, Headern und Body. Es werden keine Requests, URLs, Header oder Bodies
geloggt.

Automatische Retries und Redirects sind deaktiviert, damit spätere
Schaltaktionen nicht still wiederholt oder an ein anderes Ziel weitergeleitet
werden.

## Geänderte Dateien

```text
AGENTS.md
AI_HANDOFF.md
AI_SESSION_PROMPT.md
ai-context.md
app/build.gradle.kts
app/src/main/AndroidManifest.xml
app/src/main/java/de/piecha/switchwerk/data/network/HttpApiCallResult.kt
app/src/main/java/de/piecha/switchwerk/data/network/HttpApiCallService.kt
app/src/main/java/de/piecha/switchwerk/data/network/HttpApiResponse.kt
app/src/main/java/de/piecha/switchwerk/data/network/OkHttpApiCallService.kt
app/src/main/java/de/piecha/switchwerk/di/AppModule.kt
app/src/test/java/de/piecha/switchwerk/data/network/OkHttpApiCallServiceTest.kt
gradle/libs.versions.toml
```

Die frühere wiederverwendbare Vorlage "Prompt für den nächsten Chat" wurde aus
dem Handoff zu Issue 009 in `AI_SESSION_PROMPT.md` im Repository-Root
überführt. `AGENTS.md` und `ai-context.md` schreiben ihre Lektüre für neue
Sessions vor. Die Vorlage wurde an den aktuellen zweiphasigen Workflow
angepasst; insbesondere enthält sie keine fest verdrahtete Issue-Nummer und
keine automatische Veröffentlichung.

Die lokale Issue-Datei
`docs/issues/010-http-api-call-service.md` bleibt bis zum Abschluss der
Veröffentlichungsphase unverändert und nicht abgehakt.

## Verifikation

Erfolgreich:

```bash
git diff --check
```

Der Benutzer führte auf dem Host aus:

```bash
./gradlew testDebugUnitTest
```

Dabei kompilierten Anwendung und Tests. Von neun Tests schlug zunächst der
Timeout-Test fehl:

```text
expected: Timeout
actual: NetworkError(cause=java.io.InterruptedIOException: timeout)
```

Ursache war, dass ein Timeout beim Lesen des Response-Bodys im
`onResponse`-Zweig als allgemeiner Netzwerkfehler eingeordnet wurde. Die
Fehlerklassifizierung wurde danach zentralisiert; `InterruptedIOException`
wird nun sowohl in `onFailure` als auch beim Lesen der Response als `Timeout`
geliefert.

Der Benutzer führte den korrigierten Stand anschließend erneut auf dem Host aus:

```bash
./gradlew testDebugUnitTest
```

Ergebnis:

```text
BUILD SUCCESSFUL in 5s
26 actionable tasks: 7 executed, 19 up-to-date
```

Damit sind alle neun JVM-Tests erfolgreich. Der vollständige Debug-Build und
die Installation wurden anschließend ebenfalls auf dem Host ausgeführt.

Der Benutzer bestätigte folgende Befehle als erfolgreich:

```bash
./gradlew clean assembleDebug
./gradlew installDebug
```

Damit sind Unit-Tests, Debug-Build und Installation für Issue 010 erfolgreich
bestätigt.

Der Benutzer bestätigte anschließend auch die manuellen Regressionstests der
installierten App als erfolgreich. Eine echte Shelly-Schaltaktion war dabei
nicht Bestandteil der Prüfung, weil die Anbindung von Dashboard, Geräteaktion,
WLAN-Verbindung und HTTP-Dienst erst mit Issue 011 erfolgt.

Die Host-Ausgabe enthielt außerdem bereits bestehende Warnungen:

- kein Room-Schemaexport konfiguriert
- `EncryptedSharedPreferences` und `MasterKey` sind veraltet
- `local.properties` enthält zusätzlich einen nicht existierenden SDK-Pfad,
  obwohl Gradle auf dem Host ein SDK finden und kompilieren konnte

## Auf dem Host auszuführen

Im lokalen Repository auf dem Branch `http-api-call-service`:

```bash
./gradlew testDebugUnitTest
```

Manuelle Prüfung mit einem lokalen HTTP-Endpunkt oder Shelly:

1. GET liefert Statuscode, Header und Body.
2. POST überträgt den erwarteten Body und Content-Type.
3. Ein 4xx/5xx-Status liefert `HttpError` mit Response.
4. Ein nicht erreichbares Ziel liefert `NetworkError`.
5. Ein zu kurzer Timeout liefert `Timeout`.
6. Ein Request mit dem von `WifiConnectionService` gelieferten `Network`
   erreicht das Gerät über dessen WLAN.

## Akzeptanzkriterien

- GET möglich: implementiert, Test vorhanden
- POST möglich: implementiert, Test vorhanden
- Response verfügbar: Statuscode, Header und Body implementiert
- Fehler verfügbar: HTTP-, Timeout-, Netzwerk- und Validierungsfehler
  implementiert

Die Kriterien sind im Code abgedeckt. Ihre Build- und Laufzeitbestätigung steht
vollständig vor:

- alle neun JVM-Tests erfolgreich
- Debug-Build erfolgreich
- Installation erfolgreich
- manuelle Regressionstests der App erfolgreich

## Nächster Schritt

Pull Request #20 prüfen und nach ausdrücklicher Merge-Freigabe nach `main`
mergen. Erst danach die lokale Issue-Datei abhaken, Issue #19 schließen und den
Feature-Branch lokal und remote löschen.
