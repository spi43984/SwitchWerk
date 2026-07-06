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

Jede größere Implementierung bekommt ein GitHub Issue.

Die nächste Umsetzung wird über `docs/issues/overview.txt` priorisiert:
zuerst Status `offen`, dann Priorität `P0` bis `P4`, danach Issue-ID aufsteigend.

Issue sollte enthalten:

- Ziel
- Akzeptanzkriterien
- Sicherheits-/Datenschutzhinweise
- Testhinweise

Issues, Codebeispiele und Testdaten dürfen keine personenbezogenen Daten,
privaten Domains, realen Hostnamen, realen SSIDs, lokalen IP-Adressen,
Passwörter, Tokens oder API-Keys enthalten. Für technische Beispiele sind
neutrale Platzhalter zu verwenden, z. B. `server.domain.com`, `device.local`,
`192.0.2.10` oder `Example WiFi`.

## Lokale Planungs-Issues

Dateien unter `docs/issues/*.md` sind lokale Planungs-Issues und nicht automatisch GitHub-Issues.

Vor dem Anlegen eines neuen Planungs-Issues ist immer zu prüfen:

1. ob bereits ein ähnliches Issue existiert
2. ob das Thema bereits durch ein anderes Issue abgedeckt ist
3. ob sich das Thema mit bestehenden offenen Issues überschneidet
4. welche nächste freie lokale Issue-Nummer nach `docs/issues/overview.txt` zu verwenden ist

Für neue lokale Planungs-Issues gilt:

1. GitHub lesend prüfen
2. `docs/issues/overview.txt` lesen
3. nächste freie lokale Issue-Nummer bestimmen
4. neue Datei unter `docs/issues` anlegen
5. `docs/issues/overview.txt` aktualisieren
6. Beispiele und Testhinweise anonymisieren und ausschließlich neutrale
   Platzhalter verwenden.

Dabei gilt:

- kein `gh issue create`
- kein GitHub-Issue erforderlich
- kein Implementierungs-Branch erforderlich
- kein Pull Request erforderlich

Ein Dokumentations-Branch darf verwendet werden, wenn der Benutzer die Änderung in GitHub veröffentlichen möchte.

Ausnahme: Der Benutzer fordert ausdrücklich Veröffentlichung, Branch, Pull Request oder Merge an.

Grundsatz:

```text
Planungsdatei ≠ GitHub-Issue
```

Plattformbezogene Planungs-Issues werden ebenfalls zunächst ausschließlich
lokal geführt. Insbesondere bleiben iOS-Planungs-Issues unter `docs/issues/`,
bis eine iOS-Implementierung ausdrücklich beauftragt wird. Erst dann wird nach
dem üblichen Workflow geprüft, ob ein zugehöriges GitHub-Issue und ein
Implementierungs-Branch erforderlich sind.

Die Spalte `Prio` in `docs/issues/overview.txt` bleibt die globale
Priorisierung. Eine verbindliche Reihenfolge innerhalb eines
plattformbezogenen Backlogs wird zusätzlich über ein Metadatenfeld wie
`iOS-Phase: 1 von 6` in den lokalen Issue-Dateien dokumentiert und ersetzt
oder erweitert nicht die Werte `P0` bis `P4`.

`ai-context.md` nur ändern, wenn dauerhafter Projektkontext, Projektentscheidungen oder langfristige Projektregeln betroffen sind.

`AI_HANDOFF.md` nur ändern, wenn aktive Arbeit, Implementierungsstand oder abgeschlossene Arbeit dokumentiert werden muss. Die Datei enthält ausschließlich den aktuellen Übergabestand für die nächste Session; frühere Übergabestände werden ersetzt und nicht als Historie angehängt.

## Issue-Dateien, GitHub Issues und Branches

Die fachliche Issue-Planung liegt im Repository unter `docs/issues`.

Für jedes umzusetzende Issue wird zuerst die passende Datei unter `docs/issues/*.md` geprüft.

Vor Beginn der Implementierung wird geprüft, ob bereits ein passendes GitHub-Issue oder ein passender Feature-Branch existiert. Vor jedem `gh issue create` muss GitHub lesend nach einem passenden bestehenden Issue durchsucht werden. Das gilt auch dann, wenn ein Startbefehl bereits `gh issue create` enthält. Nur wenn noch kein passendes GitHub-Issue existiert, wird es aus der lokalen Issue-Datei erzeugt.

Beispiel für Issue 009:

    git switch main
    git pull

    gh issue list --state all --search "WiFi Connection Service"

    gh issue create \
      --title "WiFi Connection Service" \
      --body-file docs/issues/009-wifi-connection-service.md

Der `gh issue create`-Befehl darf nur ausgeführt werden, wenn die vorherige
Suche kein passendes GitHub-Issue ergeben hat.

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
- nach dem Merge explizit prüfen, dass der PR wirklich gemergt wurde und der
  Merge-Commit beziehungsweise Squash-Commit auf `main` angekommen ist
- das zugehörige GitHub-Issue erst nach dieser Prüfung schließen

Der PR-Text verknüpft das Issue mit `Refs #<NUMMER>`. Die Schlüsselwörter
`Closes`, `Fixes` und `Resolves` werden nicht verwendet, weil GitHub das Issue
sonst bereits beim Merge automatisch schließt. Nach diesem Projektworkflow
wird das Issue erst nach dem separaten Dokumentations-Push explizit geschlossen.

Wenn ein Repository Merge-Commits nicht erlaubt, ist `gh pr merge --merge`
nicht geeignet. In diesem Fall muss die im Repository erlaubte Strategie
verwendet werden, zum Beispiel `--squash`. Ein geschlossener PR ohne
`mergedAt` gilt nicht als abgeschlossen und darf kein Issue schließen.

Für AI-gestützte Arbeit gelten zusätzlich die Freigaberegeln aus `AGENTS.md`, `ai-context.md` und `AI_SESSION_PROMPT.md`: PRs und Merge nur nach ausdrücklicher Freigabe. Status, Priorisierung und Reihenfolge der Issues stehen ausschließlich in `docs/issues/overview.txt`.

### GitHub-Actions-Status ohne Polling

Der Assistent fragt den Check-Status eines Pull Requests nach Push oder
PR-Erstellung höchstens einmal aktiv ab. Nicht verwenden:

- `gh pr checks --watch`
- Polling-Schleifen mit `gh pr checks`, `gh run watch` oder `gh run view`
- wiederholte manuelle Statusabfragen während derselben Wartephase

Wenn der Merge bereits ausdrücklich freigegeben wurde und die Checks noch
laufen, soll der Assistent bevorzugt Auto-Merge aktivieren:

```bash
gh pr merge <PR-NUMMER> --auto --squash
```

Danach endet der Arbeitsschritt ohne Warten auf GitHub Actions. Ist Auto-Merge
im Repository nicht verfügbar, wird der ausstehende Status gemeldet und erst
nach einer späteren Benutzer-Rückmeldung erneut geprüft. Ohne ausdrückliche
Merge-Freigabe wird kein Auto-Merge aktiviert.

Nach dem tatsächlichen Merge bleiben die bestehenden Abschlussprüfungen
verbindlich: `state=MERGED`, gesetztes `mergedAt`, Merge-/Squash-Commit auf
`main`, Dokumentationsabschluss, Issue-Schließung und Branch-Bereinigung.

### Sauberer Wechsel auf main nach dem Merge

Die Abschlussdokumentation wird im Normalfall erst nach dem Merge auf dem
aktualisierten Branch `main` geändert. Vor dem Wechsel müssen Branch und
Arbeitsbaum mit `git status --short --branch` geprüft werden.

Bei sauberem Arbeitsbaum wird `main` unabhängig von einer lokalen
`pull.rebase`-Konfiguration so aktualisiert:

```bash
git fetch origin
git switch main
git merge --ff-only origin/main
```

Wurde Abschlussdokumentation bereits auf dem Feature-Branch vorbereitet, darf
nicht mit diesen uncommittierten Änderungen gepullt werden. Die betroffenen
Dateien werden vorher mit expliziten Pfaden gezielt gestasht, nach dem
Fast-Forward wiederhergestellt und erst dann auf `main` committet:

```bash
git stash push \
-m "Issue-Abschlussdokumentation" \
-- \
AI_HANDOFF.md \
docs/issues/<issue>.md \
docs/issues/overview.txt

git fetch origin
git switch main
git merge --ff-only origin/main

git stash pop
```

Weitere tatsächlich geänderte Abschlussdateien, etwa `AGENTS.md` oder
String-Ressourcen, müssen in diesem gezielten Stash ausdrücklich ergänzt
werden. Ein pauschaler Stash ist unzulässig, wenn er fremde Benutzeränderungen
einschließen könnte. Bei Konflikten nach `git stash pop` wird nicht automatisch
weitercommittet; zuerst werden die Konflikte geprüft und gelöst.

## Lokale Befehlsausgaben

Wenn der Assistent Befehle ausgibt, die der Benutzer lokal kopieren und
einfügen soll, gelten diese Regeln:

- keine führenden Leerzeichen vor Befehlen
- keine Shell-Variablen in Copy-&-Paste-Befehlen
- lange Befehle mit `\` am Zeilenende umbrechen
- nach kritischen GitHub-Aktionen immer Prüfkommandos mit ausgeben
- vor Abschlussbefehlen den tatsächlichen Git-Status prüfen und nur noch nicht
  ausgeführte, zum Zustand passende Schritte ausgeben
- niemals `git pull` mit uncommittierten Änderungen anweisen; vorbereitete
  Abschlussdateien bei Bedarf gezielt stashen
- nach einem Merge bevorzugt `git fetch origin`, `git switch main` und
  `git merge --ff-only origin/main` statt eines konfigurationsabhängigen
  `git pull` ausgeben
- PR-Texte mit `Refs #<NUMMER>` verknüpfen und keine automatisch schließenden
  Schlüsselwörter verwenden
- Nach Implementierungen oder prüfpflichtigen Änderungen immer auch die
  vollständigen Release-Befehle zum Kompilieren und Installieren ausgeben,
  unabhängig davon, ob der konkrete Prompt nur allgemein nach Prüfungen oder
  Befehlen fragt:

```text
./gradlew lintRelease
./gradlew testDebugUnitTest
./gradlew clean assembleRelease
./gradlew installRelease
```

  `testReleaseUnitTest` existiert nicht. `installRelease` setzt konfigurierte
  Release-Signierung voraus. Bei abweichender Signatur zur installierten App
  muss die vorhandene App vorher deinstalliert werden; dabei gehen lokale
  App-Daten verloren.

Beispiel:

```bash
gh pr merge 123 \
--squash \
--delete-branch \
--subject "Kurzer Merge-Titel" \
--body "Kurze Merge-Beschreibung."
gh pr view 123 \
--json state,mergedAt,mergeCommit
git switch main
git pull
git log \
--oneline \
-1
```

## GitHub Actions

GitHub Actions ergänzt die lokalen Prüfungen, ersetzt sie aber nicht. Um
Runner-Zeit zu sparen, läuft der Workflow **Android Quality Checks** nur für
Pull Requests nach `main`, die Android- oder Build-relevante Dateien ändern.
Änderungen ausschließlich unter `docs/` oder ausschließlich an Markdown-Dateien
starten keinen Android-Workflow. Pushes auf `main` und Feature-Branches starten
keine Android-Prüfung.

Der Workflow führt ohne `clean` ausschließlich diese nicht überlappenden
Qualitätsprüfungen aus:

    ./gradlew lintDebug testDebugUnitTest

`gradle/actions/setup-gradle` stellt den Gradle- und Dependency-Cache bereit.
Ein separater, potenziell inkonsistenter Cache für Build-Ausgaben wird bewusst
nicht verwendet.

Der Workflow **Release APK** erzeugt weiterhin ein Debug-APK und lädt es als
Artefakt hoch. Er startet bei Tags mit Präfix `v` oder manuell; der manuelle
Start ist auf `main` begrenzt. Dadurch bleiben Release-Artefakte verfügbar,
ohne bei jedem Push erneut erzeugt zu werden.

## Vollständiger Issue-Workflow

1. Nächstes offenes Issue nach `docs/issues/overview.txt` bestimmen: zuerst Status `offen`, dann Priorität `P0` bis `P4`, danach Issue-ID aufsteigend.
2. Auf `main` wechseln.
3. `git pull` ausführen.
4. GitHub lesend prüfen, ob bereits ein passendes GitHub-Issue existiert.
5. Prüfen, ob bereits ein passender Feature-Branch existiert.
6. GitHub-Issue nur bei Bedarf aus der passenden Datei unter `docs/issues/*.md` erzeugen.
7. GitHub-Issue-Nummer notieren.
8. Fachlichen Branch anlegen oder vorhandenen passenden Branch verwenden.
9. Implementieren.
10. Bei jeder neuen Funktion ausdrücklich prüfen und den Benutzer fragen, ob
    zugehörige neue oder geänderte Einstellungen exportiert und importiert
    werden sollen. Die Entscheidung im Issue-Scope dokumentieren und keine
    Variante stillschweigend voraussetzen.
11. Bei neuen oder geänderten Funktionen Hilfe-, Info- und Tooltip-Texte prüfen
   und aktualisieren, damit die UI-Erklärung zum tatsächlichen Verhalten passt.
12. Build und Installation auf dem Host passend zur installierten Variante testen.
    Debug:

       ./gradlew lintDebug
       ./gradlew testDebugUnitTest
       ./gradlew clean assembleDebug
       ./gradlew installDebug

    Release bei konfigurierter Release-Signierung:

       ./gradlew lintRelease
       ./gradlew testDebugUnitTest
       ./gradlew clean assembleRelease
       ./gradlew installRelease

    `testReleaseUnitTest` existiert nicht. Bei einem Variantenwechsel mit
    abweichender Signatur muss die installierte App zuerst deinstalliert werden;
    ihre lokalen Daten gehen dabei verloren.

13. Ohne ausdrückliche Veröffentlichungsanforderung nicht committen, pushen oder einen Pull Request erstellen.
14. Nach ausdrücklicher Veröffentlichungsanforderung auf dem Feature-Branch committen.
15. Feature-Branch pushen.
16. Pull Request erstellen.
17. Pull Request prüfen und erst nach separater ausdrücklicher Merge-Freigabe nach `main` mergen.
18. Unmittelbar nach dem Merge prüfen, dass der PR wirklich gemergt wurde:

       gh pr view <PR-NUMMER> \
       --json state,mergedAt,mergeCommit

   `state` muss `MERGED` sein und `mergedAt` darf nicht leer sein.
19. Sicherstellen, dass der Arbeitsbaum sauber ist. Bereits vorbereitete
    Abschlussdokumentation andernfalls mit expliziten Dateipfaden gezielt
    stashen. Danach `origin` abrufen, auf `main` wechseln und per
    `git merge --ff-only origin/main` aktualisieren. Anschließend einen
    angelegten Dokumentations-Stash wiederherstellen.
20. Prüfen, dass `main` den Merge- oder Squash-Commit enthält:

       git log \
       --oneline \
       -1

21. Lokale Issue-Datei unter `docs/issues` abhaken.
22. `docs/issues/overview.txt` aktualisieren.
23. Prüfen, dass `docs/issues/overview.txt` und die lokale Issue-Datei denselben Status zeigen.
24. `ai-context.md` nur bei geändertem dauerhaftem Projektkontext, geänderten Projektentscheidungen oder langfristigen Projektregeln aktualisieren.
25. `AI_HANDOFF.md` aktualisieren, wenn aktive Arbeit, Implementierungsstand oder abgeschlossene Arbeit dokumentiert werden muss.
26. Dokumentationsänderungen committen und pushen.
27. Nach dem Dokumentations-Push erneut `main` prüfen:

       git status \
       -sb

28. Zugehöriges GitHub-Issue erst danach schließen.
29. Branch lokal löschen.
30. Branch remote löschen.
31. Remote-Referenzen bereinigen.

## Verbindliche Issue-Abschluss-Checkliste

Ein Issue gilt erst als vollständig abgeschlossen, wenn alle Punkte geprüft wurden:

* [ ] `docs/issues/<issue>.md` aktualisiert
* [ ] `docs/issues/overview.txt` aktualisiert
* [ ] Hilfe-, Info- und Tooltip-Texte bei neuen oder geänderten Funktionen geprüft und bei Bedarf aktualisiert
* [ ] `ai-context.md` bei geändertem dauerhaftem Projektkontext aktualisiert
* [ ] `AI_HANDOFF.md` aktualisiert, falls aktive Arbeit, Implementierungsstand oder abgeschlossene Arbeit dokumentiert werden muss
* [ ] nächstes offenes Issue in `docs/issues/overview.txt` festgelegt
* [ ] Status von Issue-Datei und `docs/issues/overview.txt` ist konsistent
* [ ] Pull Request ist wirklich gemergt (`state=MERGED`, `mergedAt` gesetzt)
* [ ] `main` enthält den Merge- oder Squash-Commit
* [ ] GitHub-Issue geschlossen
* [ ] Feature-Branch lokal gelöscht
* [ ] Feature-Branch remote gelöscht
* [ ] Remote-Referenzen bereinigt

Beispiel nach Merge:

    git switch main
    git pull
    git branch -d wifi-connection-service
    git push origin --delete wifi-connection-service
    git fetch --prune
    git status
