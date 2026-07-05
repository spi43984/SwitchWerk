# Issue 074: Schaltgruppen

## Metadaten

- Status: Offen
- Priorität: P1
- Typ: Feature
- Bereich: Schaltgruppen / Dashboard / Geräteaktionen / Einstellungen / Konfiguration

## Ziel

SwitchWerk soll Schaltgruppen unterstützen. Eine Schaltgruppe bündelt zwei oder mehr bereits konfigurierte Geräteaktionen und kann vom Anwender wie ein normales Gerät über das Dashboard ausgelöst werden.

Schaltgruppen dienen nicht primär der Gruppierung oder Filterung der Anzeige, sondern der gemeinsamen Ausführung mehrerer Geräteaktionen.

## Hintergrund

Die ursprüngliche Intention dieses Issues ist, mehrere Geräte gemeinsam zu schalten. Beispiele sind Beleuchtungsszenen, mehrere Relais, mehrere Shelly-Geräte, Lüfter, Tore oder andere lokale Geräte, die in einer festen Reihenfolge ausgelöst werden sollen.

Für Anwender soll eine Schaltgruppe auf dem Dashboard möglichst wie ein normales Gerät wirken: Name antippen, Aktion läuft, Fortschritt oder Ergebnis wird angezeigt. Die innere Abfolge der enthaltenen Geräteaktionen wird in den Einstellungen konfiguriert.

Konfigurierbare Pausen zwischen Geräten sind wichtig, weil manche Geräte oder Anwendungsfälle eine kurze Verzögerung benötigen, z. B. um Stromspitzen zu vermeiden, mechanische Abläufe zu entzerren oder zwei Aktionen bewusst zeitversetzt auszuführen.

## Scope

### Schaltgruppenverwaltung

- Unter Einstellungen einen eigenen Reiter `Gruppen` ergänzen.
- Die Reihenfolge der Einstellungsreiter soll sein:
  - `WLAN-Profile`
  - `Geräte`
  - `Gruppen`
  - `System`
  - `Backup`
- Schaltgruppen lokal anlegen, umbenennen und löschen.
- Eine Schaltgruppe hat mindestens:
  - stabile interne ID
  - Namen
  - Aktionsbeschriftung
  - Dashboard-Reihenfolge
- Schaltgruppen sollen in derselben Dashboard-Sortierung wie normale Geräte erscheinen können.
- Eine Schaltgruppe darf keine leere Ausführung ermöglichen. Leere Schaltgruppen sollen verständlich angezeigt und nicht ausführbar sein.

### Mitglieder einer Schaltgruppe

- Bereits konfigurierte Geräte zu einer Schaltgruppe hinzufügen.
- Geräte aus einer Schaltgruppe entfernen.
- Eine Schaltgruppe kann zwei oder mehr Geräte enthalten.
- Dasselbe Gerät darf mehrfach in derselben Schaltgruppe vorkommen, damit
  zeitversetzte Wiederholungen derselben Geräteaktion möglich sind.
- Die Reihenfolge der Geräte innerhalb einer Schaltgruppe muss mit Hoch-/Runter-Pfeilen festgelegt werden können.
- Die Bedienung soll bewusst robust über Pfeile erfolgen und kein Drag-and-Drop einführen.
- Pro Gruppenmitglied soll eine Pause nach der Geräteaktion konfigurierbar sein.
- Für Pausen sinnvolle feste Werte anbieten, z. B.:
  - 0 ms
  - 500 ms
  - 1 s
  - 2 s
- Eine benutzerdefinierte Pause kann zusätzlich vorgesehen werden, wenn sie ohne große Zusatzkomplexität sauber validierbar ist.
- Benutzerdefinierte Pausen sollen bis maximal 1 Stunde erlaubt sein und in der
  UI in einer lesbaren Zeiteinheit dargestellt werden, nicht nur als langer
  Millisekundenwert.

### Dashboard

- Schaltgruppen werden auf dem Dashboard wie normale Geräte angezeigt.
- Eine Schaltgruppe soll optisch als ausführbare Aktion erkennbar sein, aber die Bedienung soll dem normalen Geräteschalten entsprechen.
- Beim Antippen einer Schaltgruppe wird die Gruppe ausgeführt.
- Während der Ausführung wird ein laufender Zustand angezeigt.
- Erfolg, Abbruch und Fehler werden verständlich angezeigt.
- Normale Geräteaktionen auf dem Dashboard bleiben unverändert.
- Dieses Issue führt keine Dashboard-Filterung nach Gruppen ein.
- Dieses Issue führt keine Anzeige-Kategorien oder Ordnerstruktur für Geräte ein.

### Ausführung von Schaltgruppen

- Eine Schaltgruppe führt ihre Geräteaktionen sequenziell in der konfigurierten Reihenfolge aus.
- Zwischen zwei Geräteaktionen wird die beim vorherigen Gruppenmitglied konfigurierte Pause eingehalten.
- Die Ausführung nutzt ausschließlich die bestehende Geräteaktionslogik.
- Es werden keine neuen HTTP-, RPC- oder WLAN-Sonderwege für Gruppen eingeführt.
- Während eine Schaltgruppe läuft, soll sie nicht mehrfach parallel gestartet werden können.
- Wenn möglich, soll eine laufende Schaltgruppe abgebrochen werden können.
- Das Fehlerverhalten soll für den ersten Schritt einfach und nachvollziehbar sein:
  - Standardmäßig wird die Schaltgruppe bei Fehler einer Geräteaktion abgebrochen.
  - Optional kann eingestellt werden, dass die Schaltgruppe nach Fehlern weiterläuft.
  - Der fehlerhafte Schritt und die betroffene Geräteaktion werden verständlich angezeigt.
- Komplexere Fehlerstrategien wie `nur warnen`, Bedingungen oder automatische Wiederholungen sind nicht Bestandteil dieses Issues.

### Import und Export

- Schaltgruppen in den Konfigurationsexport aufnehmen.
- Gruppenmitglieder, Reihenfolge und Pausen exportieren.
- Schaltgruppen beim Import konsistent übernehmen.
- Ältere Konfigurationen ohne Schaltgruppen bleiben kompatibel.
- Importierte Schaltgruppen dürfen nur auf vorhandene oder importierte Geräte verweisen.
- Fehlende Gerätereferenzen müssen beim Import sicher und verständlich behandelt werden.

## Nicht im Scope

- Keine Gruppen zur reinen Anzeigeorganisation.
- Keine Dashboard-Filterung nach Gruppen.
- Keine Ordner- oder Kategorieansicht für Geräte.
- Keine verschachtelten Gruppen.
- Keine Gruppen in Gruppen.
- Keine parallele Gruppenausführung.
- Keine komplexen Szenen mit Bedingungen.
- Keine zeitgesteuerten Automationen.
- Keine komplexen Fehlerstrategien über `beim ersten Fehler abbrechen` und `nach Fehlern weiter ausführen` hinaus.
- Keine Cloud-Synchronisation.
- Keine Rollen- oder Benutzerverwaltung.
- Keine Änderung der WLAN-Verbindungslogik.
- Keine Erweiterung der Intent-Funktion aus Issue 072.
- Keine Quick Settings Tiles.
- Keine farbigen Geräte oder Gruppenfarben.
- Keine Templates.

## Architekturhinweise

- Bestehende MVVM-, Repository- und Room-Struktur beibehalten.
- Keine Netzwerklogik in UI oder Composables.
- Schaltgruppen als lokale Domänenmodelle ergänzen.
- Eine Schaltgruppe nicht als normales `Device` in der bestehenden Device-Tabelle speichern, wenn dadurch Geräte- und Gruppenlogik vermischt würde.
- Für das Dashboard ein gemeinsames UI-Modell vorsehen, z. B. sinngemäß `DashboardItem`, das normale Geräte und Schaltgruppen darstellen kann.
- Schaltgruppenmitglieder in einer eigenen Zuordnungstabelle speichern.
- Die Reihenfolge und Pause eines Gruppenmitglieds in der Zuordnung speichern.
- Ausführung einer Schaltgruppe über einen eigenen Service oder eine klar abgegrenzte ViewModel-/Repository-Schicht koordinieren, die intern die bestehende `DeviceActionService`-Logik wiederverwendet.
- Keine beliebigen URLs, Hosts oder Befehle in Gruppen speichern; Gruppen referenzieren ausschließlich bereits lokal konfigurierte Geräteaktionen.
- Room-Migration ergänzen.
- Import-/Export-Versionierung oder Kompatibilitätslogik beachten.
- Hilfe-, Info- und Tooltip-Texte für Schaltgruppen prüfen und bei Bedarf aktualisieren.

## Akzeptanzkriterien

- [ ] Unter Einstellungen gibt es einen eigenen Reiter `Gruppen` zwischen `Geräte` und `System`.
- [ ] Schaltgruppen können angelegt, umbenannt und gelöscht werden.
- [ ] Geräte können einer Schaltgruppe hinzugefügt und daraus entfernt werden.
- [ ] Eine Schaltgruppe kann mehrere Geräte enthalten.
- [ ] Dasselbe Gerät kann mehrfach in derselben Schaltgruppe eingetragen werden.
- [ ] Geräte können innerhalb einer Schaltgruppe per Hoch-/Runter-Pfeilen sortiert werden.
- [ ] Pro Gruppenmitglied kann eine Pause nach der Geräteaktion konfiguriert werden.
- [ ] Benutzerdefinierte Pausen sind bis maximal 1 Stunde erlaubt und werden
      lesbar formatiert angezeigt.
- [ ] Schaltgruppen werden auf dem Dashboard wie normale ausführbare Geräte angezeigt.
- [ ] Eine Schaltgruppe wird beim Antippen sequenziell in der konfigurierten Reihenfolge ausgeführt.
- [ ] Die konfigurierten Pausen zwischen den Geräteaktionen werden eingehalten.
- [ ] Eine laufende Schaltgruppe kann nicht versehentlich mehrfach parallel gestartet werden.
- [ ] Fehler in einer enthaltenen Geräteaktion werden verständlich angezeigt.
- [ ] Bei Fehler einer enthaltenen Geräteaktion wird die Schaltgruppe standardmäßig abgebrochen.
- [ ] Optional kann eine Schaltgruppe nach Fehlern weiter ausgeführt werden.
- [ ] Leere Schaltgruppen werden verständlich dargestellt und nicht ausgeführt.
- [ ] Bestehende Einzelgeräteaktionen funktionieren unverändert.
- [ ] Schaltgruppen, Mitglieder, Reihenfolge und Pausen werden exportiert und importiert.
- [ ] Ältere Konfigurationen ohne Schaltgruppen bleiben importierbar.
- [ ] Deutsche und englische Texte sind konsistent gepflegt.
- [ ] Hilfe-, Info- und Tooltip-Texte wurden geprüft und bei Bedarf aktualisiert.

## Testhinweise

- Schaltgruppe anlegen, umbenennen und löschen.
- Geräte zu einer Schaltgruppe hinzufügen.
- Geräte aus einer Schaltgruppe entfernen.
- Mehrere Geräte in einer Schaltgruppe konfigurieren.
- Geräte innerhalb einer Schaltgruppe mit Hoch-/Runter-Pfeilen sortieren.
- Pausen zwischen Geräteaktionen konfigurieren.
- Schaltgruppe auf dem Dashboard anzeigen.
- Schaltgruppe vom Dashboard aus ausführen.
- Prüfen, dass die Geräteaktionen sequenziell ausgeführt werden.
- Prüfen, dass Pausen zwischen Geräteaktionen eingehalten werden.
- Verhalten bei leerer Schaltgruppe prüfen.
- Verhalten bei Fehler einer enthaltenen Geräteaktion prüfen.
- Prüfen, dass normale Einzelgeräte weiterhin unverändert funktionieren.
- Export und Import mit Schaltgruppen prüfen.
- Export und Import von Reihenfolge und Pausen prüfen.
- Import älterer Konfiguration ohne Schaltgruppen prüfen.
