# Issue #6: Settings Navigation Foundation

## Ziel

Die App soll eine einfache Navigation zwischen Dashboard und Einstellungen erhalten.

Dieses Issue erstellt nur die Navigationsgrundlage und einen ersten SettingsScreen. Es werden noch keine echten Einstellungen gespeichert oder bearbeitet.

## Hintergrund

Nach Issue #4 zeigt das Dashboard eine Geräteliste mit Aktionsbuttons.

Als nächster Schritt braucht SwitchWerk einen Einstieg in die Einstellungen, weil dort später WLAN-Profile, Geräte, Import/Export und QR-Code-Import verwaltet werden.

## Anforderungen

### Dashboard

Das Dashboard erhält eine sichtbare Möglichkeit, die Einstellungen zu öffnen.

### SettingsScreen

Platzhalterbereiche:

- WLAN-Profile
- Geräte
- Import / Export

### Navigation

Wechsel zwischen:

- Dashboard
- Einstellungen

Rücknavigation zum Dashboard möglich.

## Nicht Bestandteil

- WLAN-Profile bearbeiten
- Geräte bearbeiten
- Persistenz
- Passwortspeicherung
- QR-Code
- WLAN-Verbindung
- HTTP-Aufrufe

## Akzeptanzkriterien

- [ ] Dashboard enthält Einstieg in Einstellungen
- [ ] SettingsScreen vorhanden
- [ ] WLAN-Profile Bereich sichtbar
- [ ] Geräte Bereich sichtbar
- [ ] Import/Export Bereich sichtbar
- [ ] Rücknavigation möglich
- [ ] Build erfolgreich
- [ ] installDebug erfolgreich
