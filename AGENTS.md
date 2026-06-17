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

## Branch- und Issue-Workflow

[... bestehender Inhalt unverändert ...]

## Verbindliche Issue-Abschluss-Checkliste

Ein Issue gilt erst als vollständig abgeschlossen, wenn alle Punkte geprüft wurden:

- [ ] docs/issues/<issue>.md aktualisiert
- [ ] docs/issues/overview.txt aktualisiert
- [ ] ai-context.md aktualisiert
- [ ] AI_HANDOFF.md aktualisiert
- [ ] nächstes offenes Issue festgelegt
- [ ] Status der Dokumentationsdateien ist konsistent
- [ ] GitHub-Issue geschlossen

## AI-Handoff

Die Datei `AI_HANDOFF.md` wird immer direkt im Hauptverzeichnis des
Repositories abgelegt und aktualisiert.
