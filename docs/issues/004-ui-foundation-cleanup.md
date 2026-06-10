# Issue #4: UI Foundation Cleanup

## Ziel

Die App soll Systemleisten korrekt berücksichtigen und Inhalte nicht mehr unter Statusleiste oder Navigationsleiste zeichnen.

## Hintergrund

Die App verwendet `enableEdgeToEdge()`. Das ist modern, erfordert aber, dass Compose-Screens Insets berücksichtigen. Ohne passende Insets oder Systemleisten-Styles können Statusleiste, Uhr, WLAN- und Akkuanzeige schlecht sichtbar sein oder von App-Inhalten überdeckt werden.

## Scope

- Dashboard berücksichtigt Statusleiste und Navigationsleiste.
- SettingsScreen berücksichtigt Statusleiste und Navigationsleiste.
- Inhalte liegen nicht hinter Systemleisten.
- Systemleisten-Symbole sind auf hellem Hintergrund sichtbar.
- Bestehende Navigation bleibt erhalten.
- Bestehende Dashboard-Geräteliste bleibt erhalten.
- Bestehender SettingsScreen bleibt erhalten.

## Nicht Bestandteil

- Neue Navigation-Library
- Persistenz
- Geräteverwaltung
- WLAN-Profilverwaltung
- WLAN-Verbindung
- HTTP-Aufrufe

## Akzeptanzkriterien

- [ ] Statusleiste wird nicht überdeckt
- [ ] Navigationsleiste wird nicht überdeckt
- [ ] Statusleisten-Symbole sind sichtbar
- [ ] Dashboard bleibt nutzbar
- [ ] SettingsScreen bleibt nutzbar
- [ ] Navigation Dashboard zu Einstellungen und zurück funktioniert weiterhin
- [ ] Build erfolgreich
- [ ] installDebug erfolgreich
