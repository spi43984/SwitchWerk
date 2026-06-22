# Issue #44: GitHub Actions Resource Optimization

## Metadaten

- Status: Offen
- Priorität: P0
- Typ: CI / GitHub Actions / Build-Kosten
- Bereich: GitHub Workflows / Lokale Prüfungen / Release-Build

## Ziel

Der GitHub Actions Verbrauch soll reduziert werden, damit ausreichend Kontingent
für wichtige Builds, Pull Requests, Releases und APK-Erzeugung verfügbar bleibt.

Die Entwicklungsarbeit erfolgt primär lokal auf dem Ubuntu-Host. GitHub Actions
sollen nur noch notwendige Minimalprüfungen ausführen.

## Hintergrund

Aktuell werden bei vielen Änderungen vollständige Android-Builds und weitere
Prüfungen ausgeführt.

Dadurch wird unnötig GitHub Actions Rechenzeit verbraucht, obwohl:

- lokale Builds regelmäßig durchgeführt werden
- Gerätetests lokal erfolgen
- Installationen lokal erfolgen
- viele Änderungen reine Dokumentationsänderungen sind

## Scope

### Workflow-Analyse

Vorhandene Workflows analysieren:

- Trigger
- Build-Schritte
- Lint-Schritte
- Test-Schritte
- Release-Schritte
- Artefakt-Erzeugung
- Caching

### CI-Minimierung

Prüfen und umsetzen:

- keine vollständigen Android-Builds bei reinen Dokumentationsänderungen
- keine Android-Builds bei Änderungen ausschließlich unter docs/
- keine Android-Builds bei Änderungen ausschließlich an Markdown-Dateien
- Push-Builds auf Feature-Branches kritisch prüfen
- PR- und Release-Builds bevorzugen
- doppelte oder überlappende Gradle-Prüfungen vermeiden
- clean in GitHub Actions vermeiden, sofern nicht fachlich nötig

### Lokale Prüfungen bevorzugen

Dokumentation ergänzen, welche Prüfungen lokal vor Veröffentlichung auszuführen sind:

./gradlew lintDebug
./gradlew testDebugUnitTest
./gradlew clean assembleDebug
./gradlew installDebug

### Release-Schutz

APK-Erzeugung und Releases müssen weiterhin zuverlässig funktionieren.

Prüfen:

- Release nur bei Tags
- Release nur manuell
- Release nur auf main
- Artefakt-Upload bleibt erhalten

### Caching

Prüfen und bei Bedarf verbessern:

- Gradle Cache
- Dependency Cache
- Android Build Cache

## Nicht im Scope

- Änderungen an der App-Funktionalität
- Änderungen an Netzwerklogik
- Änderungen an WLAN-Verbindungen
- Änderungen an Geräteaktionen
- Änderungen an Berechtigungen
- Änderungen an Persistenz oder Datenmodell

## Akzeptanzkriterien

- [ ] GitHub Actions Verbrauch ist reduziert.
- [ ] Dokumentationsänderungen starten keine Android-Builds.
- [ ] Markdown-only Änderungen starten keine Android-Builds.
- [ ] Release-Prozess bleibt funktionsfähig.
- [ ] APK-Erzeugung für Releases bleibt möglich.
- [ ] Lokale Lint-, Test-, Build- und Installationsprüfungen sind dokumentiert.
- [ ] AGENTS.md, GITHUB_WORKFLOW.md, TESTING.md und AI_SESSION_PROMPT.md sind geprüft und bei Bedarf aktualisiert.
- [ ] Keine App-Funktionalität wurde verändert.
