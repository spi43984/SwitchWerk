# AI Handoff

Stand: 26. Juni 2026

## Aktueller Stand

Zuletzt lokal abgeschlossen:

- Issue 059 „Export Config Reset Sensitive Toggles“
- Branch: `export-config-reset-sensitive-toggles`
- GitHub-Issue: #134
- Der Export-Schalter **Passwörter einschließen** wird als One-Shot-Option
  behandelt und nach Exportstart, Dateiauswahl-Abbruch, Warndialog-Abbruch oder
  Fehlerpfad wieder deaktiviert.
- Der Import-Schalter **Passwörter importieren** wurde als weiterer
  sicherheitskritischer One-Shot-Schalter geprüft und nach Import oder Abbruch
  ebenfalls zurückgesetzt.
- Hilfe- und Info-Texte sowie `CODE_STYLE.md` und `SECURITY.md` dokumentieren
  das One-Shot-Prinzip.
- Container-Prüfungen erfolgreich:
  - `./gradlew lintDebug`
  - `./gradlew testDebugUnitTest`
- Host-Build und Installation wurden vom Benutzer bestätigt:
  - `./gradlew clean assembleDebug`
  - `./gradlew installDebug`
- `docs/issues/059-export-config-reset-sensitive-toggles.md` und
  `docs/issues/overview.txt` sind lokal auf `abgeschlossen` gesetzt.
- Nächstes offenes Issue nach `docs/issues/overview.txt`: Issue 060
  „Scrollbares Hamburger-Menü bei Platzmangel“.

Zuletzt abgeschlossen:

- Issue 022 „Request Body And Content-Type Support“
- GitHub-Issue: #127
- Pull Request: #128
- Merge-Commit: `173bfba`

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
