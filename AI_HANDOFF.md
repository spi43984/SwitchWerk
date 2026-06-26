# AI Handoff

Stand: 26. Juni 2026

## Aktueller Stand

Zuletzt lokal abgeschlossen:

- Issue 058 „Setup Wizard / Einrichtungs-Assistent“
- Branch: `setup-wizard`
- GitHub-Issue: #131
- Setup-Wizard wird beim ersten Start angezeigt und über `AppSettings` /
  `AppSettingsRepository` persistent gesteuert.
- **Überspringen** schließt den Wizard nur für die laufende Sitzung.
- **Nicht mehr anzeigen** deaktiviert die automatische Anzeige dauerhaft.
- Replace-Import einer Konfiguration aktiviert die Wizard-Anzeige erneut.
- Verlinkte Wizard-Ziele öffnen Backup, WLAN-Profile, Geräte oder Dashboard;
  Zurück führt wieder zum Wizard, bis er über die unteren Buttons geschlossen wird.
- Die Wizard-Scrollposition wird beim Öffnen verlinkter Seiten wiederhergestellt.
- **Einrichtungs-Assistent erneut anzeigen** ist unter
  **Einstellungen → System** und in **Hilfe** verfügbar.
- Hilfe ist scrollbar.
- Container-Prüfungen erfolgreich:
  - `./gradlew lintDebug`
  - `./gradlew testDebugUnitTest`
- Host-Build und Installation müssen noch vom Benutzer bestätigt werden:
  - `./gradlew clean assembleDebug`
  - `./gradlew installDebug`
- `docs/issues/058-setup-wizard.md` und `docs/issues/overview.txt` sind lokal
  auf `abgeschlossen` gesetzt.
- Nächstes offenes Issue nach `docs/issues/overview.txt`: Issue 059
  „Export Config Reset Sensitive Toggles“.

Zuletzt abgeschlossen:

- Issue 022 „Request Body And Content-Type Support“
- GitHub-Issue: #127
- Pull Request: #128
- Merge-Commit: `173bfba`

## Wichtige offene Hinweise

- Issue 057 „Encrypted Storage Restore Start Crash“ war laut älterem Handoff lokal abgeschlossen und vom Benutzer bestätigt, aber möglicherweise noch nicht veröffentlicht. Status bei Bedarf in GitHub und `docs/issues/overview.txt` prüfen.
- Issue 019 „Configurable WiFi List Sorting“ war laut älterem Handoff lokal implementiert und bestätigt, aber möglicherweise noch nicht veröffentlicht. Status bei Bedarf in GitHub und `docs/issues/overview.txt` prüfen.

## Start für nächste Codex-Session

1. `AGENTS.md` lesen.
2. `AI_HANDOFF.md` lesen.
3. Für Issue-Arbeit die konkrete Datei unter `docs/issues` lesen.
4. Bei Reihenfolge/Status `docs/issues/overview.txt` lesen.

## Workflow-Erinnerung

- `docs/issues/overview.txt` ist führend für Issue-Status, Priorität und Reihenfolge.
- Ohne ausdrückliche Anweisung nicht committen, pushen, PR erstellen, mergen, GitHub-Issue schließen oder Branch löschen.
- Bei neuen oder geänderten Funktionen Hilfe-, Info- und Tooltip-Texte prüfen.
- Host-Build und Installation werden durch den Benutzer bestätigt:
  - `./gradlew clean assembleDebug`
  - `./gradlew installDebug`
