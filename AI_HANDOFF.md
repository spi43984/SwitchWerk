# AI Handoff

Stand: 5. Juli 2026

## Abgeschlossen

- Issue 078 „Release Notes mit GitHub Models vorbefüllen“
- GitHub-Issue: #176
- Commit auf `main`: `e3f383d`
- `scripts/release-github.sh` versucht vor dem verpflichtenden Editor-Schritt,
  über GitHub Models deutschsprachige Release Notes zu erzeugen.
- Der Vorschlag basiert auf Commit-Betreffs und `git diff --stat` seit dem
  vorherigen Release.
- Bei fehlendem `jq`, fehlendem `curl`, fehlendem `gh auth token`,
  fehlender Models-Berechtigung oder API-/Parsing-Fehlern fällt das Skript auf
  den manuellen Platzhalter zurück.
- Tokens und vollständige API-Antworten werden nicht geloggt.
- Changelog-Link und bestehende Platzhalterprüfung bleiben erhalten.
- Lokale Prüfungen waren erfolgreich:
  - `bash -n scripts/release-github.sh`
  - `shellcheck scripts/release-github.sh`

## Aktueller Arbeitsbaum

- `app/build.gradle.kts` enthält lokale Versionsänderungen auf `0.8.8`, die
  wahrscheinlich aus einem Release-Test stammen und nicht Teil von Issue 078
  sind.

## Nächster Stand

- Nächstes offenes Issue nach `docs/issues/overview.txt`: Issue 075
  „Schnellkacheln“.
- Weitere Reihenfolge und Status ausschließlich aus
  `docs/issues/overview.txt` entnehmen.

Für die nächste Session `AI_SESSION_PROMPT.md` als wiederverwendbare
Startvorlage verwenden.

Ohne ausdrückliche Anweisung nicht committen, pushen, PR erstellen, mergen,
GitHub-Issue schließen oder Branch löschen.
