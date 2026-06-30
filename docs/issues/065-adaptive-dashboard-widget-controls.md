# Issue 065: Adaptive Dashboard Widget Controls

## Metadaten

- Status: Offen
- Priorität: P0
- Typ: UX / UI
- Bereich: Dashboard / Widget-Ansicht / Compose UI

## Ziel

Die Dashboard-Widget-Ansicht soll auf schmalen Displays und bei großer Android-Schriftgröße stabil und lesbar bleiben.

Widgets sollen weiterhin nebeneinander dargestellt werden. Wenn die Darstellung durch schmale Displays oder große Schrift nicht mehr ohne unlesbaren Text möglich ist, sollen die Umschalt-Buttons für Listen- und Widget-Ansicht automatisch ausgeblendet werden.

## Hintergrund

Auf Geräten mit schmalem Display oder größer eingestellter Systemschrift kann es passieren, dass Widgets nicht mehr nebeneinander, sondern untereinander dargestellt werden oder dass der verfügbare Platz für die Widget-Beschriftung nicht mehr ausreicht.

Die Widget-Ansicht soll zunächst adaptiv schmäler werden, solange der Text weiterhin vollständig und einzeilig lesbar bleibt. Ein Umbruch der Widget-Texte ist nicht gewünscht.

Wenn diese lesbare Darstellung nicht mehr möglich ist, soll als Fallback der Widget-Button ausgeblendet werden. In diesem Fall kann auch der Listen-Button entfallen, da beide Buttons nur zum Umschalten der Ansicht dienen und auf kleinen Displays zusätzlichen Platz verbrauchen.

## Scope

### Adaptive Widget-Darstellung

- Widget-Karten sollen flexible Breiten verwenden.
- Widgets sollen nebeneinander dargestellt bleiben, solange der Text vollständig lesbar ist.
- Widget-Texte dürfen nicht umbrechen.
- Widget-Texte sollen nicht abgeschnitten oder nur mit Ellipsis als „lesbar“ gelten.
- Die Darstellung soll Android-Schriftgrößen beziehungsweise `fontScale` berücksichtigen.

### Fallback bei Platzmangel

- Wenn die Widget-Texte bei verfügbarer Breite und aktueller Schriftgröße nicht mehr vollständig einzeilig lesbar dargestellt werden können:
  - Widget-Button ausblenden.
  - Listen-Button ebenfalls ausblenden.
  - Die dadurch frei werdende Breite vollständig für die Widget-Darstellung nutzen.
- Das Layout soll nicht automatisch auf mehrere Zeilen umbrechen.
- Die Widgets sollen nicht untereinander dargestellt werden, nur weil die Umschalt-Buttons zu viel Platz verbrauchen.

### Betroffene Bereiche

- Dashboard-Kopfzeile.
- Umschaltung zwischen Listenansicht und Widget-Ansicht.
- Widget-Ansicht im Dashboard.
- Portrait- und Landscape-Modus.
- Geräte mit schmalem Display.
- Geräte mit großer Android-Schriftgröße.

## Nicht im Scope

- Keine neue Dashboard-Ansicht.
- Keine fachliche Änderung an Geräten oder Aktionen.
- Keine Änderung der gespeicherten Dashboard-Auswahl.
- Keine Änderung an Import oder Export der Dashboard-Einstellung.
- Keine Homescreen-Widgets von Android.
- Keine neue externe Abhängigkeit.
- Kein automatischer Zeilenumbruch für Widget-Texte.
- Kein GitHub-Issue, Branch, Pull Request oder Merge im Rahmen des lokalen Planungs-Issues.

## Architekturhinweise

- Bestehende Compose- und Material-3-Struktur beibehalten.
- Vorzugsweise `BoxWithConstraints`, verfügbare Breite und `LocalDensity.current.fontScale` verwenden.
- Die Entscheidung zum Ausblenden der Umschalt-Buttons soll aus Layout- und Lesbarkeitskriterien abgeleitet werden.
- Die gespeicherte Dashboard-Ansicht darf durch das temporäre Ausblenden der Buttons nicht verändert werden.
- Hilfe-, Info- und Tooltip-Texte prüfen und bei Bedarf auf Deutsch und Englisch aktualisieren.

## Akzeptanzkriterien

- [ ] Widgets verwenden adaptive Breiten.
- [ ] Widgets bleiben nebeneinander, solange die Widget-Texte vollständig einzeilig lesbar sind.
- [ ] Widget-Texte brechen nicht auf mehrere Zeilen um.
- [ ] Widget-Texte werden nicht als akzeptabel betrachtet, wenn sie nur abgeschnitten oder mit Ellipsis sichtbar sind.
- [ ] Bei zu wenig Breite wird der Widget-Button automatisch ausgeblendet.
- [ ] Wenn der Widget-Button ausgeblendet wird, wird auch der Listen-Button ausgeblendet.
- [ ] Die freigewordene Breite wird für die Widget-Darstellung genutzt.
- [ ] Die gespeicherte Dashboard-Auswahl bleibt unverändert.
- [ ] Die Lösung funktioniert im Portrait-Modus.
- [ ] Die Lösung funktioniert im Landscape-Modus.
- [ ] Die Lösung funktioniert bei großer Android-Schriftgröße.
- [ ] Hilfe-, Info- und Tooltip-Texte wurden geprüft und bei Bedarf aktualisiert.
- [ ] Deutsch und Englisch sind bei neuen Texten konsistent gepflegt.

## Testhinweise

- Dashboard mit Widget-Ansicht auf normal breitem Display prüfen.
- Dashboard mit Widget-Ansicht auf schmalem Display prüfen.
- Dashboard mit Widget-Ansicht im Landscape-Modus prüfen.
- Große Android-Schriftgröße aktivieren und Dashboard prüfen.
- Sehr lange Gerätenamen prüfen.
- Prüfen, dass Widget- und Listen-Button bei ausreichendem Platz sichtbar bleiben.
- Prüfen, dass Widget- und Listen-Button bei Platzmangel ausgeblendet werden.
- Prüfen, dass die gespeicherte Dashboard-Ansicht nach App-Neustart unverändert bleibt.
- Prüfen, dass Listenansicht und Widget-Ansicht weiterhin umschaltbar sind, wenn ausreichend Platz vorhanden ist.
