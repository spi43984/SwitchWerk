# Issue 085: Statuspunkt-Rahmen und Pulsieren optimieren

## Metadaten

- Status: Offen
- Priorität: P0
- Typ: UX / UI
- Bereich: Dashboard / Geräte- und Gruppenkacheln / Statusanzeige

## Ziel

Der mit Issue 073 eingeführte kontrastierende Rahmen um die farbigen Statuspunkte
rechts oben in Geräte- und Gruppenkacheln soll optisch ausgewogener werden. Der
Statuspunkt soll besser erkennbar bleiben, ohne dass der Rahmen zu dominant wirkt.

## Hintergrund

Seit Issue 073 erhalten farbige Kacheln einen kontrastierenden Rahmen um den
Statuspunkt, damit der Statuspunkt auch auf eingefärbten Kachelhintergründen
sichtbar bleibt.

Aktuell wirkt dieser Rahmen jedoch ziemlich dick. Dadurch erscheint der innere
farbige Kreis relativ klein. Zusätzlich pulsiert der innere Kreis nach einer
ausgeführten Aktion derzeit optisch gegen die Kachelhintergrundfarbe. Dadurch
geht die eigentliche Statusfarbe teilweise verloren.

## Scope

- Rahmen des Statuspunkts etwas dünner machen, aber weiterhin gut sichtbar halten.
- Rahmenfarbe weiterhin als Kontrastfarbe zur Kachelfarbe beziehungsweise zum
  jeweiligen Hintergrund bestimmen.
- Statuspunkt insgesamt etwas größer darstellen.
- Inneren farbigen Kreis ebenfalls etwas größer darstellen, damit Grün, Rot oder
  Grau besser erkennbar bleiben.
- Pulsierende Anzeige nach ausgeführter Aktion so anpassen, dass der innere Kreis
  zwischen seiner Statusfarbe und der Rahmenfarbe wechselt.
- Pulsieren nicht mehr gegen die Kachelhintergrundfarbe beziehungsweise
  Kartenfarbe durchführen.
- Darstellung für Geräte- und Gruppenkacheln konsistent halten.
- Darstellung in Listen- und Widget-Dashboard prüfen, sofern beide dieselbe
  Statuspunkt-Komponente verwenden.

## Nicht im Scope

- Keine Änderung der Geräteaktionslogik.
- Keine Änderung der WLAN- oder Verbindungsstatuslogik.
- Keine Änderung gespeicherter Geräte- oder Gruppendaten.
- Keine Änderung an Konfigurationsimport oder -export.
- Keine neue Farbeinstellung.
- Keine Änderung der Farbpalette aus Issue 073.

## Architekturhinweise

- Bestehende Compose-Komponenten für Statuspunkte wiederverwenden oder gezielt
  erweitern.
- Größen, Rahmenstärke und Pulsfarben möglichst zentral halten, damit Geräte- und
  Gruppenkacheln konsistent bleiben.
- Keine Business-Logik in Composables verschieben.
- Statusfarbe, Rahmenfarbe und Kachelhintergrund klar getrennt behandeln.
- Falls die Pulsanimation bereits zentral implementiert ist, nur dort anpassen.

## Akzeptanzkriterien

- [ ] Der Rahmen um den Statuspunkt ist sichtbar, aber weniger dominant als bisher.
- [ ] Der Statuspunkt ist insgesamt etwas größer als bisher.
- [ ] Der innere farbige Kreis ist größer und seine Farbe besser erkennbar.
- [ ] Auf farbigen Kacheln hebt sich der Rahmen weiterhin zuverlässig vom
      Kachelhintergrund ab.
- [ ] Nach einer ausgeführten Aktion pulsiert der innere Kreis zwischen
      Statusfarbe und Rahmenfarbe.
- [ ] Der innere Kreis pulsiert nicht mehr gegen die Kachelhintergrundfarbe.
- [ ] Geräte- und Gruppenkacheln verhalten sich optisch konsistent.
- [ ] Darstellung funktioniert im hellen und dunklen Theme.
- [ ] Darstellung bleibt bei größerer Android-Schriftgröße erkennbar und
      bedienbar.
- [ ] Keine Änderung an Import, Export oder gespeicherten Konfigurationsdaten.

## Testhinweise

- Gerät ohne gewählte Kachelfarbe anzeigen.
- Gerät mit gewählter Kachelfarbe anzeigen.
- Schaltgruppe ohne gewählte Kachelfarbe anzeigen.
- Schaltgruppe mit gewählter Kachelfarbe anzeigen.
- Erfolgreiche Aktion ausführen und Pulsieren beobachten.
- Fehlgeschlagene Aktion ausführen und Pulsieren beziehungsweise Statusfarbe
  beobachten.
- Helles und dunkles Theme prüfen.
- Dashboard-Liste und Dashboard-Widgetdarstellung prüfen.
- Größere Android-Schriftgröße prüfen.
