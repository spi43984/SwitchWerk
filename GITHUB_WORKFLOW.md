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

## Pull Requests

Auch wenn du alleine arbeitest:

- PR erstellen
- Diff prüfen
- Tests ausführen
- erst dann nach main mergen
