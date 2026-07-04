# Issue 068: GUI-Navigation und Konfiguration vereinfachen

## Metadaten

- Status: Abgeschlossen
- Priorität: P0
- Typ: UX / UI
- Bereich: Einrichtungs-Assistent / Hilfe / Über SwitchWerk / Einstellungen / Backup / Import / Übersetzungen

## Ziel

Die Navigation und Orientierung in der App soll für Anwender klarer werden.

Insbesondere sollen Einrichtungs-Assistent, Hilfe, Über-SwitchWerk-Text, Einstellungen-Navigation sowie Import und Export so überarbeitet werden, dass wichtige Aktionen besser sichtbar sind und Anwender schneller verstehen, ob sie eine bestehende Konfiguration importieren oder Geräte selbst einrichten möchten.

## Hintergrund

Der Einrichtungs-Assistent ist aktuell lang. Anwender übersehen leicht, dass sie nach unten scrollen müssen, um wichtige Aktionen wie den Import einer bestehenden Konfiguration zu finden.

Zusätzlich ist das Menü `Backup` fachlich breiter, weil es sowohl Export als auch Import von Konfigurationen enthält. Die Bezeichnung und die optische Gliederung sollen deshalb klarer werden.

Auch horizontale scrollbare Menüs werden teilweise übersehen. Sichtbare Scrollhinweise sollen die Bedienbarkeit verbessern.

## Scope

### Einrichtungs-Assistent

- Den langen Einführungstext deutlich kürzen.
- Im Assistenten nur den ersten Satz der Einführung belassen.
- Nach dem ersten Satz einen Button `Hilfe anzeigen` ergänzen.
- Der Button `Hilfe anzeigen` öffnet die Hilfe.
- Danach optisch trennen.
- Abschnitt `Möchtest Du eine bestehende Konfiguration importieren?` ergänzen.
- In diesem Abschnitt einen Button `Konfiguration importieren` anzeigen.
- Der Button öffnet den Bereich für Konfigurationen beziehungsweise den bisherigen Backup-Bereich.
- Danach optisch trennen.
- Abschnitt `Möchtest Du selbst Geräte einrichten?` ergänzen.
- In diesem Abschnitt folgende Buttons anzeigen:
  - `1. Einrichtung WLAN-Profile`
  - `2. Einrichtung Geräte`
  - `3. Dashboard`
- Die Buttons öffnen die jeweils passenden Bereiche.

### Über SwitchWerk

- Den ersten Absatz der bisherigen Einführung aus dem Einrichtungs-Assistenten nach `Über SwitchWerk` übernehmen.
- Der Text beginnt mit `SwitchWerk schaltet...` und endet mit dem Hinweis auf Internetforen.
- Den übernommenen Text mit dem bestehenden Über-SwitchWerk-Text sinnvoll zusammenfassen.
- Dopplungen vermeiden.
- Deutsch und Englisch konsistent pflegen.

### Hilfe

- Die bisherige Aufzählung `1.`, `2.`, `3.` aus dem Einrichtungs-Assistenten in die Hilfe übernehmen.
- Den Hinweis `Die farbigen...` ebenfalls in die Hilfe übernehmen.
- Die übernommenen Inhalte mit dem bestehenden Hilfetext sinnvoll zusammenfassen.
- Die Hilfe soll die neue Navigation und die neuen Bezeichnungen erklären.
- Deutsch und Englisch konsistent pflegen.

### Menübezeichnung Backup

- Die Menübezeichnung `Backup` in `Konfigurationen` ändern.
- Alle sichtbaren Texte, Hilfe-Texte, i-Texte und Übersetzungen auf die neue Bezeichnung prüfen.
- Technische interne Namen müssen nicht umbenannt werden, sofern dies unnötige Risiken oder großen Umbau verursachen würde.

### Horizontales Einstellungen-Menü

- Das horizontale Menü unter Einstellungen mit Bereichen wie `WLAN-Profile`, `Geräte`, `System` und bisher `Backup` bleibt horizontal scrollbar.
- Links und rechts kleine Scrollpfeile anzeigen, wenn links oder rechts weitere Menünamen vorhanden sind.
- Die Darstellung soll sich an den bestehenden Scrollpfeilen in der Liste zugeordneter WLAN-Profile bei Geräten orientieren.
- Die Pfeile nur anzeigen, wenn tatsächlich weiterer Inhalt in die jeweilige Richtung vorhanden ist.
- Die Lösung muss im Portrait- und Landscape-Modus funktionieren.
- Die Lösung muss bei größerer Android-Schriftgröße bedienbar bleiben.

### Konfigurationen: Import und Export

- Im bisherigen Backup-Bereich die Bereiche Export und Import klarer optisch voneinander trennen.
- Es muss eindeutig sein, welche Option zu Export und welche zu Import gehört.
- Die Schalter und Texte für Passwörter klar benennen:
  - `Passwörter exportieren`
  - `Passwörter importieren`
- Verwechslungen wie `Passwörter einschließen` ohne klaren Bezug vermeiden.

### Dreifachschalter für Passwort-Optionen

- `Passwörter exportieren` als Dreifachschalter umsetzen.
- `Passwörter importieren` als Dreifachschalter umsetzen.
- Standardposition ist die Mitte.
- In der Mittelposition ist noch keine Entscheidung getroffen.
- Solange der jeweilige Dreifachschalter in der Mittelposition steht, ist der zugehörige nächste Button deaktiviert:
  - `Konfiguration exportieren`
  - `Importieren`
- Linke Position bedeutet `ohne Passwörter`.
- Rechte Position bedeutet `mit Passwörtern`.
- Eine kurze Hilfe direkt beim Schalter erklärt die drei Zustände.
- Der sichtbare Status des Schalters muss klar anzeigen, welche Auswahl aktiv ist.
- Nach Abschluss, Abbruch oder Fehler einer Import- oder Exportaktion muss die sicherheitskritische Passwort-Auswahl wieder auf den sicheren Default zurückgesetzt werden.

### Dialog Konfiguration importieren

- Im Dialog `Konfiguration importieren` den nächsten erforderlichen Schritt langsam blinken oder anderweitig sehr deutlich hervorheben.
- Die Hervorhebung soll in dieser Reihenfolge durch die Import-Schritte führen:
  1. Auswahlmöglichkeit `Datei importieren`, `URL importieren` oder `QR-Code`
  2. Button `Konfiguration laden`
  3. Auswahlbereich `Ergänzen/überschreiben` und `Alles ersetzen`
  4. Entscheidung `Passwörter importieren`
  5. Buttons `Importieren` und `Abbrechen`
- Die Hervorhebung darf nicht hektisch wirken.
- Die Hervorhebung muss barrierearm sein und darf die Bedienung nicht stören.
- `Abbrechen` bleibt als sichere Abbruchaktion rechts, sofern in diesem Dialog eine Sicherheitsabfrage oder kritische Aktion betroffen ist.

### Hilfe-, Info- und i-Texte

- Alle Hilfe-, Info- und i-Texte auf neue Bezeichnungen prüfen.
- Inkonsistenzen zwischen `Backup`, `Konfigurationen`, Import, Export und Einrichtungs-Assistent bereinigen.
- Texte so formulieren, dass technisch weniger versierte Anwender die nächsten Schritte verstehen.

### Übersetzungen

- Deutsche und englische String-Ressourcen anpassen.
- Alle neuen oder geänderten UI-Texte übersetzen.
- Terminologie konsistent halten:
  - Deutsch: `Konfigurationen`, `Konfiguration importieren`, `Konfiguration exportieren`, `WLAN-Profile`
  - Englisch sinngemäß konsistent.

## Nicht im Scope

- Keine Änderung am eigentlichen Import-/Export-Dateiformat.
- Keine Änderung an gespeicherten Konfigurationsdaten.
- Keine Änderung an WLAN-Verbindungslogik.
- Keine Änderung an Geräteaktionen oder HTTP/RPC-Aufrufen.
- Keine neue Cloud-Abhängigkeit.
- Keine neue externe Tracking-, Analytics- oder Account-Funktion.
- Keine vollständige Umbenennung interner Klassen oder Dateien nur wegen der sichtbaren Menübezeichnung, sofern nicht fachlich nötig.

## Architekturhinweise

- Bestehende Compose- und Material-3-Struktur beibehalten.
- Gemeinsame Komponenten für Tabs, Scrollhinweise, Dialoge, Buttons und Hilfe-Texte bevorzugt zentral erweitern.
- Business-Logik nicht in Composables verschieben.
- UI-State und Benutzeraktionen über bestehende ViewModel-Struktur führen.
- Bestehende Packages, Namenskonventionen und Komponenten wiederverwenden.
- Sicherheitskritische One-Shot-Auswahlen zu Passwörtern müssen nach Aktion, Abbruch oder Fehler zurückgesetzt werden.
- Keine sensiblen Daten, Passwörter, Tokens, realen Hostnamen, realen SSIDs oder lokalen IP-Adressen in Code, Logs, Tests oder Dokumentation schreiben.
- Sichtbare Texte in Android-String-Ressourcen pflegen.
- Deutsch und Englisch konsistent halten.

## Akzeptanzkriterien

- [x] Der Einrichtungs-Assistent zeigt nur noch den ersten Satz der Einführung direkt an.
- [x] Der Einrichtungs-Assistent enthält einen Button `Hilfe anzeigen`.
- [x] `Hilfe anzeigen` öffnet die Hilfe.
- [x] Der Einrichtungs-Assistent enthält einen klar getrennten Abschnitt zum Import einer bestehenden Konfiguration.
- [x] Der Abschnitt zum Import enthält einen Button `Konfiguration importieren`.
- [x] `Konfiguration importieren` öffnet den Bereich `Konfigurationen`.
- [x] Der Einrichtungs-Assistent enthält einen klar getrennten Abschnitt zum selbstständigen Einrichten.
- [x] Die Buttons `1. Einrichtung WLAN-Profile`, `2. Einrichtung Geräte` und `3. Dashboard` öffnen die passenden Bereiche.
- [x] Der längere Einführungstext ist in `Über SwitchWerk` integriert.
- [x] Die bisherige Schritt-Aufzählung und der Hinweis `Die farbigen...` sind in die Hilfe integriert.
- [x] Die sichtbare Menübezeichnung `Backup` wurde zu `Konfigurationen` geändert.
- [x] Das horizontale Einstellungen-Menü zeigt links/rechts Scrollpfeile, wenn weitere Menüpunkte außerhalb des sichtbaren Bereichs liegen.
- [x] Die Scrollpfeile verschwinden, wenn in der jeweiligen Richtung kein weiterer Inhalt vorhanden ist.
- [x] Import und Export im Bereich `Konfigurationen` sind optisch klar getrennt.
- [x] `Passwörter exportieren` ist eindeutig dem Export zugeordnet.
- [x] `Passwörter importieren` ist eindeutig dem Import zugeordnet.
- [x] Die Passwort-Optionen für Import und Export sind als Dreifachschalter umgesetzt.
- [x] Die Mittelposition ist Default und deaktiviert den zugehörigen nächsten Aktionsbutton.
- [x] Linke Position bedeutet `ohne Passwörter`.
- [x] Rechte Position bedeutet `mit Passwörtern`.
- [x] Die Bedeutung der Schalterstellungen wird direkt sichtbar erklärt.
- [x] Nach Abschluss, Abbruch oder Fehler wird die jeweilige Passwort-Auswahl auf Default zurückgesetzt.
- [x] Im Dialog `Konfiguration importieren` wird der nächste erforderliche Schritt langsam und verständlich hervorgehoben.
- [x] Die Schritt-Hervorhebung führt durch Quelle, Laden, Importmodus, Passwortentscheidung und Abschlussbuttons.
- [x] Die Darstellung funktioniert im Portrait-Modus.
- [x] Die Darstellung funktioniert im Landscape-Modus.
- [x] Die Darstellung bleibt bei größerer Android-Schriftgröße bedienbar.
- [x] Hilfe-, Info- und i-Texte verwenden die neuen Bezeichnungen konsistent.
- [x] Deutsche und englische Übersetzungen sind vollständig und konsistent gepflegt.
- [x] Bestehende Sicherheitsregeln für Abbruchaktionen bleiben erhalten.

## Abschluss

- Implementiert in Branch `docs/issue-068-gui-navigation-konfiguration`.
- Pull Request: #162 `GUI-Navigation und Konfiguration vereinfachen`.
- GitHub-Issue: #161.
- Einrichtungs-Assistent gekürzt und mit direkten Einstiegsaktionen für Hilfe, Konfigurationen, WLAN-Profile, Geräte und Dashboard versehen.
- Sichtbare Bezeichnung `Backup` zu `Konfigurationen` geändert; interne technische Namen blieben unverändert.
- Import und Export im Bereich `Konfigurationen` optisch getrennt.
- Passwortauswahl für Export und Import als Dreifachauswahl umgesetzt; Mittelposition deaktiviert die jeweilige Aktion.
- Passwortauswahl wird nach Abschluss, Abbruch oder Fehler auf den sicheren Default zurückgesetzt.
- Importdialog führt mit ruhiger pulsierender Hervorhebung durch Quelle, Laden, Importmodus, Passwortentscheidung und Zusammenfassung.
- Hilfe-, Info- und i-Texte sowie deutsche und englische Übersetzungen wurden geprüft und konsistent aktualisiert.
- Container-Prüfungen: `./gradlew :app:compileDebugKotlin`, `./gradlew :app:lintDebug`.
- GitHub Actions in PR #162 erfolgreich: `build`, `submit-gradle`.
- Host-Prüfungen laut Benutzerrückmeldung erfolgreich: Build und Tests.

## Testhinweise

- App auf Deutsch starten und Einrichtungs-Assistent öffnen.
- Prüfen, dass nur der kurze Einstiegssatz sichtbar ist.
- Button `Hilfe anzeigen` aus dem Assistenten testen.
- Button `Konfiguration importieren` aus dem Assistenten testen.
- Buttons `1. Einrichtung WLAN-Profile`, `2. Einrichtung Geräte` und `3. Dashboard` aus dem Assistenten testen.
- `Über SwitchWerk` öffnen und neuen zusammengefassten Text prüfen.
- Hilfe öffnen und neue Schrittbeschreibung prüfen.
- Einstellungen öffnen und prüfen, dass der Bereich `Konfigurationen` statt `Backup` sichtbar ist.
- Horizontales Einstellungen-Menü im Portrait-Modus auf Scrollpfeile prüfen.
- Horizontales Einstellungen-Menü im Landscape-Modus auf Scrollpfeile prüfen.
- Darstellung mit größerer Android-Schriftgröße prüfen.
- Exportbereich öffnen und optische Trennung zum Importbereich prüfen.
- Importbereich öffnen und optische Trennung zum Exportbereich prüfen.
- Dreifachschalter `Passwörter exportieren` in allen drei Positionen prüfen.
- Dreifachschalter `Passwörter importieren` in allen drei Positionen prüfen.
- Prüfen, dass `Konfiguration exportieren` in Default-Mittelposition deaktiviert ist.
- Prüfen, dass `Importieren` in Default-Mittelposition deaktiviert ist.
- Export durchführen, abbrechen und Fehlerfall prüfen; Passwort-Auswahl muss zurückgesetzt werden.
- Import durchführen, abbrechen und Fehlerfall prüfen; Passwort-Auswahl muss zurückgesetzt werden.
- Dialog `Konfiguration importieren` öffnen und Schritt-Hervorhebung vollständig prüfen.
- Englische App-Sprache aktivieren und alle betroffenen Texte prüfen.
