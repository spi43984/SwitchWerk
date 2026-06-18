# Issue #30: WiFi Profile Deletion Safety

## Metadaten

- Status: Offen
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

- [ ] Beim Löschen eines unbenutzten WLAN-Profils erfolgt keine zusätzliche Warnung.
- [ ] Beim Löschen eines verwendeten WLAN-Profils wird die Anzahl betroffener Geräte angezeigt.
- [ ] Die betroffenen Gerätenamen werden angezeigt.
- [ ] Der Benutzer muss das Löschen ausdrücklich bestätigen.
- [ ] Die sichere Abbruchaktion befindet sich rechts.
- [ ] Nach dem Löschen existieren keine verwaisten WLAN-Referenzen mehr.
- [ ] Geräte zeigen nach dem Löschen nicht mehr „unbekanntes WLAN“ aufgrund einer verwaisten Referenz.
- [ ] Bestehende Datenbankmigrationen bleiben unverändert.
