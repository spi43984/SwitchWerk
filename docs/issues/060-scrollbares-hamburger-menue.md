# Issue 060: Scrollbares Hamburger-Menü bei Platzmangel

## Metadaten

- Status: Abgeschlossen
- Priorität: P0
- Typ: Bugfix / UI / UX
- Bereich: Navigation / Hamburger-Menü / Compose UI

## Ziel

Das Hamburger-Menü bleibt unabhängig von Displaygröße, Orientierung und Schriftgröße vollständig bedienbar.

Wenn der verfügbare Platz nicht ausreicht, muss der Menüinhalt automatisch scrollbar werden.

## Hintergrund

Das Hamburger-Menü mit Icon und Menüeinträgen ist im Landscape-Format zu eng dargestellt.

Dadurch können Menüeinträge abgeschnitten oder nicht vollständig erreichbar sein.

Dieses Problem kann nicht nur im Landscape-Format auftreten, sondern grundsätzlich auch im Portrait-Format, zum Beispiel bei kleinen Displays, großer Schriftgröße oder zusätzlichen Menüeinträgen.

## Scope

### Hamburger-Menü

- Bestehende Drawer-/Hamburger-Menü-Implementierung prüfen.
- Menüinhalt automatisch scrollbar machen, wenn der verfügbare Platz nicht ausreicht.
- Verhalten sowohl im Landscape- als auch im Portrait-Format sicherstellen.
- Hamburger-Icon und Menüeinträge weiterhin korrekt darstellen.
- Alle Menüeinträge müssen jederzeit erreichbar bleiben.
- Das Icon im unteren Info-/Über-Bereich darf im Landscape-Modus höchstens so
  breit dargestellt werden wie im Portrait-Modus.
- Das Icon auf der Über-SwitchWerk-Ansicht darf beim Wechsel von Portrait zu
  Landscape nicht breiter werden.
- Bestehendes Layout und Design möglichst unverändert beibehalten.
- Keine funktionalen Änderungen an den Menüeinträgen vornehmen.

## Nicht im Scope

- Neue Navigation
- Umstrukturierung der bestehenden App-Navigation
- Änderung der Menüeinträge
- Änderung von Icons, Texten oder Zielseiten
- Neues Design-System
- GitHub-Issue, Branch, Pull Request oder Merge

## Architekturhinweise

- Bestehende Compose- und Material-3-Struktur beibehalten.
- Keine neue externe Abhängigkeit einführen.
- Scrollbarkeit lokal auf den Menüinhalt begrenzen.
- Mögliche Ansätze sind `LazyColumn` oder `Column` mit `verticalScroll()`, abhängig von der bestehenden Implementierung.
- Insets, Padding und bestehende Drawer-Struktur berücksichtigen.

## Akzeptanzkriterien

- [x] Im Landscape-Modus sind alle Menüeinträge erreichbar.
- [x] Im Portrait-Modus sind alle Menüeinträge erreichbar.
- [x] Bei Platzmangel kann der Menüinhalt vertikal gescrollt werden.
- [x] Bei ausreichend Platz bleibt das bestehende Layout unverändert.
- [x] Hamburger-Icon und Menüeinträge werden nicht abgeschnitten.
- [x] Das Icon im unteren Info-/Über-Bereich wird im Landscape-Modus nicht
      breiter als im Portrait-Modus dargestellt.
- [x] Das Icon auf der Über-SwitchWerk-Ansicht wird beim Wechsel von Portrait
      zu Landscape nicht breiter dargestellt.
- [x] Die Lösung funktioniert auch bei großer Android-Schriftgröße.
- [x] Es gibt keine Regressionen im bestehenden Drawer-Verhalten.
