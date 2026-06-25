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
    Stattdessen neutrale Platzhalter verwenden, z. B. `server.domain.con`,
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
4. Prüfen, ob zur passenden Datei unter `docs/issues/*.md` bereits ein
   GitHub-Issue oder Feature-Branch existiert.
5. Nur falls noch kein GitHub-Issue existiert, dieses aus der lokalen
   Issue-Datei erzeugen.
6. Danach einen eigenen Branch mit fachlichem Namen anlegen oder einen
   vorhandenen passenden Branch verwenden, z. B. `wifi-connection-service`.
7. Ausschließlich den vereinbarten Issue-Scope implementieren.
8. Änderungen und Diff prüfen.
9. Nur verfügbare und sinnvolle Prüfungen in der aktuellen Umgebung ausführen.
10. Vollständige Copy-&-Paste-Befehle für Build, Installation und manuelle Tests
    auf dem Host ausgeben.

Mindestens auf dem Host zu prüfen:

    ./gradlew clean assembleDebug
    ./gradlew installDebug

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

### Verbindliche Issue-Abschluss-Checkliste

Ein Issue gilt erst als vollständig abgeschlossen, wenn alle Punkte geprüft wurden:

- [ ] `docs/issues/<issue>.md` aktualisiert
- [ ] `docs/issues/overview.txt` aktualisiert
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
    gh issue create --title "WiFi Connection Service" --body-file docs/issues/009-wifi-connection-service.md
    git switch -c wifi-connection-service

Beispiel nach Merge:

    git switch main
    git pull
    git branch -d wifi-connection-service
    git push origin --delete wifi-connection-service
    git fetch --prune
    git status

Der Assistent gibt für alle vom Benutzer lokal auszuführenden Schritte immer die
vollständigen Copy-&-Paste-Befehle aus.

## AI-Handoff

Die Datei `AI_HANDOFF.md` wird immer direkt im Hauptverzeichnis des
Repositories abgelegt und aktualisiert.

Die wiederverwendbare Startvorlage liegt als `AI_SESSION_PROMPT.md` ebenfalls
im Hauptverzeichnis.

Bei Widersprüchen gilt folgende Priorität:

1. `AGENTS.md`
2. `ai-context.md`
3. `AI_SESSION_PROMPT.md`
4. `AI_HANDOFF.md`
