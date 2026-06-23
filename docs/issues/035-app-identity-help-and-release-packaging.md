# 035 App Identity, Help And Release Packaging

## Status

- Status: Offen
- Priorität: P0
- Typ: GUI / Dashboard

## Priorität

P0

## Ziel

Die App soll für Anwender besser erkennbar, erklärbarer und verteilbar werden. Dazu werden App-Icon, optionale Metadaten, Hilfe-Menü, kontextbezogene Hinweise sowie ein Release-Build-Prozess vorbereitet.

## Implementierungsstand

Auf Branch `app-identity-help-release-packaging` umgesetzt:

- Launcher- und Adaptive-Icons für SwitchWerk in den erforderlichen Dichten;
  das launcher-optimierte Schalter-/Blitz-Icon ist vom About-Logo getrennt.
- App-Name `SwitchWerk`, Versionsname `0.1`; Paketname unverändert.
- Hamburger-Menü mit getrennter Hilfe- und About-Ansicht. About zeigt Lizenz-
  und Autorenhinweis sowie das zentrale About-Logo.
- Deutsche und englische Hilfe- und Accessibility-Texte als Android-String-
  Ressourcen. Hilfe verwendet übersichtliche Aufzählungen.
- Kontextbezogene `i`-Hinweise an Dashboard, WLAN-Profilen, Geräten,
  Aktionsdetails, Einstellungen sowie Backup. Sie erklären jeweils das ganze
  sichtbare Dialogfeld; bei System und Backup sind die Hinweise tabübergreifend
  ausgerichtet.
- Release-Signing über die lokale, ignorierte `keystore.properties`
  vorbereitet. Keystore und Passwörter werden nicht versioniert.
- Lokale Befehle, Keystore-Erzeugung und die Verteilungsentscheidung sind in
  `docs/release-build.md` dokumentiert.

## Verteilungsentscheidung

Für die erste Anwenderverteilung wird ein GitHub Release mit signierter APK
empfohlen. Ein Play-Store-Upload ist bewusst späterer, separater Aufwand.

## Verifizierung und offene Schritte

- Die Debug-Prüfungen `lintDebug`, `testDebugUnitTest` und
  `clean assembleDebug` wurden im Host-Verlauf erfolgreich ausgeführt.
- Eine vollständige erneute manuelle Host-Prüfung der finalen Icon-/About-
  Assets sowie `installDebug` ist vor Veröffentlichung nochmals durchzuführen.
- Ein realer Release-Keystore wurde bewusst noch nicht erzeugt. Vor einem
  Release sind `keystore.properties`, `./gradlew assembleRelease` und die
  signierte APK lokal zu prüfen.
- Kein Play-Store-Upload, kein Paketnamenwechsel und keine sensiblen Dateien im
  Repository.

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
