# AI Handoff

Stand: 25. Juni 2026

## Aktueller Stand

Keine aktive Implementierung im Codex-Kontext.

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
