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
   - `ai-context.md`, wenn dauerhafter Projektkontext oder Issue-Status nötig ist
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

## Branch- und Issue-Workflow

Implementierungen und größere Änderungen werden nie direkt auf `main` begonnen.

Der vollständige GitHub-Workflow ist in `GITHUB_WORKFLOW.md` dokumentiert.

### Phase 1: Vorbereitung und Implementierung

Für jedes fachliche Issue gilt zunächst:

1. Auf `main` wechseln.
2. Aktuellen Stand holen.
3. Prüfen, ob zur passenden Datei unter `docs/issues/*.md` bereits ein
   GitHub-Issue oder Feature-Branch existiert.
4. Nur falls noch kein GitHub-Issue existiert, dieses aus der lokalen
   Issue-Datei erzeugen.
5. Danach einen eigenen Branch mit fachlichem Namen anlegen oder einen
   vorhandenen passenden Branch verwenden, z. B. `wifi-connection-service`.
6. Ausschließlich den vereinbarten Issue-Scope implementieren.
7. Änderungen und Diff prüfen.
8. Nur verfügbare und sinnvolle Prüfungen in der aktuellen Umgebung ausführen.
9. Vollständige Copy-&-Paste-Befehle für Build, Installation und manuelle Tests
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
8. `ai-context.md` aktualisieren:
   - abgeschlossenes Issue in `Abgeschlossen` verschieben
   - abgeschlossenes Issue aus `Offen` entfernen
   - `Nächstes Issue` auf das nächste offene Issue setzen
9. Prüfen, dass `docs/issues/overview.txt`, die lokale Issue-Datei und
   `ai-context.md` denselben Status zeigen.
10. Dokumentationsänderungen committen und pushen.
11. Zugehöriges GitHub-Issue erst danach schließen.
12. Feature-Branch lokal und remote löschen.

### Verbindliche Issue-Abschluss-Checkliste

Ein Issue gilt erst als vollständig abgeschlossen, wenn alle Punkte geprüft wurden:

- [ ] `docs/issues/<issue>.md` aktualisiert
- [ ] `docs/issues/overview.txt` aktualisiert
- [ ] `ai-context.md` aktualisiert
- [ ] `AI_HANDOFF.md` aktualisiert
- [ ] nächstes offenes Issue festgelegt
- [ ] Status der Dokumentationsdateien ist konsistent
- [ ] GitHub-Issue geschlossen

Ohne ausdrückliche Nachfrage des Benutzers gilt:

- nicht committen
- nicht pushen
- keinen Pull Request erstellen
- nicht mergen
- kein GitHub-Issue schließen
- keinen Branch löschen

Beispiel:

    git switch main
    git pull
    gh issue create --title "WiFi Connection Service" --body-file docs/issues/009-wifi-connection-service.md
    git switch -c wifi-connection-service

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
