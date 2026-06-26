# AI Handoff

Stand: 26. Juni 2026

## Aktueller Stand

Zuletzt lokal abgeschlossen:

- Issue 061 „Import Replace Preserves Wizard State“
- Branch: `import-replace-preserve-wizard-state`
- GitHub-Issue: #138
- Duplikat: #139 geschlossen
- Pull Request: #140
- Merge-Commit: `92a9b17`
- Beim Import mit „Alles ersetzen“ bleibt der bestehende Zustand des
  Einrichtungs-Assistenten unverändert.
- `showSetupWizardOnStart=false` bleibt nach Replace-Import `false`;
  `showSetupWizardOnStart=true` bleibt `true`; Merge-Import verändert den
  Wizard-State ebenfalls nicht.
- Die Workflow-Dokumentation schreibt jetzt ausdrücklich vor, dass vor jedem
  `gh issue create` GitHub lesend auf ein passendes bestehendes Issue geprüft
  werden muss, auch wenn ein Startbefehl `gh issue create` enthält.
- Container-Prüfungen erfolgreich:
  - `./gradlew lintDebug`
  - `./gradlew testDebugUnitTest`
- Host-Build, Installation und Tests wurden vom Benutzer
  bestätigt:
  - `./gradlew clean assembleDebug`
  - `./gradlew installDebug`
- `docs/issues/061-import-replace-preserve-wizard-state.md` und
  `docs/issues/overview.txt` sind lokal auf `abgeschlossen` gesetzt.
- Nächstes offenes Issue nach `docs/issues/overview.txt`: Issue 032
  „Room Schema And Migration Test Coverage“.

Zuletzt abgeschlossen:

- Issue 022 „Request Body And Content-Type Support“
- GitHub-Issue: #127
- Pull Request: #128
- Merge-Commit: `173bfba`

## Start für nächste Codex-Session

1. `AGENTS.md` lesen.
2. `AI_HANDOFF.md` lesen.
3. `AI_SESSION_PROMPT.md` als wiederverwendbare Startvorlage beachten.
4. Für Issue-Arbeit die konkrete Datei unter `docs/issues` lesen.
5. Bei Reihenfolge/Status `docs/issues/overview.txt` lesen.

## Workflow-Erinnerung

- `docs/issues/overview.txt` ist führend für Issue-Status, Priorität und Reihenfolge.
- Ohne ausdrückliche Anweisung nicht committen, pushen, PR erstellen, mergen, GitHub-Issue schließen oder Branch löschen.
- Bei neuen oder geänderten Funktionen Hilfe-, Info- und Tooltip-Texte prüfen.
- Host-Build und Installation werden durch den Benutzer bestätigt:
  - `./gradlew clean assembleDebug`
  - `./gradlew installDebug`
