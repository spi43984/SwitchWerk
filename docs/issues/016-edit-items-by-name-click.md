# Issue #16: Edit Items by Name Click and Swipe Delete

## Ziel

Geräte und WLAN-Profile ohne dauerhaft sichtbare Aktionsbuttons bearbeiten und löschen.

Kurzer Klick auf den Namen bzw. Listeneintrag öffnet die Bearbeitung. Seitliches Wischen legt eine Löschaktion frei. Gelöscht wird erst nach zusätzlichem Tipp auf den freigelegten Löschen-Button.

## Scope

- Kurzer Klick auf Gerätenamen öffnet Gerätebearbeitung
- Kurzer Klick auf WLAN-Eintrag öffnet WLAN-Bearbeitung
- Seitliches Wischen eines Geräts legt die Löschaktion frei
- Seitliches Wischen eines WLAN-Profils legt die Löschaktion frei
- Der Swipe selbst löscht nicht sofort
- Löschen erfolgt erst nach zusätzlichem Tipp auf den freigelegten Löschen-Button
- Zurückwischen oder Tippen außerhalb der freigelegten Aktion bricht die Löschabsicht ab
- Bleistift-Icons entfernen
- Mülleimer-Icons entfernen

## Nicht im Scope

- Neue Dialog-Layouts
- Drag & Drop Sortierung
- Snackbar-Undo-Löschung
- Sofortiges Löschen durch Swipe
- Mehrfachauswahl
- Änderung der Datenmodelle
- Änderung der Passwortspeicherung
- Änderung der Geräteaktion- oder WLAN-Verbindungslogik

## UI- und Sicherheitsregeln

- Gefährliche Aktionen werden nicht durch einen einzelnen Swipe ausgeführt
- Löschen benötigt zwei bewusste Schritte: Swipe zum Freilegen und Tipp auf `Löschen`
- Die sichere Abbruchmöglichkeit bleibt durch Zurückwischen oder Verlassen der freigelegten Aktion erhalten
- Die bestehende UI-Regel für Sicherheitsdialoge bleibt unberührt, falls zusätzlich ein Dialog verwendet wird: gefährliche Aktion links, sichere Abbruchaktion rechts

## Akzeptanzkriterien

- [ ] Klick auf Gerätenamen öffnet Gerätebearbeitung
- [ ] Klick auf WLAN-Eintrag öffnet WLAN-Bearbeitung
- [ ] Bleistift-Icons sind entfernt
- [ ] Mülleimer-Icons sind entfernt
- [ ] Swipe auf Gerät legt `Löschen` frei, löscht aber nicht sofort
- [ ] Swipe auf WLAN-Profil legt `Löschen` frei, löscht aber nicht sofort
- [ ] Löschen erfolgt erst nach zusätzlichem Tipp auf den freigelegten `Löschen`-Button
- [ ] Zurückwischen oder Verlassen der freigelegten Aktion bricht die Löschabsicht ab
- [ ] Geräte löschen bleibt möglich
- [ ] WLAN-Profile löschen bleibt möglich

## Testhinweise

- Gerät per kurzem Klick auf den Namen bearbeiten
- WLAN-Profil per kurzem Klick auf den Eintrag bearbeiten
- Gerät swipen und prüfen, dass noch nicht gelöscht wird
- WLAN-Profil swipen und prüfen, dass noch nicht gelöscht wird
- Gerät nach Swipe über freigelegten `Löschen`-Button löschen
- WLAN-Profil nach Swipe über freigelegten `Löschen`-Button löschen
- Nach Swipe zurückwischen und prüfen, dass nicht gelöscht wird
- Prüfen, dass keine Bearbeiten- oder Löschen-Icons mehr dauerhaft sichtbar sind
