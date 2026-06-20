# AI Handoff

Stand: 20. Juni 2026

## Aktuelle Arbeit

Issue #59 / lokales Issue 026 "Settings UI Rework" ist auf dem Feature-Branch
`settings-ui-rework` lokal implementiert und noch nicht veröffentlicht.

- Dashboard-Navigation über Hamburger-Menü mit Einstellungen und Hilfe
- Settings-Bereiche WLAN-Profile, Geräte, System und Backup als Tabs
- wiederverwendbare Hilfeansicht mit Version und GitHub-Link
- gemeinsame Compose-Komponenten für Dialoge, Dialogbuttons und Aktionen
- WLAN-, Geräte- und Import-/Export-Dialoge auf das gemeinsame Layout umgestellt
- Import und Export im Backup-Bereich optisch getrennt
- Hamburger-Menü als schmales, bildschirmhohes Panel innerhalb des Dashboards
  am rechten Rand, mit rechtsbündigen Einträgen
- Menüfläche folgt den Dashboard-Innenkanten und Card-Rundungen; das X ersetzt
  das Hamburger-Icon positionsgleich; nur die Menüfläche reicht rechts bündig
  bis an den Bildschirmrand
- Settings-Listen und Settings-Gruppen ohne umschließende Card-Rahmen
- System optisch in Darstellung, Aktionsdetails und Hilfe getrennt
- Sortierung der Aktionsdetails wird zusammen mit den Aktionsdetails deaktiviert
- aktiver Settings-Tab bleibt beim Wechsel zur Hilfe erhalten
- appweiter Theme-Hintergrund deckt auch den äußeren Screenbereich ab
- erster Settings-Tab ohne zusätzlichen linken Standardabstand
- keine Änderung an ViewModels, Repositories oder fachlicher Geräte-/WLAN-/Backup-Logik

Im Container erfolgreich geprüft:

```text
git diff --check
./gradlew testDebugUnitTest
```

Host-Build, Installation und manuelle UI-Prüfungen stehen noch aus. Die
Änderungen sind nicht committet oder gepusht.

Issue-Status, Priorisierung und nächste geplante Umsetzung stehen ausschließlich
in `docs/issues/overview.txt`.

## Zuletzt abgeschlossene Arbeit

Issue 027 "WiFi Timeout Analysis And Stabilization" ist implementiert,
geprüft, veröffentlicht und nach `main` gemergt.

- GitHub-Issue: #57
- Pull Request: #58
- Merge-Commit: `7779e1d`

## Umgesetzter Scope Issue 027

- vollständige, zeitgestempelte WLAN-/DHCP-/DNS-/HTTP-Diagnose
- sichtbare Zeitdifferenzen zwischen Diagnoseschritten
- WLAN-Erfolg erst nach WiFi-Transport und gültiger Link-IP
- robustes Handling verspäteter, mehrfacher und verlorener NetworkCallbacks
- netzgebundene DNS-Auflösung und verständliche Timeout-Klassifizierung
- direkte Meldung bei deaktiviertem WLAN
- WPA2/WPA3-Fallback innerhalb eines gemeinsamen Zeitbudgets
- lokal ungeprüfter Status für importierte WLAN-Sicherheitstypen
- einmalige WPA2/WPA3-Erkennung nach Import mit Vordergrund-Scan
- Room-Datenbankversion 5 mit Migration 4 nach 5
- keine zusätzlichen Cloud-, Tracking- oder Logging-Abhängigkeiten

## Bestätigte Prüfungen

```text
git diff --check
./gradlew testDebugUnitTest
./gradlew clean assembleDebug
./gradlew installDebug
```

Host-Build, Installation, Room-Migration sowie manuelle WLAN-, Import-,
Force-Stop- und Geräteaktionstests wurden vom Benutzer als erfolgreich
bestätigt.

## Wichtige Hinweise für die nächste Session

- Wiederverwendbare Startvorlage: `AI_SESSION_PROMPT.md`
- Die vollständige Issue-Liste einschließlich Status, Priorisierung und
  Umsetzungsreihenfolge steht ausschließlich in `docs/issues/overview.txt`.
