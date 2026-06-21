# AI Handoff

Stand: 21. Juni 2026

## Aktuelle Arbeit

Keine aktive Implementierung.

Issue-Status, Priorisierung und nächste geplante Umsetzung stehen ausschließlich
in `docs/issues/overview.txt`.

Nächstes offenes Issue: 025 "Dashboard Widget Layout".

## Zuletzt abgeschlossene Arbeit

Issue 039 "Unified List Interactions" ist implementiert und manuell geprüft.

- GitHub-Issue: #68
- gemeinsame Swipe-to-delete-Komponente unter `ui/components`
- WLAN-Profile, Geräte und Geräte-WLAN-Zuordnungen nutzen dieselbe
  Listeninteraktion
- Tap auf den Listeneintrag öffnet die Bearbeitung
- Swipe zeigt ausschließlich die Löschaktion; WLAN-Profile, Geräte und
  Geräte-WLAN-Zuordnungen verlangen vor dem Löschen eine Bestätigung
- jeder Tap im Geräteformular beendet einen offenen Swipe der
  Geräte-WLAN-Zuordnungen; horizontale Swipe-Gesten bleiben davon ausgenommen
- höhere horizontale Auslösedistanz reduziert versehentliche Swipes beim
  vertikalen Scrollen
- geschlossene Listeneinträge übernehmen transparent den Hintergrund ihres
  Containers; der Lösch-Callback läuft unabhängig von der Schließanimation
- Bleistift- und direkter Löschbutton der Geräte-WLAN-Zuordnungen entfernt
- Hoch-/Runter-Pfeile sowie bestehende Sortier-, Persistenz- und
  ViewModel-Callbacks unverändert

Bestätigte Prüfungen:

```text
git diff --check
./gradlew testDebugUnitTest
manuelle Geräteprüfung der Listeninteraktionen, Löschbestätigungen,
Swipe-Empfindlichkeit und Darstellung
```

## Zuvor abgeschlossene Arbeit

Issue 020 "Device Assigned WiFi Order" ist implementiert, auf dem Ubuntu-Host
geprüft und abgeschlossen.

- GitHub-Issue: #63
- robuste Hoch/Runter-Sortierung in der Gerätebearbeitung
- 300 dp hohe Zuordnungsliste mit Scroll-Hinweisen oben und unten
- persistierte Reihenfolge über die bestehende `priority`-Spalte
- gespeicherte Reihenfolge als verbindliche Reihenfolge des Schaltvorgangs
- keine Room-Migration, neuen Berechtigungen oder Änderungen an Zugangsdaten
- Drag-and-Drop nach Gerätetest bewusst zugunsten der Pfeilbedienung verworfen

Bestätigte Prüfungen:

```text
./gradlew testDebugUnitTest
./gradlew clean assembleDebug
./gradlew installDebug
manuelle Sortier-, Neustart-, Schalt-, Import- und Exporttests
```

## Weitere zuvor abgeschlossene Arbeit

Issue 026 "Settings UI Rework" ist implementiert, geprüft, veröffentlicht und
nach `main` gemergt.

- GitHub-Issue: #59
- Pull Request: #62
- Merge-Commit: `bf29768`
- Hamburger-Menü mit Einstellungen und Hilfe
- Settings-Tabs für WLAN-Profile, Geräte, System und Backup
- wiederverwendbare Hilfe-, Dialog-, Button- und Tab-Komponenten
- vereinheitlichte WLAN-, Geräte- und Import-/Export-Dialoge
- Dark-Mode-Hintergrund und rahmenlose Settings-Bereiche
- keine Änderung an fachlicher WLAN-, Geräteaktions-, Import-/Export- oder Passwortlogik

Bestätigte Prüfungen:

```text
git diff --check
./gradlew testDebugUnitTest
./gradlew clean assembleDebug
./gradlew installDebug
```

## Frühere abgeschlossene Arbeit

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
