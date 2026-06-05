# Issue #4: Dashboard Device List

## Ziel

Das Dashboard soll die konfigurierten Geräte als Liste anzeigen.

Jedes Gerät erhält:

- Gerätenamen
- Aktions-Button mit der konfigurierten Beschriftung

Die Geräte werden aus dem bestehenden DeviceRepository geladen.

Es findet noch keine WLAN-Verbindung und kein HTTP-Aufruf statt.

## Anforderungen

- Geräte als Liste anzeigen
- Sortierung nach sortOrder
- Card pro Gerät
- Action-Button pro Gerät
- Empty State bei leerer Liste
- Button reagiert zunächst nur mit Toast/Snackbar/Log

## Nicht Bestandteil

- WLAN-Verbindung
- HTTP-Aufruf
- Einstellungen
- Import/Export
- Passwortspeicherung

## Akzeptanzkriterien

- [ ] Dashboard zeigt Geräte als Liste
- [ ] Geräte nach sortOrder sortiert
- [ ] Gerätekarte vorhanden
- [ ] Action-Button vorhanden
- [ ] Button reagiert auf Klick
- [ ] Empty State vorhanden
- [ ] Build erfolgreich
- [ ] installDebug erfolgreich
