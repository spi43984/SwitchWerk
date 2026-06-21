# Issue #25: Dashboard Widget Layout

## Metadaten

- Status: Abgeschlossen
- Priorität: P1
- Typ: GUI / Dashboard

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

- kurze Ladeanzeige waehrend der Aktion
- kurze Erfolgsmeldung
- verstaendliche Fehlermeldung im Aktionsbereich
- anschliessende automatische Wiederherstellung von Aktionsbutton und
  Sortierbedienung

### Import / Export

Die Dashboard-Darstellung wird Bestandteil des Konfigurationsexports.

Export:

- aktuelle Dashboard-Darstellung wird exportiert

Import:

- falls die Einstellung vorhanden ist, wird sie uebernommen
- falls die Einstellung fehlt, bleibt die aktuelle Einstellung des Anwenders unveraendert

### Bedienung

- Aktionsbereich bleibt jederzeit sichtbar und hoehenstabil
- Widgets bleiben auch bei langen Geraetenamen nutzbar
- Bestehende Geraeteaktionen bleiben unveraendert

## Erweiterung

### Drag & Drop

Die in Issue 014 umgesetzte Pfeil-Sortierung bleibt bestehen.

Für das Dashboard-Layout soll zusätzlich geprüft werden:

- Drag & Drop für Widgets/Geräte
- freie Positionierung innerhalb des Dashboard-Layouts
- Speicherung der Layout-Positionen

### Getrennte Sortierungen pro Sicht

Die Reihenfolge soll nicht global gespeichert werden.

Jede Sicht verwaltet ihre eigene Reihenfolge bzw. ihr eigenes Layout:

- Dashboard-Sicht
- zukünftige alternative Ansichten

Beispiel:

- Dashboard: Tor 1, Tor 2, Licht
- Andere Sicht: Licht, Tor 2, Tor 1

Änderungen in einer Sicht dürfen die Reihenfolge anderer Sichten nicht beeinflussen.

### Architektur

Die aktuelle sortOrder aus Issue 014 ist die Dashboard-Reihenfolge.

Für spätere Layouts ist ein separates Layout-/View-abhängiges Positionsmodell vorzusehen, damit mehrere Ansichten unabhängig gespeichert werden können.

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

- [x] Listenansicht weiterhin verfuegbar
- [x] Widget-Ansicht verfuegbar
- [x] Umschaltung zwischen Liste und Widgets direkt im Dashboard moeglich
- [x] Umschaltung erfolgt sofort ohne App-Neustart
- [x] Auswahl wird gespeichert
- [x] Auswahl bleibt nach App-Neustart erhalten
- [x] Dashboard-Darstellung wird exportiert
- [x] Vorhandene Einstellung wird beim Import uebernommen
- [x] Fehlende Einstellung ueberschreibt bestehende Benutzereinstellung nicht
- [x] Mehrere Geraete koennen nebeneinander dargestellt werden
- [x] Aktionsbereich bleibt jederzeit sichtbar und hoehenstabil
- [x] Statusbereich zeigt aktuelle Aktionen an
- [x] Bedienung auf kleinen Displays bleibt nutzbar
- [x] Build erfolgreich

## Umsetzungsergebnis

- persistierbarer Dashboard-Modus `LIST` oder `WIDGETS`
- direkter Material-3-Umschalter im Dashboard-Kopf
- adaptives Compose-Grid mit gemeinsamer persistierter `sortOrder`
- kompakte Hoch-/Runter-Sortierung in Listen- und Widget-Ansicht
- stabile Kartenhoehen mit zeitlich begrenzten Erfolgs- und Fehlermeldungen
- kompakte Landscape-Kopfzeile; Aktionsdetails werden dort nur visuell
  ausgeblendet, ohne die gespeicherte Einstellung zu veraendern
- Export der Dashboard-Darstellung und rueckwaertskompatibler Import, der bei
  fehlender Einstellung den aktuellen Benutzerwert beibehaelt
- keine neue Netzwerk-, Cloud-, Tracking- oder Analytics-Abhaengigkeit

Bestätigte Prüfungen:

```text
git diff --check
./gradlew testDebugUnitTest
./gradlew clean assembleDebug
./gradlew installDebug
manuelle Prüfung von Listen-/Widget-Umschaltung, Sortierung, Statusdarstellung,
Portrait- und Landscape-Layout
```

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
