# Issue #41: Dashboard Drag And Drop

## Metadaten

- Status: Verworfen
- Priorität: P2
- Typ: GUI / Dashboard / Bedienung

## Ziel

Die Geräte im Dashboard sollen per Drag & Drop sortiert werden können.

Die bestehende Pfeil-Sortierung aus Issue 014 bleibt erhalten und dient weiterhin als robuste Fallback-Bedienung.

## Hintergrund

Issue 014 hat die Dashboard-Sortierung bewusst über Hoch-/Runter-Aktionen umgesetzt.

Issue 025 führt die Widget-Ansicht ein. Wegen der zusätzlichen Komplexität soll Drag & Drop separat geplant und umgesetzt werden.

Damit bleibt die Drag-&-Drop-Umsetzung fachlich eigenständig und kann unabhängig von der Widget-Layout-Umsetzung bewertet werden.

## Scope

- Drag & Drop für Geräte im Dashboard prüfen und umsetzen
- Listenansicht unterstützen
- Widget-Ansicht unterstützen, sofern Issue 025 bereits umgesetzt ist
- Reihenfolge nach Drag & Drop dauerhaft speichern
- bestehende Pfeil-Sortierung beibehalten
- Bedienbarkeit auf kleinen Displays prüfen
- Konflikte zwischen Scrollen und Ziehen vermeiden
- robuste Touch-Bedienung auf realem Gerät testen

## Architektur

Die bestehende Dashboard-Reihenfolge aus Issue 014 bleibt die fachliche Grundlage.

Falls Issue 025 bis dahin ein separates Layout-/Positionsmodell eingeführt hat, muss Drag & Drop dieses Modell verwenden.

Falls Issue 025 noch nicht umgesetzt ist, beschränkt sich die erste Umsetzung auf die Listenansicht und bereitet die Widget-Ansicht architektonisch vor.

## Nicht im Scope

- Einführung der Widget-Ansicht selbst
- Änderung von Issue 025
- freie Widget-Größen
- Dashboard-Seiten
- Gerätegruppierung
- Änderung der Geräteverwaltung
- Änderung der WLAN-Zuordnungen
- Änderung der Schaltlogik
- neue Cloud- oder Tracking-Abhängigkeiten

## Akzeptanzkriterien

- [ ] Geräte können im Dashboard per Drag & Drop sortiert werden
- [ ] bestehende Hoch-/Runter-Sortierung bleibt verfügbar
- [ ] Reihenfolge bleibt nach App-Neustart erhalten
- [ ] Listenansicht bleibt stabil bedienbar
- [ ] Widget-Ansicht wird unterstützt oder sauber vorbereitet
- [ ] Scrollen und Drag & Drop behindern sich nicht gegenseitig
- [ ] Bedienung funktioniert auf kleinen Displays
- [ ] keine Änderung an Geräteaktionen
- [ ] keine Änderung an WLAN-Verbindungslogik
- [ ] Build erfolgreich

## Testhinweise

- mehrere Geräte im Dashboard anlegen
- Gerät per Drag & Drop nach oben verschieben
- Gerät per Drag & Drop nach unten verschieben
- lange Liste scrollen
- Drag & Drop während Scroll-Kontext testen
- App neu starten und Reihenfolge prüfen
- Pfeil-Sortierung weiterhin testen
- Geräteaktion nach Umsortierung ausführen
- Portrait-Modus testen
- Landscape-Modus testen

## Entscheidung

Status: Verworfen.

Die Umsetzung wird bewusst nicht weiterverfolgt, weil Geräte voraussichtlich selten umsortiert werden und die bestehende Hoch-/Runter-Sortierung den Bedarf robust erfüllt. Drag & Drop hätte nur geringen Bedienmehrwert, erhöht aber das Risiko versehentlicher Sortieränderungen und bringt zusätzlichen Implementierungs- und Testaufwand für Listen- und Widget-Ansicht.
