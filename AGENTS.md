# AGENTS.md

## Ziel des Projekts

Diese Android-App steuert Shelly und andere lokale Geräte über HTTP/API-Aufrufe.

Die App soll einfach, wartbar, sicher und datenschutzfreundlich bleiben.

## Grundregeln für KI-Agenten

Beim Erstellen oder Ändern von Code:

1. Kotlin verwenden.
2. Jetpack Compose verwenden.
3. MVVM verwenden.
4. Koin für Dependency Injection verwenden.
5. Coroutines und Flow verwenden.
6. Keine unnötige Komplexität einführen.
7. Keine Cloud-Abhängigkeit einbauen, außer ausdrücklich gewünscht.
8. Keine sensiblen Daten loggen.
9. Netzwerkfehler sauber behandeln.
10. Code einfach und verständlich halten.
11. Keine personenbezogenen Daten, privaten Domains, realen Hostnamen,
    realen SSIDs, lokalen IP-Adressen oder Zugangsdaten in Code,
    Dokumentation, Issues, Tests oder Beispielkonfigurationen schreiben.
    Stattdessen neutrale Platzhalter verwenden, z. B. `server.domain.com`,
    `device.local`, `192.0.2.10` oder `Example WiFi`.

## Bevorzugte Bibliotheken

- Jetpack Compose
- Material Design 3
- Koin
- Kotlin Coroutines
- StateFlow
- OkHttp oder Retrofit
- Room nur falls lokale Speicherung nötig ist

## Nicht verwenden ohne Rückfrage

- Firebase
- Analytics SDKs
- Tracking SDKs
- Werbung
- Cloud-Synchronisation
- Account-Systeme
- Hintergrund-Standortzugriff

## Entwicklungsziel

Lieber eine kleine, stabile App als eine überarchitektierte App.

## Codex-Kostensparregeln

Codex soll möglichst wenig Kontingent verbrauchen und keine unnötigen
Repository-Analysen durchführen.

Für jede Aufgabe gilt:

1. Zuerst nur lesen:
   - `AGENTS.md`
   - `AI_HANDOFF.md`
   - bei Issue-Arbeiten die konkrete Datei unter `docs/issues`
2. Danach nur gezielt weitere Dateien lesen:
   - `ai-context.md`, wenn dauerhafter Projektkontext oder Projektentscheidungen nötig sind
   - `docs/issues/overview.txt`, wenn Issue-Status, Priorisierung oder Reihenfolge nötig sind
   - `ARCHITECTURE.md`, wenn Architektur, Packages oder Schichten betroffen sind
   - `CODE_STYLE.md`, wenn Code geändert wird
   - `TESTING.md`, wenn Tests geplant oder bewertet werden
   - `SECURITY.md`, wenn Berechtigungen, Netzwerk, Speicherung oder sensible Daten betroffen sind
   - Gradle-Dateien nur bei Build-, Dependency- oder Android-Konfigurationsfragen
3. Keine vollständige Repository-Analyse ohne ausdrücklichen Auftrag.
4. Keine pauschale Suche über alle Dateien, wenn die betroffenen Pfade aus Issue,
   Handoff oder Fehlermeldung ableitbar sind.
5. Große Planungs-, Architektur- und Dokumentationsfragen bevorzugt im ChatGPT
   Browser vorbereiten.
6. Codex primär für konkrete, abgegrenzte Codeänderungen verwenden.
7. Projektwissen dauerhaft in Markdown-Dateien dokumentieren, nicht nur in
   Codex-Sessions.

## Arbeitsmodell ChatGPT, Codex und Host

- ChatGPT Browser: Analyse, Planung, Architekturfragen, Issue-Zuschnitt,
  Dokumentations-Review.
- Codex CLI im Docker-Container: konkrete Codeänderungen mit minimalem Kontext.
- Ubuntu-Host: Android Studio, Gradle-Builds, ADB, Installation und Gerätetests.

Codex darf nur Prüfungen im Container ausführen, die dort verfügbar und für die
aktuelle Aufgabe sinnvoll sind. Die maßgebliche Bestätigung für Build,
Installation und manuelle Gerätetests erfolgt durch den Benutzer auf dem
Ubuntu-Host.

## UI-Regel für Sicherheitsabfragen

Bei Sicherheitsabfragen steht die sichere Abbruchaktion immer rechts.

Beispiel:

- links: Ja / Löschen / Bestätigen
- rechts: Nein / Abbrechen

Damit ist „Nein“ bei Lösch- oder Sicherheitsabfragen immer auf der rechten Seite.

## Dokumentationsregeln für lokale Planungs-Issues

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
6. Beispiele und Testhinweise anonymisieren: keine personenbezogenen Daten,
   privaten Domains, realen Hostnamen, realen SSIDs, lokalen IP-Adressen,
   Passwörter, Tokens oder API-Keys verwenden.

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

`ai-context.md` nur ändern, wenn dauerhafter Projektkontext, Projektentscheidungen oder langfristige Projektregeln betroffen sind.

`AI_HANDOFF.md` nur ändern, wenn aktive Arbeit, Implementierungsstand oder abgeschlossene Arbeit dokumentiert werden muss.

## Branch- und Issue-Workflow

Implementierungen und größere Änderungen werden nie direkt auf `main` begonnen.

Der vollständige GitHub-Workflow ist in `GITHUB_WORKFLOW.md` dokumentiert.

### Phase 1: Vorbereitung und Implementierung

Für jede Implementierung eines bestehenden fachlichen Issues gilt zunächst:

1. Nächstes Issue nach `docs/issues/overview.txt` bestimmen: zuerst Status `offen`, dann Priorität `P0` bis `P4`, danach Issue-ID aufsteigend.
2. Auf `main` wechseln.
3. Aktuellen Stand holen.
4. Vor jedem `gh issue create` zwingend GitHub lesend prüfen, ob zur
   passenden Datei unter `docs/issues/*.md` bereits ein passendes GitHub-Issue
   existiert. Diese Prüfung gilt auch dann, wenn der Benutzer einen
   Startbefehl mit `gh issue create` vorgibt.
5. Prüfen, ob bereits ein passender Feature-Branch existiert.
6. Nur falls nach der GitHub-Prüfung noch kein passendes GitHub-Issue
   existiert, wird genau ein GitHub-Issue aus der lokalen Issue-Datei erzeugt.
   Dieser schreibende GitHub-Zugriff ist im Rahmen der Implementierung erlaubt.
7. Danach einen eigenen Branch mit fachlichem Namen anlegen oder einen
   vorhandenen passenden Branch verwenden, z. B. `wifi-connection-service`.
8. Ausschließlich den vereinbarten Issue-Scope implementieren.
9. Bei jeder neuen Funktion ausdrücklich prüfen und den Benutzer fragen, ob
   zugehörige neue oder geänderte Einstellungen in den Konfigurationsexport
   und -import aufgenommen werden sollen. Die Entscheidung im Issue-Scope
   dokumentieren; weder Übertragung noch ausschließlich lokale Speicherung
   stillschweigend annehmen.
10. Bei neuen oder geänderten Funktionen Hilfe-, Info- und Tooltip-Texte prüfen
   und aktualisieren, damit die UI-Erklärung zum tatsächlichen Verhalten passt.
11. Änderungen und Diff prüfen.
12. Nur verfügbare und sinnvolle Prüfungen in der aktuellen Umgebung ausführen.
13. Vollständige Copy-&-Paste-Befehle für Build, Installation und manuelle Tests
    auf dem Host ausgeben.

Erlaubter GitHub-Schreibzugriff in Phase 1:

- genau ein `gh issue create`, wenn nach der vorherigen GitHub-Prüfung kein
  passendes GitHub-Issue existiert

Nicht erlaubt ohne ausdrückliche Freigabe:

- Commit
- Push
- Pull Request
- Merge
- GitHub-Issue schließen
- Branch löschen

Codex soll dem Benutzer passende, möglichst schnelle Prüfbefehle nennen und
nicht automatisch immer den vollständigen Block mit `clean` ausgeben. Für
schnelle Iterationen reichen oft `testDebugUnitTest` und `lintDebug`; bei
Android-Integration, Manifest, Widget, Launcher oder Installation zusätzlich
ohne `clean` bauen und installieren. Der vollständige Block mit `clean` ist vor
Veröffentlichung oder bei unklaren Build-Problemen sinnvoll.

Der Benutzer testet standardmäßig die Release-Variante. Nach implementierten
Code-, UI-, Android-Integrations- oder Widget-Änderungen gibt Codex deshalb im
Abschluss automatisch die passenden vollständigen Release-Befehle aus. Für den
schnellen Geräte-Test sind das normalerweise `./gradlew assembleRelease` und
`./gradlew installRelease`, jeweils als eigene direkt kopierbare Befehlszeile
und ohne `clean`. Debug-Befehle nur nennen, wenn der Benutzer ausdrücklich die
Debug-Variante testet oder die Release-Variante technisch ungeeignet ist.

Mindestens auf dem Host zu prüfen. Codex bietet abhängig von der aktuell auf dem
Testgerät installierten Variante passende vollständige Copy-&-Paste-Befehle an.

Debug:

    ./gradlew lintDebug
    ./gradlew testDebugUnitTest
    ./gradlew clean assembleDebug
    ./gradlew installDebug

Release:

    ./gradlew lintRelease
    ./gradlew testDebugUnitTest
    ./gradlew clean assembleRelease
    ./gradlew installRelease

Es gibt keinen Task `testReleaseUnitTest`; für beide Varianten gilt
`testDebugUnitTest`. `installRelease` setzt eine konfigurierte Release-Signierung
voraus. Bei abweichenden Signaturen muss die bereits installierte Variante vor
dem Wechsel deinstalliert werden, wodurch lokale App-Daten verloren gehen.

Codex meldet den Build nicht als erfolgreich, solange der Benutzer kein
erfolgreiches Ergebnis vom Host zurückgemeldet hat.

### Phase 2: Veröffentlichung und Abschluss

Erst wenn die Implementierung geprüft wurde und der Benutzer ausdrücklich die
Veröffentlichung oder den Abschluss anfordert, dürfen folgende Schritte
ausgeführt werden:

1. Änderungen committen.
2. Feature-Branch pushen.
3. Pull Request erstellen.
4. Pull Request prüfen und nach ausdrücklicher Freigabe nach `main` mergen.
5. Nach dem Merge auf `main` wechseln und aktuellen Stand holen.
6. Lokale Issue-Datei unter `docs/issues` abhaken.
7. `docs/issues/overview.txt` aktualisieren.
8. Prüfen, dass `docs/issues/overview.txt` und die lokale Issue-Datei denselben
   Status zeigen. `docs/issues/overview.txt` ist die einzige Quelle für die Liste
   und Reihenfolge offener, abgeschlossener und zurückgestellter Issues.
9. `ai-context.md` nur aktualisieren, wenn sich dauerhafter Projektkontext,
   Projektentscheidungen oder langfristige Projektregeln geändert haben.
10. `AI_HANDOFF.md` aktualisieren, wenn aktive Arbeit, Implementierungsstand oder
    abgeschlossene Arbeit dokumentiert werden muss.
11. Dokumentationsänderungen committen und pushen.
12. Zugehöriges GitHub-Issue erst danach schließen.
13. Feature-Branch lokal löschen.
14. Feature-Branch remote löschen.
15. Remote-Referenzen bereinigen.

### Budgetschonender Umgang mit GitHub Actions

GitHub-Actions-Status nach dem Push oder Erstellen eines Pull Requests höchstens
einmal aktiv abfragen. Codex verwendet dafür kein `gh pr checks --watch`, keine
Polling-Schleife und keine wiederholten Statusabfragen, weil das unnötig
Sitzungs- und Wochenbudget verbraucht.

Wenn der Benutzer den Merge bereits ausdrücklich freigegeben hat und Checks
noch laufen:

1. Nach Möglichkeit Auto-Merge mit der im Repository erlaubten Merge-Strategie
   aktivieren, zum Beispiel `gh pr merge <PR-NUMMER> --auto --squash`.
2. Danach nicht auf GitHub Actions warten und die Checks nicht pollen.
3. Den Benutzer über den ausstehenden automatischen Merge informieren und den
   Arbeitsschritt beenden.

Wenn Auto-Merge nicht verfügbar ist, den einmalig festgestellten ausstehenden
Status melden und auf eine spätere Rückmeldung oder Fortsetzungsanweisung des
Benutzers warten. Erst dann den Status erneut prüfen und den Abschlussworkflow
fortsetzen.

Liegt keine ausdrückliche Merge-Freigabe vor, darf Auto-Merge nicht aktiviert
werden. Dann nur den einmaligen Status melden und auf die Freigabe warten.

### Verbindliche Issue-Abschluss-Checkliste

Ein Issue gilt erst als vollständig abgeschlossen, wenn alle Punkte geprüft wurden:

- [ ] `docs/issues/<issue>.md` aktualisiert
- [ ] `docs/issues/overview.txt` aktualisiert
- [ ] Hilfe-, Info- und Tooltip-Texte bei neuen oder geänderten Funktionen geprüft und bei Bedarf aktualisiert
- [ ] `ai-context.md` bei geändertem dauerhaftem Projektkontext aktualisiert
- [ ] `AI_HANDOFF.md` aktualisiert, falls aktive Arbeit, Implementierungsstand oder abgeschlossene Arbeit dokumentiert werden muss
- [ ] nächstes offenes Issue in `docs/issues/overview.txt` festgelegt
- [ ] Status von Issue-Datei und `docs/issues/overview.txt` ist konsistent
- [ ] GitHub-Issue geschlossen
- [ ] Feature-Branch lokal gelöscht
- [ ] Feature-Branch remote gelöscht
- [ ] Remote-Referenzen bereinigt

Ohne ausdrückliche Nachfrage des Benutzers gilt:

- nicht committen
- nicht pushen
- keinen Pull Request erstellen
- nicht mergen
- kein GitHub-Issue schließen
- keinen Branch löschen

Beispiel für Implementierungsstart:

    git switch main
    git pull
    gh issue list --state all --search "WiFi Connection Service"
    gh issue create --title "WiFi Connection Service" --body-file docs/issues/009-wifi-connection-service.md
    git switch -c wifi-connection-service

`gh issue create` im Beispiel darf nur ausgeführt werden, wenn die vorherige
GitHub-Prüfung kein passendes bestehendes Issue ergeben hat.

Beispiel nach Merge:

    git switch main
    git pull
    git branch -d wifi-connection-service
    git push origin --delete wifi-connection-service
    git fetch --prune
    git status

Der Assistent gibt für alle vom Benutzer lokal auszuführenden Schritte immer die
vollständigen Copy-&-Paste-Befehle aus.
Lokale Befehle werden ohne führende Leerzeichen und ohne Shell-Variablen
ausgegeben, damit sie direkt fehlerfrei kopiert und eingefügt werden können.
Lange einzelne Terminalbefehle werden für direktes Copy & Paste auf mehrere
Zeilen verteilt. Die Befehlszeile und jede fortgesetzte Argumentzeile enden
mit `\`. Nur die letzte Argumentzeile bleibt ohne `\`, damit die Shell den
Befehl ausführt und nicht auf eine weitere Zeile wartet. Das gilt auch für ein
letztes mehrzeiliges Argument wie `--body "..."`: Die Zeile mit dem letzten
Argument erhält kein abschließendes `\`; der Text bleibt bis zum schließenden
Anführungszeichen Teil desselben Arguments. Mehrere eigenständige Befehle sowie
alternative oder bedingte Abläufe werden in getrennten Befehlsblöcken
ausgegeben.
Erklärende Überschriften, Bedingungen und Hinweise zu lokalen Befehlen werden
innerhalb des kopierbaren Terminalblocks als Shell-Kommentare mit `#`
ausgegeben. Das gilt ausdrücklich auch für Hinweise wie „Wenn alle Checks
erfolgreich sind“ oder „Nur bei noch laufenden Checks“. Dadurch kann der
Benutzer den vollständigen Block einschließlich der Erläuterungen direkt in das
Terminal einfügen.

## AI-Handoff

`AI_HANDOFF.md` enthält ausschließlich den aktuellen Übergabestand für die
nächste Session. Frühere Übergabestände werden beim Aktualisieren ersetzt und
nicht angehängt. Die Datei ist keine Historie und kein Changelog; historische
Informationen verbleiben in Git, Pull Requests und den Issue-Dateien.

Die Datei `AI_HANDOFF.md` wird immer direkt im Hauptverzeichnis des
Repositories abgelegt und aktualisiert.

Die wiederverwendbare Startvorlage liegt als `AI_SESSION_PROMPT.md` ebenfalls
im Hauptverzeichnis.

Bei Widersprüchen gilt folgende Priorität:

1. `AGENTS.md`
2. `ai-context.md`
3. `AI_SESSION_PROMPT.md`
4. `AI_HANDOFF.md`
