# Issue 038: Dialog Keyboard Handling

## Metadaten

- Status: Abgeschlossen
- Priorität: P0
- Typ: UX / GUI
- Bereich: Dialoge / Eingabeformulare

## Ziel

Eingabedialoge sollen auch bei geöffneter Android-Tastatur vollständig bedienbar bleiben.

Der Anwender soll nach einer Eingabe nicht erst die Tastatur manuell einklappen müssen, um Schaltflächen wie `Speichern`, `Abbrechen` oder `Löschen` zu erreichen.

## Hintergrund

Aktuell kann die Software-Tastatur den unteren Bereich von Dialogen überlagern. Dadurch sind wichtige Aktionsschaltflächen teilweise nicht sichtbar oder schwer erreichbar.

Das betrifft insbesondere:

- kleine Displays
- große Schriftgrößen
- lange Formulare
- mehrere Eingabefelder innerhalb eines Dialogs

## Scope

### Keyboard-aware Dialog Layout

- Dialoge berücksichtigen die geöffnete Tastatur.
- Geeignete Compose-Mechanismen wie `imePadding`, `WindowInsets` oder eine gemeinsame Dialog-Basis werden geprüft.
- Die Lösung soll ohne neue Frameworks umgesetzt werden.

### Scrollbarer Dialoginhalt

- Formularinhalte in Dialogen werden bei Bedarf scrollbar.
- Eingabefelder bleiben auch bei geöffneter Tastatur erreichbar.
- Lange Dialoge dürfen nicht außerhalb des sichtbaren Bereichs abgeschnitten werden.

### Sichtbarer Aktionsbereich

- Der Aktionsbereich mit `Speichern`, `Abbrechen` und vergleichbaren Aktionen bleibt erreichbar.
- Bevorzugte UX:
  - Formularbereich scrollt
  - Aktionsbereich bleibt sichtbar oder wird zuverlässig über die Tastatur geschoben

### Begrenzte Dialoghöhe

- Formular-Dialoge verwenden eine maximale Höhe von ca. 80-85 % der verfügbaren Bildschirmhöhe.
- Die genaue technische Umsetzung bleibt der zentralen Dialog-Basis überlassen.
- Dialoge sollen auf kleinen Displays nicht den gesamten Bildschirm ausfüllen.
- Die Höhenbegrenzung soll zentral umgesetzt werden und nicht in jedem Dialog separat.

### Fokus- und Tastaturbedienung

- Die Tastaturaktion `Weiter` springt zum nächsten Eingabefeld.
- Die Tastaturaktion `Fertig` schließt die Eingabe sinnvoll ab.
- Optional kann beim letzten Feld direkt gespeichert werden, wenn dies fachlich eindeutig und sicher ist.

### Wiederverwendbare Lösung

- Die Lösung soll möglichst zentral umgesetzt werden.
- Bestehende Dialoge sollen konsistent angepasst werden.
- Neue Dialoge sollen die gleiche Basis verwenden können.

## Betroffene Dialoge

Zu prüfen sind insbesondere:

- WLAN-Profil-Dialoge
- Geräte-Dialoge
- Geräte-WLAN-Zuordnung
- Einstellungen
- Import-/Export-Dialoge
- zukünftige Formular-Dialoge

## Nicht im Scope

- Fachliche Änderungen an WLAN-, Geräte- oder Import-/Export-Logik
- Neue Eingabefelder
- Änderung der Datenmodelle
- Neue Berechtigungen
- Neue externe Abhängigkeiten

## Architekturhinweise

- Bestehende Compose- und Material-3-Struktur beibehalten.
- Falls sinnvoll, eine wiederverwendbare Komponente wie `KeyboardAwareDialog` oder eine gemeinsame Formular-Dialog-Basis einführen.
- Die gemeinsame Dialog-Basis soll neben Keyboard-Awareness optional auch die maximale Dialoghöhe zentral verwalten.
- UI-Zustand bleibt weiterhin im ViewModel beziehungsweise in bestehenden State-Haltern.
- Keine Logik in reine UI-Hilfskomponenten verschieben, die fachlich in ViewModels gehört.

## Akzeptanzkriterien

- [x] Die Tastatur verdeckt keine wichtigen Dialogaktionen mehr.
- [x] `Speichern` bleibt bei geöffneter Tastatur erreichbar.
- [x] `Abbrechen` bleibt bei geöffneter Tastatur erreichbar.
- [x] Dialoginhalte sind bei Bedarf scrollbar.
- [x] Formular-Dialoge sind auf ca. 80-85 % der verfügbaren Bildschirmhöhe begrenzt.
- [x] Die Lösung funktioniert auf kleinen Displays.
- [x] Die Lösung funktioniert bei großer Schriftgröße.
- [x] Fokuswechsel zwischen Eingabefeldern funktioniert sinnvoll.
- [x] Bestehende Dialogfunktionen bleiben unverändert.
- [x] Die Lösung wird konsistent für relevante Eingabedialoge verwendet.
- [x] Relevante UI- oder Screenshot-Tests sind ergänzt, sofern sinnvoll.
- [x] Build und Installation wurden auf dem Ubuntu-Host erfolgreich geprüft.

## Testhinweise

- Kleines Display beziehungsweise Emulator mit geringer Höhe
- Große Android-Schriftgröße
- Geöffnete Tastatur im ersten Eingabefeld
- Geöffnete Tastatur im letzten Eingabefeld
- Hochformat
- Querformat, falls unterstützt
- Dialog mit wenigen Feldern
- Dialog mit vielen Feldern
- Speichern bei geöffneter Tastatur
- Abbrechen bei geöffneter Tastatur
