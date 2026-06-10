# Issue #2: Dashboard Device List

## Ziel

Das Dashboard zeigt die konfigurierten Geräte als Liste an.

Jedes Gerät erhält eine Karte mit Gerätenamen und Aktionsbutton.

## Anforderungen

- Geräte aus dem DeviceRepository laden
- Geräte nach `sortOrder` sortieren
- Geräte als Cards anzeigen
- Aktionsbutton mit `actionLabel` anzeigen
- Empty State anzeigen, wenn keine Geräte vorhanden sind
- Button-Klick zunächst nur mit Toast, Snackbar oder Log quittieren

## Nicht Bestandteil

- WLAN-Verbindung
- HTTP/API-Aufruf
- echte Speicherung
- Gerätebearbeitung
- Drag & Drop Sortierung

## Akzeptanzkriterien

- [ ] Dashboard zeigt Geräte als Liste
- [ ] Geräte werden nach `sortOrder` sortiert
- [ ] Jede Gerätekarte zeigt Name und Button
- [ ] Empty State vorhanden
- [ ] Button reagiert sichtbar auf Klick
- [ ] Build erfolgreich
- [ ] installDebug erfolgreich
