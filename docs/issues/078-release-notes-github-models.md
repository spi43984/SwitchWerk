# Issue 078: Release Notes mit GitHub Models vorbefüllen

## Metadaten

- Status: Abgeschlossen
- Priorität: P0
- Typ: Tooling / Release / Automatisierung
- Bereich: scripts/release-github.sh / GitHub Models / Release Notes

## Ziel

Das Release-Skript soll beim Erstellen eines GitHub-Releases automatisch einen
verständlichen deutschsprachigen Vorschlag für die Release Notes erzeugen und in
die Datei schreiben, die der Anwender anschließend im Editor prüft und bearbeitet.

## Hintergrund

`scripts/release-github.sh` erzeugt bereits eine temporäre Release-Notes-Datei
und öffnet sie im Editor. Aktuell enthält diese Datei nur einen Platzhalter.

GitHubs automatische Release Notes sind für SwitchWerk zu technisch. Gewünscht
ist ein Vorschlag im Stil der bestehenden anwenderorientierten Release Notes,
z. B. kurze Sätze wie bei Version 0.8.7.

Ein lokaler Test mit GitHub Models war erfolgreich. GitHub Copilot CLI wird für
diesen Zweck nicht weiter verfolgt, weil es nicht installiert ist und für diesen
Anwendungsfall weniger gut automatisierbar ist.

## Scope

- `scripts/release-github.sh` so erweitern, dass vor dem Öffnen des Editors ein
  Release-Notes-Vorschlag erzeugt wird.
- GitHub Models über die bestehende GitHub-Authentifizierung verwenden.
- Als Eingabe mindestens verwenden:
  - Commit-Beschreibungen seit dem letzten Release-Tag
  - `git diff --stat` seit dem letzten Release-Tag
- Den erzeugten Vorschlag in die temporäre Release-Notes-Datei schreiben.
- Danach weiterhin den Editor öffnen, damit der Anwender den Text prüfen und
  bearbeiten kann.
- Bei fehlendem `jq`, fehlendem `curl`, fehlendem GitHub-Token, fehlender
  Models-Berechtigung oder API-Fehler sauber auf den bisherigen manuellen
  Platzhalter zurückfallen.
- Keine Tokens, Zugangsdaten oder sensiblen Werte ausgeben oder loggen.

## Beispiel für den getesteten API-Aufruf

```bash
export GITHUB_TOKEN="$(gh auth token)"

PREVIOUS_TAG="$(git describe --tags --abbrev=0)"
git log "${PREVIOUS_TAG}"..HEAD --pretty=format:'- %s' > /tmp/switchwerk-release-test-commits.txt
git diff --stat "${PREVIOUS_TAG}"..HEAD > /tmp/switchwerk-release-test-diffstat.txt

jq -n \
  --rawfile commits /tmp/switchwerk-release-test-commits.txt \
  --rawfile diffstat /tmp/switchwerk-release-test-diffstat.txt \
  '{
    model: "openai/gpt-4o-mini",
    messages: [
      {
        role: "system",
        content: "Du bist der Release-Manager von SwitchWerk. Du schreibst kurze deutschsprachige Release Notes für Endanwender. Verwende ausschließlich die gelieferten Informationen. Erfinde keine Funktionen. Erfinde keine Performance- oder Stabilitätsverbesserungen. Keine Commit-Hashes, keine PR-Nummern, keine technischen Interna."
      },
      {
        role: "user",
        content: "Formuliere Release Notes im Stil von SwitchWerk 0.8.7.\n\nRegeln:\n- Maximal 8 Stichpunkte.\n- Kurze, verständliche Sätze.\n- Nur Änderungen nennen, die Anwender bemerken können.\n- Ähnliche Änderungen zusammenfassen.\n- Interne Refactorings, Build-, CI-, Test- und reine Dokumentationsänderungen weglassen.\n- Wenn zu wenig Anwender-relevante Änderungen erkennbar sind, schreibe nur die sicheren Punkte.\n\nCommits seit dem letzten Release:\n\($commits)\n\nGeänderte Dateien als Überblick:\n\($diffstat)"
      }
    ],
    temperature: 0.2
  }' | curl -sS \
  -H "Authorization: Bearer $GITHUB_TOKEN" \
  -H "Content-Type: application/json" \
  https://models.github.ai/inference/chat/completions \
  -d @- | jq -r '.choices[0].message.content'
```

## Nicht im Scope

- GitHub Copilot CLI
- Vollautomatisches Veröffentlichen ohne manuelle Prüfung
- Änderung des APK-Builds
- Änderung der Versionierungslogik
- Änderung der GitHub-Release-Assets

## Architekturhinweise

- Die bestehende Funktion `prepare_release_notes` erweitern oder in kleine
  Hilfsfunktionen aufteilen.
- Der Editor-Schritt bleibt verpflichtend.
- Das Skript muss auch ohne GitHub Models weiterhin funktionieren.
- Der API-Fehlerfall darf den Release-Prozess nicht unnötig blockieren, solange
  manuelle Release Notes möglich bleiben.
- Temporäre Dateien unter `/tmp` verwenden und keine sensiblen Inhalte in das
  Repository schreiben.

## Akzeptanzkriterien

- [x] Das Skript erzeugt vor dem Editor-Aufruf einen deutschsprachigen Vorschlag
      für Release Notes.
- [x] Der Vorschlag basiert auf Commits und Diff-Statistik seit dem letzten
      Release.
- [x] Der Vorschlag enthält keine Commit-Hashes, PR-Nummern oder technischen
      Interna.
- [x] Der Anwender kann den Vorschlag vor dem Release im Editor bearbeiten.
- [x] Bei nicht verfügbarer GitHub-Models-API fällt das Skript auf einen
      manuellen Platzhalter zurück.
- [x] Tokens und API-Antworten mit sensiblen Daten werden nicht geloggt.
- [x] `scripts/release-github.sh` bleibt mit `set -euo pipefail` robust.
- [x] Bestehende Release-Schritte bleiben unverändert erhalten.

## Abschluss

- Implementiert in `scripts/release-github.sh`.
- GitHub-Issue: #176
- Commit auf `main`: `e3f383d`
- Lokale Prüfungen:
  - `bash -n scripts/release-github.sh`
  - `shellcheck scripts/release-github.sh`
- Hilfe-, Info- und Tooltip-Texte: nicht betroffen, da reine Release-Tooling-Änderung.
- Konfigurationsexport/-import: nicht betroffen, da keine App-Einstellung geändert wurde.

## Testhinweise

- Test mit verfügbarer GitHub-Models-API durchführen.
- Test ohne gültigen `GITHUB_TOKEN` durchführen.
- Test ohne `jq` oder ohne `curl` simulieren.
- Prüfen, dass der Editor weiterhin geöffnet wird.
- Prüfen, dass der Platzhalter-Fallback weiterhin bearbeitbar ist.
- Prüfen, dass kein Token in der Ausgabe erscheint.
- Release-Skript nicht vollständig gegen ein echtes Release ausführen, solange
  nur die Generierung getestet wird.

## Überschneidungsprüfung

- Issue 037 behandelt GitHub-Release-Update-Support, aber nicht die automatische
  Vorbefüllung anwenderorientierter Release Notes.
- Issue 062 betrifft die Anzeige von Release Notes in der App, nicht deren
  Erstellung im Release-Skript.
- Issue 069 behandelt das stabile Latest-APK-Asset, aber nicht den Inhalt der
  Release Notes.
