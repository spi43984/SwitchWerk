# Issue #8: UI Foundation Cleanup

## Ziel

Die App soll Systemleisten korrekt berücksichtigen und Inhalte nicht mehr unter Statusleiste oder Navigationsleiste zeichnen.

## Hintergrund

Die App verwendet `enableEdgeToEdge()`. Das ist modern, erfordert aber, dass Compose-Screens Insets berücksichtigen. Aktuell können Statusleiste, Uhr, WLAN- und Akkuanzeige von App-Inhalten überdeckt werden.

## Anforderungen

### Safe Area / Insets

- Dashboard berücksichtigt Statusleiste und Navigationsleiste.
- SettingsScreen berücksichtigt Statusleiste und Navigationsleiste.
- Inhalte liegen nicht hinter Systemleisten.

### Konsistente Screen-Struktur

- Gemeinsamer äußerer Screen-Aufbau mit `safeDrawingPadding()` oder gleichwertiger Lösung.
- Bestehende Navigation bleibt erhalten.
- Bestehende Dashboard-Geräteliste bleibt erhalten.
- Bestehender SettingsScreen bleibt erhalten.

## Nicht Bestandteil dieses Issues

- Neue Navigation-Library
- Persistenz
- Geräteverwaltung
- WLAN-Profilverwaltung

## Akzeptanzkriterien

- [ ] Statusleiste wird nicht überdeckt
- [ ] Navigationsleiste wird nicht überdeckt
- [ ] Dashboard bleibt nutzbar
- [ ] SettingsScreen bleibt nutzbar
- [ ] Navigation Dashboard zu Einstellungen und zurück funktioniert weiterhin
- [ ] Build erfolgreich
- [ ] installDebug erfolgreich

## Definition of Done

- App startet
- Inhalte sind unterhalb der Statusleiste sichtbar
- Inhalte sind oberhalb der Navigationsleiste sichtbar
- GitHub Action erfolgreich
