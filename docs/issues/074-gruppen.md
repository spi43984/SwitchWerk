# Issue 074: Gruppen

## Metadaten

- Status: Offen
- Priorität: P1
- Typ: Feature
- Bereich: Gruppen / Dashboard / Einstellungen / Konfiguration

## Ziel

SwitchWerk soll Geräte in Gruppen zusammenfassen können, damit mehrere zusammengehörige Geräte einfacher gefunden, strukturiert und im Dashboard gefiltert oder gruppiert angezeigt werden können.

## Hintergrund

Mit wachsender Anzahl von Geräten reicht eine reine Geräteliste nicht mehr aus. Gruppen helfen bei der Strukturierung, zum Beispiel nach Ort, Funktion oder Anwendungsfall.

Gruppen sollen zunächst die Organisation und Darstellung verbessern. Eine spätere gemeinsame Ausführung mehrerer Geräteaktionen mit Pausen, Fehlerstrategie und Abbruchverhalten ist fachlich sinnvoll, soll aber separat umgesetzt werden, damit dieses Issue überschaubar bleibt.

## Scope

### Gruppenverwaltung

- Unter Einstellungen einen eigenen Reiter `Gruppen` ergänzen.
- Die Reihenfolge der Einstellungsreiter soll sein:
  - `WLAN-Profile`
  - `Geräte`
  - `Gruppen`
  - `System`
  - `Backup`
- Gruppen lokal anlegen, umbenennen und löschen.
- Geräte Gruppen zuordnen und wieder aus Gruppen entfernen.
- Ein Gerät darf mehreren Gruppen zugeordnet werden.
- Innerhalb einer Gruppe muss die Reihenfolge der zugeordneten Geräte mit Hoch-/Runter-Pfeilen festgelegt werden können.
- Die Bedienung soll bewusst robust über Pfeile erfolgen und kein Drag-and-Drop einführen.

### Gruppendarstellung

- Gruppen im Dashboard übersichtlich anzeigen oder als Filter nutzbar machen.
- Standardansicht bleibt `Alle Geräte` beziehungsweise die bisherige ungefilterte Geräteliste.
- Innerhalb einer Gruppe die zugeordneten Geräte in der gruppenspezifischen Reihenfolge anzeigen.
- Geräteaktionen innerhalb einer Gruppe unverändert über die bestehende Einzelgeräte-Aktionslogik ausführen.
- Leere Gruppen verständlich darstellen.
- Gruppenreihenfolge einfach und robust festlegen oder sinnvoll sortieren.

### Gruppenausführung

- Für dieses Issue nur Struktur, Verwaltung und Darstellung von Gruppen umsetzen.
- Keine gemeinsame Ausführung einer Gruppe in diesem Issue umsetzen.
- Keine konfigurierbaren Pausen zwischen Geräten in diesem Issue umsetzen.
- Für Gruppenaktionen, sequentielle Ausführung, konfigurierbare Pausen, Fortschrittsanzeige, Abbruch und Fehlerstrategie soll bei Bedarf ein eigenes Folge-Issue vorbereitet werden.

### Import und Export

- Gruppen in Konfigurationsexport aufnehmen.
- Gruppenzuordnungen und gruppenspezifische Gerätereihenfolge exportieren.
- Gruppen beim Import konsistent übernehmen.
- Ältere Konfigurationen ohne Gruppen bleiben kompatibel.

## Nicht im Scope

- Keine komplexen Szenen mit Bedingungen.
- Keine zeitgesteuerten Automationen.
- Keine Cloud-Synchronisation.
- Keine Rollen- oder Benutzerverwaltung.
- Keine gemeinsame Gruppenausführung.
- Keine konfigurierbaren Pausen zwischen Geräteaktionen.
- Keine Änderung der WLAN-Verbindungslogik.
- Keine Erweiterung der Intent-Funktion aus Issue 072.
- Keine Quick Settings Tiles.
- Keine farbigen Geräte oder Gruppenfarben.
- Keine Templates.

## Architekturhinweise

- Gruppen als lokale Domänenmodelle ergänzen.
- Bestehende MVVM-, Repository- und Room-Struktur beibehalten.
- Gruppen nicht als einfaches Feld direkt am Gerät modellieren, da ein Gerät mehreren Gruppen angehören darf.
- Eine eigene Zuordnungstabelle für Gruppe-zu-Gerät verwenden.
- Die gruppenspezifische Gerätereihenfolge in der Zuordnung speichern.
- Room-Migration ergänzen.
- Import-/Export-Versionierung oder Kompatibilitätslogik beachten.
- Keine Netzwerklogik in Gruppen-UI.
- Einzelne Geräteaktionen weiterhin nur über bestehende Geräteaktionslogik ausführen.
- Hilfe-, Info- und Tooltip-Texte für Gruppen und Dashboard-Filter prüfen und bei Bedarf aktualisieren.

## Akzeptanzkriterien

- [ ] Unter Einstellungen gibt es einen eigenen Reiter `Gruppen` zwischen `Geräte` und `System`.
- [ ] Gruppen können angelegt, umbenannt und gelöscht werden.
- [ ] Geräte können Gruppen zugeordnet und aus Gruppen entfernt werden.
- [ ] Ein Gerät kann mehreren Gruppen zugeordnet werden.
- [ ] Geräte können innerhalb einer Gruppe per Hoch-/Runter-Pfeilen sortiert werden.
- [ ] Gruppen sind im Dashboard sichtbar oder als Filter nutzbar.
- [ ] Die Standardansicht aller Geräte bleibt erhalten.
- [ ] Leere Gruppen werden verständlich dargestellt.
- [ ] Bestehende Einzelgeräteaktionen funktionieren unverändert.
- [ ] Gruppen und Gruppenzuordnungen werden exportiert und importiert.
- [ ] Die gruppenspezifische Gerätereihenfolge wird exportiert und importiert.
- [ ] Ältere Konfigurationen ohne Gruppen bleiben importierbar.
- [ ] Deutsche und englische Texte sind konsistent gepflegt.
- [ ] Hilfe-, Info- und Tooltip-Texte wurden geprüft und bei Bedarf aktualisiert.

## Testhinweise

- Gruppe anlegen, umbenennen und löschen.
- Gerät einer Gruppe zuordnen.
- Gerät aus einer Gruppe entfernen.
- Gerät mehreren Gruppen zuordnen.
- Geräte innerhalb einer Gruppe mit Hoch-/Runter-Pfeilen sortieren.
- Gruppe im Dashboard anzeigen oder filtern.
- Standardansicht aller Geräte prüfen.
- Leere Gruppe prüfen.
- Einzelne Geräteaktion aus einer Gruppenansicht ausführen.
- Export und Import mit Gruppen prüfen.
- Export und Import der gruppenspezifischen Gerätereihenfolge prüfen.
- Import älterer Konfiguration ohne Gruppen prüfen.
