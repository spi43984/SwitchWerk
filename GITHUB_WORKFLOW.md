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


## Issue-Dateien, GitHub Issues und Branches

Die fachliche Issue-Planung liegt im Repository unter `docs/issues`.

Für jedes umzusetzende Issue wird zuerst die passende Datei unter `docs/issues/*.md` geprüft.

Vor Beginn der Implementierung wird aus dieser Datei ein GitHub-Issue erzeugt.

Beispiel für Issue 009:

    git switch main
    git pull

    gh issue create \
      --title "WiFi Connection Service" \
      --body-file docs/issues/009-wifi-connection-service.md

Die von GitHub vergebene Issue-Nummer muss nicht mit der Dateinummer übereinstimmen.

Erst danach wird ein eigener Branch angelegt. Der Branchname orientiert sich am fachlichen Thema und muss nicht mit `issue-` beginnen.

Beispiel:

    git switch -c wifi-connection-service

## Pull Requests

Auch wenn du alleine arbeitest:

- PR erstellen
- Diff prüfen
- Tests ausführen
- erst dann nach `main` mergen

## Vollständiger Issue-Workflow

1. Nächstes offenes Issue unter `docs/issues` bestimmen.
2. Auf `main` wechseln.
3. `git pull` ausführen.
4. GitHub-Issue aus der passenden Datei unter `docs/issues/*.md` erzeugen.
5. GitHub-Issue-Nummer notieren.
6. Fachlichen Branch anlegen.
7. Implementieren.
8. Build und Installation testen:

       ./gradlew clean assembleDebug
       ./gradlew installDebug

9. Commit auf dem Feature-Branch erstellen.
10. Branch pushen.
11. Pull Request erstellen.
12. Pull Request prüfen und nach `main` mergen.
13. Lokale Issue-Datei unter `docs/issues` abhaken.
14. Änderung an der Issue-Datei committen und pushen.
15. Zugehöriges GitHub-Issue schließen.
16. Branch lokal und remote löschen.

Beispiel nach Merge:

    git switch main
    git pull

    git branch -d wifi-connection-service
    git push origin --delete wifi-connection-service
