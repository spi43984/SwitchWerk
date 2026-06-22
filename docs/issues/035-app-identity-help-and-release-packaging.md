# 035 App Identity, Help And Release Packaging

## Status

- Status: Offen
- Priorität: P1
- Typ: GUI / Dashboard

## Priorität

P2

## Ziel

Die App soll für Anwender besser erkennbar, erklärbarer und verteilbar werden. Dazu werden App-Icon, optionale Metadaten, Hilfe-Menü, kontextbezogene Hinweise sowie ein Release-Build-Prozess vorbereitet.

## Umfang

### 1. Neues App-Icon

- Neues App-Icon für SwitchWerk einführen.
- Adaptive Icon für Android berücksichtigen.
- Launcher-Icon in allen notwendigen Dichten bereitstellen.
- Prüfen, ob zusätzlich ein monochromes Icon sinnvoll ist.

### 2. App-Metadaten

Prüfen und bei Bedarf definieren:

- App-Name
- Kurzbeschreibung
- Langbeschreibung
- Versionsname
- Paketname unverändert lassen
- ggf. Copyright-/Lizenzhinweise
- ggf. Link zu Projekt, Hilfe oder GitHub-Repository
- ggf. Download-Link
- About-Menü

### 3. Hilfe-Menü

Ein Hilfe-Menü in die App integrieren.

Hilfetexte erstellen auf Grundlage der bisherigen Issues und umgesetzter Funktionen. Hilfetexte mehrsprachig erstellen.
Hilfetexte zur Prüfung vorlegen.

Mögliche Inhalte:

- Erklärung des App-Zwecks
- WLAN-Wechsel zu Geräte-AP
- Geräte schalten
- Hinweis auf fehlendes Internet während Geräte-AP-Verbindung
- Fehlerhilfe bei Timeout, WLAN nicht gefunden, HTTP-Fehler
- Versionsinformation
- Erklärung der notwendigen Berechtigungen und wofür sie verwendet werden
- kurze Erkläreung zu Datenschutz, dass keine personenbezogenen Daten gespeichert oder weitergegeben werden
- kurze Erklärung, was in Log geschrieben wird

### 4. Kontextbezogene Tipps

Bei einzelnen Funktionen ein kleines `i`-Icon einführen.

Beispiele:

- WLAN-Profil
- Geräteadresse/IP
- HTTP/RPC-Befehl
- Aktionsdetails
- Verbindungsmodus
- Passwortspeicherung

Beim Antippen soll ein kurzer erklärender Hinweis angezeigt werden, z. B. Dialog, Bottom Sheet oder Tooltip-ähnliche Darstellung.

### 5. Release-Build statt Debug-Build

Release-Build-Prozess vorbereiten.

Anforderungen:

- Release-APK oder AAB bauen können.
- App mit stabilem Entwicklerschlüssel signieren.
- Debug-Signing nicht für Anwender-Verteilung verwenden.
- Keystore und Passwörter nicht ins Repository einchecken.
- Gradle-Konfiguration für Release-Signing vorbereiten.
- GitHub Actions prüfen: Debug-Build beibehalten, Release-Build separat ergänzen.

### 6. Verteilungsentscheidung

Entscheidung vorbereiten:

Option A: GitHub Release mit APK

- einfacher Start
- kein Play-Store-Konto nötig
- Anwender müssen Installation aus unbekannten Quellen erlauben

Option B: Google Play Store

- professioneller für Anwender
- automatische Updates
- zusätzlicher Aufwand für Store-Eintrag, Datenschutzangaben, Signierung, Review

Ergebnis des Issues soll eine Empfehlung enthalten, ob zunächst GitHub Releases oder direkt Google Play genutzt werden.

## Akzeptanzkriterien

- Neues App-Icon ist integriert und erscheint im Launcher.
- App-Name und relevante Metadaten sind geprüft bzw. angepasst.
- Hilfe-Menü ist über die App erreichbar.
- Erste `i`-Hinweise sind an zentralen Funktionen vorhanden.
- Release-Build kann lokal erzeugt werden.
- Release-Signing ist vorbereitet, ohne sensible Daten ins Repository einzuchecken.
- Entscheidung GitHub Release vs. Google Play ist dokumentiert.
- Build läuft erfolgreich mit:

  - `./gradlew clean assembleDebug`
  - `./gradlew installDebug`

Zusätzlich ist ein Release-Build-Befehl dokumentiert:

  - `./gradlew assembleRelease`

oder

  - `./gradlew bundleRelease`

## Nicht-Ziele

- Keine vollständige Neugestaltung der App-Oberfläche.
- Keine inhaltliche Komplettdokumentation aller Fehlerfälle.
- Kein automatischer Play-Store-Upload.
- Keine Änderung des Paketnamens, solange keine zwingende Notwendigkeit besteht.

## Hinweise für Umsetzung

- Bestehende Architektur beibehalten.
- Hilfe- und Tipptexte möglichst zentral pflegen.
- UI-Regel beachten: Bei Sicherheitsabfragen steht die sichere Abbruchaktion rechts.
- Release-Signing sicher dokumentieren, aber keine geheimen Daten speichern.
