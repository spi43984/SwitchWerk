# Issue 074: Gruppen

## Metadaten

- Status: Offen
- Priorität: P1
- Typ: Feature
- Bereich: Gruppen / Dashboard / Geräteaktionen / Konfiguration

## Ziel

SwitchWerk soll Geräte oder Geräteaktionen in Gruppen zusammenfassen können, damit mehrere zusammengehörige Aktionen einfacher gefunden und später gemeinsam ausgeführt werden können.

## Hintergrund

Mit wachsender Anzahl von Geräten reicht eine reine Geräteliste nicht mehr aus. Gruppen helfen bei der Strukturierung, zum Beispiel nach Ort, Funktion oder Anwendungsfall.

## Scope

### Gruppenverwaltung

- Gruppen lokal anlegen, umbenennen und löschen.
- Geräte oder konkrete Geräteaktionen Gruppen zuordnen.
- Ein Gerät darf mehreren Gruppen zugeordnet werden.
- Gruppen sollen im Dashboard sichtbar oder filterbar sein.

### Gruppendarstellung

- Gruppen im Dashboard übersichtlich anzeigen.
- Innerhalb einer Gruppe die zugeordneten Geräteaktionen anzeigen.
- Leere Gruppen verständlich behandeln.
- Gruppenreihenfolge einfach und robust festlegen oder sinnvoll sortieren.

### Gruppenausführung

- Für dieses Issue mindestens die Struktur und Darstellung von Gruppen umsetzen.
- Gemeinsames Ausführen mehrerer Aktionen nur aufnehmen, wenn es ohne große Zusatzkomplexität sauber möglich ist.
- Falls gemeinsame Ausführung mehr Aufwand benötigt, ein Folge-Issue für Szenen oder Gruppenaktionen vorbereiten.

### Import und Export

- Gruppen in Konfigurationsexport aufnehmen.
- Gruppen beim Import konsistent übernehmen.
- Ältere Konfigurationen ohne Gruppen bleiben kompatibel.

## Nicht im Scope

- Keine komplexen Szenen mit Bedingungen.
- Keine zeitgesteuerten Automationen.
- Keine Cloud-Synchronisation.
- Keine Rollen- oder Benutzerverwaltung.
- Keine Änderung der WLAN-Verbindungslogik, außer sie ist für bestehende Geräteaktionen ohnehin erforderlich.

## Architekturhinweise

- Gruppen als lokale Domänenmodelle ergänzen.
- Bestehende MVVM-, Repository- und Room-Struktur beibehalten.
- Import-/Export-Versionierung oder Kompatibilitätslogik beachten.
- Keine Netzwerklogik in Gruppen-UI.
- Gemeinsame Ausführung nur über bestehende Geräteaktionslogik.

## Akzeptanzkriterien

- [ ] Gruppen können angelegt, umbenannt und gelöscht werden.
- [ ] Geräte oder Geräteaktionen können Gruppen zugeordnet werden.
- [ ] Ein Gerät kann mehreren Gruppen zugeordnet werden.
- [ ] Gruppen sind im Dashboard sichtbar oder als Filter nutzbar.
- [ ] Leere Gruppen werden verständlich dargestellt.
- [ ] Gruppen werden exportiert und importiert.
- [ ] Ältere Konfigurationen ohne Gruppen bleiben importierbar.
- [ ] Bestehende Geräteaktionen funktionieren unverändert.
- [ ] Deutsche und englische Texte sind konsistent gepflegt.
- [ ] Hilfe-, Info- und Tooltip-Texte wurden geprüft und bei Bedarf aktualisiert.

## Testhinweise

- Gruppe anlegen, umbenennen und löschen.
- Gerät einer Gruppe zuordnen.
- Gerät mehreren Gruppen zuordnen.
- Gruppe im Dashboard anzeigen oder filtern.
- Leere Gruppe prüfen.
- Export und Import mit Gruppen prüfen.
- Import älterer Konfiguration ohne Gruppen prüfen.
