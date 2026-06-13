# AI Handoff

Stand: 13. Juni 2026

## Zweck

Diese Datei dokumentiert den in der vorherigen AI-Sitzung bearbeiteten Stand von
SwitchWerk. Maßgeblich bleibt immer der aktuelle Git-Stand sowie die Vorgaben in
`AGENTS.md` und `ai-context.md`.

Hinweis: `ai-context.md` liegt im Repository-Root. Eine Datei
`docs/ai-context.md` existiert derzeit nicht.

## Umgesetzte Arbeit

Am 12. Juni 2026 wurde Issue 009 "WiFi Connection Service" implementiert.

Ausgangspunkt war GitHub-Issue #17:

- Titel: `WiFi Connection Service`
- URL: https://github.com/spi43984/SwitchWerk/issues/17
- Status: geschlossen
- Geschlossen am: 12. Juni 2026

Die Implementierung erfolgte auf dem Branch:

```text
wifi-connection-service
```

Sie wurde über Pull Request #18 nach `main` gemergt:

- PR: https://github.com/spi43984/SwitchWerk/pull/18
- Merge-Commit: `43986d2793dd5b27a9f00503d798034b3f855da4`
- Commit-Titel: `feat: implement wifi connection service (#18)`
- Gemergt am: 12. Juni 2026

Der Feature-Branch ist lokal und remote nicht mehr vorhanden.

## Implementierter Umfang

Issue 009 umfasst ausschließlich den Aufbau einer Verbindung zu einem lokalen
Geräte-WLAN:

- `WifiConnectionService` als suspendierendes Service-Interface
- `AndroidWifiConnectionService` als Android-Implementierung
- `WifiConnectionResult` für strukturierte Ergebnisse
- WLAN-Anforderung mit `WifiNetworkSpecifier`
- Aufruf über `ConnectivityManager.requestNetwork(...)`
- Behandlung von `NetworkCallback.onAvailable(...)`
- Behandlung von `NetworkCallback.onUnavailable()`
- Behandlung von `NetworkCallback.onLost(...)`
- konfigurierbarer Timeout, standardmäßig 15 Sekunden
- Abbruchunterstützung über Coroutines
- explizites `disconnect()` und Freigabe aktiver Callbacks
- Koin-Registrierung des Service und des `ConnectivityManager`
- Manifest-Berechtigung `CHANGE_NETWORK_STATE`

Nicht Bestandteil der Implementierung:

- HTTP- oder API-Aufrufe
- Shelly-Steuerbefehle
- Auswahl eines Geräte-WLAN-Profils
- WLAN-Fallback zwischen mehreren Geräteverbindungen
- UI- oder ViewModel-Anbindung

## Architekturentscheidung

Die Implementierung liegt unter `data/network`, weil sie Android-spezifischen
Netzwerkzugriff kapselt. UI und Composables enthalten keine Netzwerklogik.

`WifiConnectionService.connect(...)` ist eine Coroutine-API. Der interne
Android-Callback wird mit `suspendCancellableCoroutine` adaptiert. Ein
erfolgreiches Ergebnis enthält das konkrete `android.net.Network`, damit ein
späterer HTTP-Dienst Verbindungen gezielt über dieses WLAN öffnen kann.

Der aktive `NetworkCallback` bleibt nach `onAvailable(...)` registriert. Dadurch
bleibt die angeforderte WLAN-Verbindung für nachfolgende Operationen erhalten.
Sie wird durch `disconnect()`, Coroutine-Abbruch, Verbindungsverlust oder Fehler
freigegeben.

Es wird immer nur eine aktive WLAN-Anforderung verwaltet. Ein neuer
Verbindungsversuch beendet zunächst einen vorhandenen Request.

Unterstützt werden:

- offene WLANs bei leerem oder fehlendem Passwort
- WPA2-PSK über `setWpa2Passphrase(...)`
- Android 10 / API 29 und neuer

Es werden keine SSIDs, Passwörter oder anderen sensiblen Netzwerkdaten geloggt.

## Geänderte Dateien

Durch Issue 009 wurden folgende Dateien geändert oder angelegt:

```text
app/src/main/AndroidManifest.xml
app/src/main/java/de/piecha/switchwerk/data/network/AndroidWifiConnectionService.kt
app/src/main/java/de/piecha/switchwerk/data/network/WifiConnectionResult.kt
app/src/main/java/de/piecha/switchwerk/data/network/WifiConnectionService.kt
app/src/main/java/de/piecha/switchwerk/di/AppModule.kt
```

Zusätzlich wurde nach dem Merge die lokale Issue-Datei abgehakt:

```text
docs/issues/009-wifi-connection-service.md
```

## Build und Verifikation

In der Implementierungssitzung wurde nach Einrichtung der fehlenden lokalen
Android-SDK folgender Build erfolgreich ausgeführt:

```bash
./gradlew clean assembleDebug
```

Ergebnis:

```text
BUILD SUCCESSFUL in 38s
38 actionable tasks: 38 executed
```

Das Debug-APK wurde unter folgendem Pfad erzeugt:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Der Build meldete bereits vorher vorhandene Warnungen:

- kein Room-Schemaexport konfiguriert
- `EncryptedSharedPreferences` und `MasterKey` sind in der verwendeten
  AndroidX-Version als veraltet markiert

Diese Warnungen wurden nicht im Rahmen von Issue 009 bearbeitet.

## Aktueller Stand

Zum Zeitpunkt dieser Übergabe:

- aktiver Branch: `main`
- `main` entspricht `origin/main`
- letzter Commit: `e3bd39abbd7481f6f181d7d2bb2ad9eafce1e3d7`
- Issue 009 ist lokal abgehakt
- GitHub-Issue #17 ist geschlossen
- Pull Request #18 ist gemergt
- die Implementierung von Issue 009 ist vollständig in `main`
- vor Erstellung dieser Datei war der Arbeitsbaum sauber
- diese Datei ist die einzige neue Änderung dieser Übergabe

## Offene Probleme und Annahmen

1. Die Verbindung wurde gebaut, aber in der AI-Umgebung nicht mit einem echten
   Android-Gerät und einem Shelly-Geräte-WLAN praktisch verifiziert.
2. WPA3 und andere Security-Typen sind nicht implementiert. Der aktuelle Scope
   umfasst offene und WPA2-PSK-Netze.
3. Android-Versionen unter API 29 liefern
   `WifiConnectionResult.UnsupportedAndroidVersion`.
4. Die App-Installation per `adb install -r` schlug beim Benutzer fehl, weil die
   bereits installierte App mit einem anderen Schlüssel signiert war. Ein Update
   ist nur mit demselben Keystore möglich; eine Deinstallation löscht lokale
   App-Daten.
5. `ai-context.md` führt Issue 009 noch unter "Offen", obwohl das Issue
   implementiert, gemergt und geschlossen ist. Diese Dokumentation sollte
   korrigiert werden.
6. Der GitHub-PR-Text nennt einen damals fehlenden SDK-Pfad. Danach wurde die
   Android-SDK in der AI-Umgebung eingerichtet und der Build erfolgreich
   ausgeführt. Der erfolgreiche Build oben ist der spätere und maßgebliche
   Stand.

## Nächster fachlicher Schritt

Der nächste geplante Funktionsbaustein ist Issue 010 "HTTP API Call Service":

```text
docs/issues/010-http-api-call-service.md
```

Vorgesehener Scope:

- OkHttp
- GET
- POST
- Timeout
- strukturierte Response
- strukturierte Fehlerbehandlung

Issue 010 soll das von `WifiConnectionResult.Success` gelieferte
`android.net.Network` nutzen können, ohne die WLAN-Verbindungslogik zu
duplizieren. Geräteaktionen und WLAN-Fallback bleiben weiterhin außerhalb des
Scopes und gehören zu Issue 011.

Vor der Umsetzung muss gemäß `AGENTS.md` geprüft werden, ob bereits ein
zugehöriges GitHub-Issue existiert. Danach gilt der vollständige Workflow:

```bash
git status
git switch main
git pull
gh issue create \
  --title "HTTP API Call Service" \
  --body-file docs/issues/010-http-api-call-service.md
git switch -c http-api-call-service
```

Falls das GitHub-Issue oder der Branch bereits existiert, darf kein Duplikat
angelegt werden. In diesem Fall zuerst den bestehenden Stand ermitteln und den
vorhandenen Branch verwenden.

## Arbeitsauftrag für die nächste AI-Sitzung

1. Alle in `ai-context.md` vorgeschriebenen Dateien vollständig lesen.
2. Git-Status, Branch, letzte Commits und vorhandene Änderungen prüfen.
3. Diese Übergabe gegen den tatsächlichen Repository-Zustand verifizieren.
4. Die veraltete Statusangabe zu Issue 009 in `ai-context.md` erkennen und bei
   passender Gelegenheit korrigieren.
5. Ohne ausdrücklichen Auftrag keine neue Implementierung beginnen.
6. Bei einem Auftrag für Issue 010 dessen Datei und GitHub-Status prüfen und den
   Branch-/Issue-Workflow aus `AGENTS.md` einhalten.
7. Keine HTTP-, Geräteaktions- oder Fallback-Logik nachträglich in Issue 009
   vermischen.

## Prompt für den nächsten Chat

```text
Implementiere das nächste offene Issue.

WICHTIG:
Bevor du Änderungen vornimmst, lies und berücksichtige folgende Dateien:

* ai-context.md
* AGENTS.md
* GITHUB_WORKFLOW.md
* ARCHITECTURE.md
* CODE_STYLE.md
* TESTING.md
* SECURITY.md
* README.md
* AI_HANDOFF.md

Zusätzlich lesen:

* docs/issues/overview.txt
* das nächste offene Issue in docs/issues/...

Bei Android-/ Build-/Dependency-Fragen zusätzlich prüfen:

* settings.gradle.kts
* build.gradle.kts
* app/build.gradle.kts
* gradle/libs.versions.toml
* gradle.properties

GitHub-Issue:

* Erstelle das nächste Issue auf github

Führe zuerst den Workflow aus:

1. git status
2. git switch main
3. git pull
4. gh issue view 17
5. git switch -c BRANCH-NAME

Falls der Branch bereits existiert:

git switch BRANCH-NAME

Nutze das Repository ShellyPulse nur als funktionale Referenz für den Ablauf:

* WifiNetworkSpecifier
* ConnectivityManager.requestNetwork(...)
* NetworkCallback
* onAvailable
* onUnavailable
* Timeout
* Fehlerbehandlung

Übernimm KEINEN Code aus ShellyPulse.

Halte dich an die bestehende SwitchWerk-Architektur:

* Kotlin
* Jetpack Compose
* MVVM
* Koin
* Coroutines
* Room
* Keine Netzwerklogik in UI/Composables
* Keine sensiblen Daten loggen

Implementiere ausschließlich das nächste Issue.

Prüfe den Scope des nächsten Issues und zeige ihn an.

Vor jeder Änderung:

* bestehende Architektur analysieren
* vorhandene Packages prüfen
* bestehende Patterns übernehmen

Nach der Implementierung zeige mir, was ich lokal ausführen muss, z. B.:

1. ./gradlew clean assembleDebug

Danach ausgeben:

* Architekturentscheidung
* Liste aller geänderten Dateien
* vollständigen Diff
* Build-Ergebnis
* offene Probleme oder Annahmen
* Bewertung, ob alle Akzeptanzkriterien des Issues erfüllt sind

WICHTIG:

* NICHT committen
* NICHT pushen
* KEINEN Pull Request erstellen
* Nur implementieren und Ergebnis zeigen, bauen und installieren mache ich lokal auf dem Host
* aktualisiere ai-context.md und erstelle eine neue AI_HANDOFF.md im Hauptverzeichnis
```
