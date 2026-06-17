# Issue #16: Edit Items by Name Click and Swipe Actions

## Ziel

Geräte und WLAN-Profile ohne dauerhaft sichtbare Aktionsbuttons bearbeiten und löschen.

Kurzer Klick auf den Namen bzw. Listeneintrag öffnet die Bearbeitung. Zusätzlich legt seitliches Wischen Aktionen frei. Der Swipe selbst führt keine Aktion aus. Bearbeiten oder Löschen erfolgt erst nach zusätzlichem Tipp auf die freigelegte Aktion.

## Scope

- Kurzer Klick auf Gerätenamen öffnet Gerätebearbeitung
- Kurzer Klick auf WLAN-Eintrag öffnet WLAN-Bearbeitung
- Seitliches Wischen eines Geräts legt die Aktionen `Bearbeiten` und `Löschen` frei
- Seitliches Wischen eines WLAN-Profils legt die Aktionen `Bearbeiten` und `Löschen` frei
- Der Swipe selbst bearbeitet und löscht nicht sofort
- Bearbeiten erfolgt nach zusätzlichem Tipp auf den freigelegten `Bearbeiten`-Button
- Löschen erfolgt erst nach zusätzlichem Tipp auf den freigelegten `Löschen`-Button
- Zurückwischen oder Tippen außerhalb der freigelegten Aktionen bricht die Aktionsauswahl ab
- Bleistift-Icons entfernen
- Mülleimer-Icons entfernen
- Keine dauerhaft sichtbaren Aktionsbuttons am rechten Listenrand

## Nicht im Scope

- Neue Dialog-Layouts
- Drag & Drop Sortierung
- Snackbar-Undo-Löschung
- Sofortiges Bearbeiten oder Löschen durch Swipe
- Mehrfachauswahl
- Änderung der Datenmodelle
- Änderung der Passwortspeicherung
- Änderung der Geräteaktion- oder WLAN-Verbindungslogik

## UI- und Sicherheitsregeln

- Gefährliche Aktionen werden nicht durch einen einzelnen Swipe ausgeführt
- Löschen benötigt zwei bewusste Schritte: Swipe zum Freilegen und Tipp auf `Löschen`
- Bearbeiten kann weiterhin direkt per kurzem Klick auf den Eintrag geöffnet werden
- Die freigelegte `Bearbeiten`-Aktion ist eine zusätzliche Bedienmöglichkeit für Nutzer, die Aktionen per Swipe erwarten
- Die sichere Abbruchmöglichkeit bleibt durch Zurückwischen oder Verlassen der freigelegten Aktionen erhalten
- Die bestehende UI-Regel für Sicherheitsdialoge bleibt unberührt, falls zusätzlich ein Dialog verwendet wird: gefährliche Aktion links, sichere Abbruchaktion rechts

## Akzeptanzkriterien

- [ ] Klick auf Gerätenamen öffnet Gerätebearbeitung
- [ ] Klick auf WLAN-Eintrag öffnet WLAN-Bearbeitung
- [ ] Bleistift-Icons sind entfernt
- [ ] Mülleimer-Icons sind entfernt
- [ ] Es sind keine dauerhaft sichtbaren Aktionsbuttons am rechten Listenrand vorhanden
- [ ] Swipe auf Gerät legt `Bearbeiten` und `Löschen` frei, führt aber keine Aktion sofort aus
- [ ] Swipe auf WLAN-Profil legt `Bearbeiten` und `Löschen` frei, führt aber keine Aktion sofort aus
- [ ] Bearbeiten erfolgt nach zusätzlichem Tipp auf den freigelegten `Bearbeiten`-Button
- [ ] Löschen erfolgt erst nach zusätzlichem Tipp auf den freigelegten `Löschen`-Button
- [ ] Zurückwischen oder Verlassen der freigelegten Aktionen bricht die Aktionsauswahl ab
- [ ] Geräte löschen bleibt möglich
- [ ] WLAN-Profile löschen bleibt möglich

## Testhinweise

- Gerät per kurzem Klick auf den Namen bearbeiten
- WLAN-Profil per kurzem Klick auf den Eintrag bearbeiten
- Gerät swipen und prüfen, dass `Bearbeiten` und `Löschen` sichtbar werden
- WLAN-Profil swipen und prüfen, dass `Bearbeiten` und `Löschen` sichtbar werden
- Gerät swipen und prüfen, dass noch nicht bearbeitet oder gelöscht wird
- WLAN-Profil swipen und prüfen, dass noch nicht bearbeitet oder gelöscht wird
- Gerät nach Swipe über freigelegten `Bearbeiten`-Button bearbeiten
- WLAN-Profil nach Swipe über freigelegten `Bearbeiten`-Button bearbeiten
- Gerät nach Swipe über freigelegten `Löschen`-Button löschen
- WLAN-Profil nach Swipe über freigelegten `Löschen`-Button löschen
- Nach Swipe zurückwischen und prüfen, dass keine Aktion ausgeführt wird
- Prüfen, dass keine Bearbeiten- oder Löschen-Icons mehr dauerhaft sichtbar sind
