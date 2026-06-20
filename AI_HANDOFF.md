# AI Handoff

Stand: 20. Juni 2026

## Aktuelle Arbeit

Issue 027 "WiFi Timeout Analysis And Stabilization" ist auf dem Branch
`wifi-timeout-stabilization` implementiert, aber noch nicht committet oder auf
dem Ubuntu-Host geprüft.

- GitHub-Issue: #57
- WLAN-Erfolg erst nach WiFi-Capability und gültiger Link-IP
- robustes Handling verspäteter und mehrfacher NetworkCallbacks
- WLAN-Timeout auf 30 Sekunden erweitert
- Gerätemitschnitt nach Force-Stop zeigte veraltete Scan-Ergebnisse und keinen
  `onAvailable`-Callback vor dem bisherigen 15-Sekunden-Security-Fallback
- Timeout ohne `onAvailable` wird neutral als NetworkRequest-Timeout klassifiziert
- WPA2/WPA3 teilen weiterhin das 30-Sekunden-Budget und können auch bei einem
  importierten, unpassenden bevorzugten Sicherheitstyp aufeinander zurückfallen
- Gerätemitschnitt nach Import bestätigte den notwendigen WPA3-zu-WPA2-Fallback
- vorhandene Android-Scan-Ergebnisse dienen nur als optionaler Hinweis zur
  Reihenfolge der Sicherheitstypen
- freigegebene Scope-Erweiterung: importierte Sicherheitstypen werden mit einem
  lokalen, nicht exportierten Prüfstatus als ungeprüft markiert
- vor der ersten Aktion eines importierten Profils wird bei Bedarf höchstens
  acht Sekunden auf einen Vordergrund-Scan gewartet; dafür wird beim Import
  `ACCESS_FINE_LOCATION` angefragt
- erfolgreicher Verbindungsaufbau oder Fallback speichert den tatsächlichen Typ
  als lokal geprüft; spätere Aktionen führen keinen erneuten Vorab-Scan aus
- Room-Datenbankversion 5 mit Migration 4→5; bestehende lokale Profile gelten
  nach der Migration als geprüft
- deaktiviertes WLAN wird vor dem NetworkRequest erkannt und verständlich gemeldet
- netzgebundene DNS-Prüfung vor dem HTTP/RPC-Aufruf
- vollständige Diagnoseereignisse für WLAN, DHCP/IP, DNS, HTTP und Timeouts
- sichtbare Zeitdifferenz zwischen Diagnoseereignissen
- kein automatischer HTTP-Retry bei potenziell nicht-idempotenten Schaltaktionen

Container-Prüfungen:

```text
git diff --check
./gradlew testDebugUnitTest
```

Beide Prüfungen waren erfolgreich. Der Benutzer hatte
`./gradlew clean assembleDebug` und `./gradlew installDebug` auf dem Host für
einen vorherigen Zwischenstand erfolgreich ausgeführt. Nach der Ergänzung der
WLAN-deaktiviert-Erkennung und der Import-Scan-Erweiterung wurden Host-Build,
Installation, Room-Migration sowie die manuellen Import-/WLAN-Aktionstests für
den aktuellen Stand ebenfalls erfolgreich bestätigt.

## Zuletzt abgeschlossene Arbeit

Issue 023 "Settings Display And Action Details" ist implementiert, geprüft,
veröffentlicht und nach `main` gemergt.

- GitHub-Issue: #54
- Pull Request: #55
- Merge-Commit: `09b55fcc7df99c823a5e88c7b247b23ff0308c64`

## Umgesetzter Scope Issue 023

- Persistente Auswahl Systemvorgabe/Hell/Dunkel
- Persistente Detailanzeige mit Höhe 20/30/40 Prozent
- Sortierbares, scrollbares In-Memory-Aktionsprotokoll
- Zeitgestempelte WLAN-/HTTP-Diagnosen ohne sensible Request-Daten
- Geräteadresse, HTTP-Methode, Statuscode sowie DNS-/IP-Fehleranzeige
- Optische Aktionstrenner und Löschfunktion
- Export und Import der UI-Einstellungen mit rückwärtskompatiblem Schema 2

## Bestätigte Prüfungen

```text
git diff --check
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew clean assembleDebug
./gradlew installDebug
GitHub Actions: Android Build #176
```

Host-Build, Installation und manuelle Tests wurden vom Benutzer als erfolgreich
gemeldet. GitHub Actions Lauf #176 war erfolgreich.

## Wichtige Hinweise für die nächste Session

- Wiederverwendbare Startvorlage: `AI_SESSION_PROMPT.md`
- Die Statusdateien wurden nach Issue 023 aktualisiert:
  - `docs/issues/023-settings-display-and-action-details.md`
  - `docs/issues/overview.txt`
  - `ai-context.md`
  - `AI_HANDOFF.md`
- Nächstes fachliches Issue ist 026 "Settings UI Rework".

## Nächste geplante Themen

```text
026 Settings UI Rework
027 WiFi Timeout Analysis And Stabilization
020 Device Assigned WiFi Order
025 Dashboard Widget Layout
028 Theme Mode Setting
029 Language Setting
030 WiFi Profile Deletion Safety
033 Android-managed WiFi networks
019 Configurable WiFi List Sorting
021 HTTP/HTTPS Device Actions
022 Request Body And Content-Type Support
031 Import Enforces Unique WiFi Profile Names
032 Room Schema And Migration Test Coverage
```
