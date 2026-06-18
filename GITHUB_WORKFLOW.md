# GITHUB_WORKFLOW.md

## Zweck

Diese Datei beschreibt den GitHub-, Branch-, Issue- und Pull-Request-Workflow.

Verbindliche Regeln für KI-Agenten stehen in `AGENTS.md`. Bei AI-gestützter Arbeit gelten zusätzlich die Freigaberegeln aus `AGENTS.md` und `AI_SESSION_PROMPT.md`.

## Branches

Einfacher Workflow:

```text
main
feature/name-des-features
bugfix/name-des-fehlers
docs/name-der-dokumentation
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

Die nächste Umsetzung wird über `docs/issues/overview.txt` priorisiert:
zuerst Status `offen`, dann Priorität `P0` bis `P4`, danach Issue-ID aufsteigend.

Issue sollte enthalten:

- Ziel
- Akzeptanzkriterien
- Sicherheits-/Datenschutzhinweise
- Testhinweise

## Issue-Dateien, GitHub Issues und Branches

Die fachliche Issue-Planung liegt im Repository unter `docs/issues`.

Für jedes umzusetzende Issue wird zuerst die passende Datei unter `docs/issues/*.md` geprüft.

Vor Beginn der Implementierung wird geprüft, ob bereits ein passendes GitHub-Issue oder ein passender Feature-Branch existiert. Nur wenn noch kein GitHub-Issue existiert, wird es aus der lokalen Issue-Datei erzeugt.

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

Für die allgemeine GitHub-Nutzung und manuelle Repository-Arbeit:

- PR erstellen
- Diff prüfen
- Tests ausführen
- erst dann nach `main` mergen

Für AI-gestützte Arbeit gelten zusätzlich die Freigaberegeln aus `AGENTS.md`, `ai-context.md` und `AI_SESSION_PROMPT.md`: PRs und Merge nur nach ausdrücklicher Freigabe.

## Vollständiger Issue-Workflow

1. Nächstes offenes Issue nach `docs/issues/overview.txt` bestimmen: zuerst Status `offen`, dann Priorität `P0` bis `P4`, danach Issue-ID aufsteigend.
2. Auf `main` wechseln.
3. `git pull` ausführen.
4. Prüfen, ob bereits ein passendes GitHub-Issue oder ein Feature-Branch existiert.
5. GitHub-Issue nur bei Bedarf aus der passenden Datei unter `docs/issues/*.md` erzeugen.
6. GitHub-Issue-Nummer notieren.
7. Fachlichen Branch anlegen oder vorhandenen passenden Branch verwenden.
8. Implementieren.
9. Build und Installation auf dem Host testen:

       ./gradlew clean assembleDebug
       ./gradlew installDebug

10. Ohne ausdrückliche Veröffentlichungsanforderung nicht committen, pushen oder einen Pull Request erstellen.
11. Nach ausdrücklicher Veröffentlichungsanforderung auf dem Feature-Branch committen.
12. Feature-Branch pushen.
13. Pull Request erstellen.
14. Pull Request prüfen und erst nach separater ausdrücklicher Merge-Freigabe nach `main` mergen.
15. Nach dem Merge auf `main` wechseln und aktuellen Stand holen.
16. Lokale Issue-Datei unter `docs/issues` abhaken.
17. `docs/issues/overview.txt` aktualisieren.
18. `ai-context.md` aktualisieren:
    - abgeschlossenes Issue in `Abgeschlossen` verschieben
    - abgeschlossenes Issue aus `Offen` entfernen
    - `Nächstes Issue` auf das nächste offene Issue setzen
19. Prüfen, dass `docs/issues/overview.txt`, die lokale Issue-Datei und `ai-context.md` denselben Status zeigen.
20. Dokumentationsänderungen committen und pushen.
21. Zugehöriges GitHub-Issue erst danach schließen.
22. Branch lokal und remote löschen.

## Verbindliche Issue-Abschluss-Checkliste

Ein Issue gilt erst als vollständig abgeschlossen, wenn alle Punkte geprüft wurden:

* [ ] `docs/issues/<issue>.md` aktualisiert
* [ ] `docs/issues/overview.txt` aktualisiert
* [ ] `ai-context.md` aktualisiert
* [ ] `AI_HANDOFF.md` aktualisiert
* [ ] nächstes offenes Issue festgelegt
* [ ] Status der Dokumentationsdateien ist konsistent
* [ ] GitHub-Issue geschlossen
* [ ] Feature-Branch lokal gelöscht
* [ ] Feature-Branch remote gelöscht

Beispiel nach Merge:

    git switch main
    git pull
    git status
    git branch -d wifi-connection-service
