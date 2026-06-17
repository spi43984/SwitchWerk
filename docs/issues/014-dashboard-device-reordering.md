# Issue #14: Dashboard Device Reordering

## Status

Abgeschlossen.

- GitHub-Issue: #30
- Implementierung: Geräte können im Dashboard per Hoch-/Runter-Aktionen sortiert werden.
- Drag & Drop wurde bewusst nicht in diesem Issue umgesetzt und bleibt für Issue 025 "Dashboard Widget Layout" vorbehalten.
- Host-Prüfung erfolgreich:
  - `./gradlew clean assembleDebug`
  - `./gradlew installDebug`

## Ziel
Geräte im Dashboard komfortabel sortieren.

## Scope
- Sortierung der Geräte im Dashboard
- Drag & Drop prüfen
- Reihenfolge dauerhaft in Room `sortOrder` speichern
- Geeignete Compose-Lösung oder Library bewerten

## Nicht im Scope
- Geräteverwaltung allgemein
- WLAN-Zuordnungen
- API-Aufrufe

## Akzeptanzkriterien
- [x] Geräte können im Dashboard umsortiert werden
- [x] Reihenfolge bleibt nach App-Neustart erhalten
- [x] Bedienung ist auf kleinen Displays gut nutzbar

## Hinweise

Die Umsetzung verwendet Pfeilaktionen statt Drag & Drop. Drag & Drop und getrennte Layout-/Sortierzustände pro Sicht werden in Issue 025 weiter geplant.