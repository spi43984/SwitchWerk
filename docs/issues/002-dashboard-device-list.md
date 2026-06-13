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

- [x] Dashboard zeigt Geräte als Liste
- [x] Geräte werden nach `sortOrder` sortiert
- [x] Jede Gerätekarte zeigt Name und Button
- [x] Empty State vorhanden
- [x] Button reagiert sichtbar auf Klick
- [x] Build erfolgreich
- [x] installDebug erfolgreich
