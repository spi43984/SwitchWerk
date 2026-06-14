# Issue #25: Dashboard Widget Layout

## Ziel

Das Dashboard soll alternativ zur bisherigen Listenansicht eine kompakte Widget-Ansicht unterstuetzen.

Die Widget-Ansicht ermoeglicht die Darstellung mehrerer Geraete nebeneinander, aehnlich zu Startbildschirm-Widgets.

Dadurch koennen auf kleinen Displays mehr Geraete gleichzeitig sichtbar sein, ohne auf die direkte Bedienbarkeit verzichten zu muessen.

## Hintergrund

Bei vielen Geraeten wird die aktuelle Listenansicht schnell lang und unuebersichtlich.

Eine zweispaltige Widget-Ansicht erhoeht die Uebersichtlichkeit und reduziert den Scrollbedarf.

## Scope

### Dashboard Layout

- Umschaltbare Darstellung:
  - Listenansicht
  - Widget-Ansicht
- Auswahl wird dauerhaft gespeichert
- Darstellung wird beim App-Start wiederhergestellt

### Dashboard Schnellumschalter

- Listenansicht und Widget-Ansicht koennen direkt im Dashboard umgeschaltet werden
- Umschaltung erfolgt sofort ohne App-Neustart
- Die zuletzt gewaehlte Ansicht wird gespeichert

### Widget-Ansicht

- Geraete werden in einem Grid dargestellt
- Standardmaessig zweispaltig
- Adaptive Compose-Loesung verwenden
- Auf schmalen Displays darf automatisch auf eine Spalte gewechselt werden

### Widget-Inhalt

Jedes Geraete-Widget zeigt:

- Geraetename
- Geraeteaktion
- Aktionsbutton
- Statusbereich

### Statusbereich

Der Statusbereich zeigt kurze verstaendliche Meldungen:

- Bereit
- Verbinde WLAN...
- WLAN verbunden
- HTTP GET...
- HTTP POST...
- Erfolgreich
- Fehler

### Import / Export

Die Dashboard-Darstellung wird Bestandteil des Konfigurationsexports.

Export:

- aktuelle Dashboard-Darstellung wird exportiert

Import:

- falls die Einstellung vorhanden ist, wird sie uebernommen
- falls die Einstellung fehlt, bleibt die aktuelle Einstellung des Anwenders unveraendert

### Bedienung

- Aktionsbutton bleibt jederzeit sichtbar
- Widgets bleiben auch bei langen Geraetenamen nutzbar
- Bestehende Geraeteaktionen bleiben unveraendert

## Beispiel

Ein Widget enthaelt oben den Geraetenamen, in der Mitte den Aktionsbutton und unten eine kurze Statuszeile.

Beispiel fuer ein einzelnes Widget:

```text
+--------------------+
| Garagentor         |
|                    |
| [ Schalten ]       |
|                    |
| Erfolgreich        |
+--------------------+
```

Zwei Widgets nebeneinander:

```text
+------------+ +------------+
| Tor        | | Licht      |
| [Schalten] | | [Schalten] |
| Bereit     | | Bereit     |
+------------+ +------------+
```

## Nicht im Scope

- Geraetegruppierung
- Dashboard-Seiten
- Widget-Groessenanpassung
- Homescreen-Widgets von Android
- Drag & Drop Sortierung (Issue 014)
- Detailprotokollierung (Issue 023)

## Akzeptanzkriterien

- [ ] Listenansicht weiterhin verfuegbar
- [ ] Widget-Ansicht verfuegbar
- [ ] Umschaltung zwischen Liste und Widgets direkt im Dashboard moeglich
- [ ] Umschaltung erfolgt sofort ohne App-Neustart
- [ ] Auswahl wird gespeichert
- [ ] Auswahl bleibt nach App-Neustart erhalten
- [ ] Dashboard-Darstellung wird exportiert
- [ ] Vorhandene Einstellung wird beim Import uebernommen
- [ ] Fehlende Einstellung ueberschreibt bestehende Benutzereinstellung nicht
- [ ] Mehrere Geraete koennen nebeneinander dargestellt werden
- [ ] Aktionsbutton ist jederzeit sichtbar
- [ ] Statusbereich zeigt aktuelle Aktionen an
- [ ] Bedienung auf kleinen Displays bleibt nutzbar
- [ ] Build erfolgreich

## Testhinweise

- Wechsel zwischen Listen- und Widget-Ansicht
- App-Neustart nach Layoutwechsel
- Mehrere Geraete im Dashboard
- Lange Geraetenamen
- Erfolgreiche Geraeteaktion
- Fehlerhafte Geraeteaktion
- Portrait-Modus
- Landscape-Modus
- Export mit Dashboard-Einstellung
- Import mit Dashboard-Einstellung
- Import einer aelteren Konfiguration ohne Dashboard-Einstellung
