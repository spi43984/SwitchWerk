# GITHUB_WORKFLOW.md

## Branches

Einfacher Workflow:

```text
main
feature/name-des-features
bugfix/name-des-fehlers
```

## Commits

Conventional Commits verwenden:

```text
feat: add device list
fix: handle unreachable shelly device
docs: update architecture notes
test: add repository tests
refactor: simplify device repository
```

## Issues

Jede größere Änderung bekommt ein GitHub Issue.

Issue sollte enthalten:

- Ziel
- Akzeptanzkriterien
- Sicherheits-/Datenschutzhinweise
- Testhinweise

## Issue-Dateien und GitHub Issues

Die fachliche Issue-Planung liegt im Repository unter `docs/issues`.

Für jedes umzusetzende Issue wird zuerst die passende Datei unter `docs/issues/*.md` geprüft.

Vor Beginn der Implementierung wird aus dieser Datei ein GitHub Issue erzeugt, zum Beispiel:

```bash
gh issue create \
  --title "Device Management" \
  --body-file docs/issues/008-device-management.md
```

Die von GitHub vergebene Issue-Nummer muss nicht mit der Dateinummer übereinstimmen.

Nach erfolgreicher Implementierung, Build, Test und Merge wird:

1. die Datei unter `docs/issues/*.md` abgehakt,
2. die Änderung committet und gepusht,
3. das zugehörige GitHub Issue geschlossen.

Beispiel:

```bash
gh issue close <NUMMER> --comment "Implemented, tested and merged."
```

## Pull Requests

Auch wenn du alleine arbeitest:

- PR erstellen
- Diff prüfen
- Tests ausführen
- erst dann nach main mergen

## Branch-Workflow für Issues

Für jedes neue Issue wird vor der Implementierung ein eigener Branch
angelegt.

Beispiel:

    git pull
    git switch -c issue-008-device-management

Nach erfolgreichem Build und Test:

    git status
    git add .
    git commit -m "feat: implement device management"
    git push -u origin issue-008-device-management

Änderungen werden anschließend per Pull Request nach main gemerged.
