# Issue #30: WiFi Profile Deletion Safety

## Metadaten

- Status: Abgeschlossen
- Priorität: P1
- Typ: WLAN / Sicherheit

## Ziel

Verhindern, dass Geräte nach dem Löschen eines verwendeten WLAN-Profils unbemerkt auf ein nicht mehr vorhandenes WLAN verweisen.

Der Benutzer soll vor dem Löschen erkennen können, welche Geräte betroffen sind.

## Problem

Aktuell kann ein WLAN-Profil gelöscht werden, obwohl es noch Geräten zugeordnet ist.

Nach dem Löschen erscheint bei diesen Geräten lediglich „unbekanntes WLAN“.

## Scope

- Vor dem Löschen prüfen, ob das WLAN-Profil Geräten zugeordnet ist.
- Anzahl betroffener Geräte ermitteln.
- Betroffene Gerätenamen anzeigen.
- Sicherheitsabfrage vor dem Löschen anzeigen.
- Gerätezuordnungen beim Löschen automatisch bereinigen.
- Nach dem Löschen dürfen keine verwaisten WLAN-Referenzen mehr existieren.

## Nicht im Scope

- Automatische Zuordnung eines Ersatz-WLANs.
- Massenänderungen von Gerätezuordnungen.
- Import-/Export-Anpassungen.

## Akzeptanzkriterien

- [x] Beim Löschen eines unbenutzten WLAN-Profils erfolgt keine zusätzliche Warnung.
- [x] Beim Löschen eines verwendeten WLAN-Profils wird die Anzahl betroffener Geräte angezeigt.
- [x] Die betroffenen Gerätenamen werden angezeigt.
- [x] Der Benutzer muss das Löschen ausdrücklich bestätigen.
- [x] Die sichere Abbruchaktion befindet sich rechts.
- [x] Nach dem Löschen existieren keine verwaisten WLAN-Referenzen mehr.
- [x] Geräte zeigen nach dem Löschen nicht mehr „unbekanntes WLAN“ aufgrund einer verwaisten Referenz.
- [x] Bestehende Datenbankmigrationen bleiben unverändert.

## Abschluss

- GitHub-Issue: #98
- Pull Request: #99
- Merge-Commit: `74dd238`
- Container-Prüfungen: `./gradlew lintDebug`, `./gradlew testDebugUnitTest`,
  `git diff --check`
- Host-Prüfungen und manuelle Löschszenarien wurden erfolgreich bestätigt.
