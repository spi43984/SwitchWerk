# AI Handoff

Stand: 17. Juni 2026

## Aktuelle Arbeit

Issue 014 "Dashboard Device Reordering" ist im Branch
`dashboard-device-reordering` implementiert und auf dem Host gebaut, aber noch
nicht committet und nicht veröffentlicht.

- GitHub-Issue: #30
- Dashboard-Geräte können per Hoch-/Runter-Aktion umsortiert werden
- Drag & Drop wurde nicht umgesetzt; die Bedienung erfolgt bewusst über Pfeile
- Reihenfolge wird über `sortOrder` in Room gespeichert
- Room-Datenbankversion wurde auf 2 erhöht und Migration 1 -> 2 ergänzt
- Container-Prüfungen: `./gradlew testDebugUnitTest`, `git diff --check`
- Host-Prüfung laut Benutzer erfolgreich:

```text
./gradlew clean assembleDebug
./gradlew installDebug
```

## Abgeschlossene Arbeit

Issue 012 "Import / Export" ist implementiert, geprüft, veröffentlicht und nach
`main` gemergt.

- GitHub-Issue: #25
- Pull Request: #26
- Implementierungs-Commit: `a67232db728808aadedb62bba6a88e69bcf790a2`
- Merge-Commit: `0f4fd413323d33e0a1fe8a12537f78d5491e43c8`

Issue 013 "QR Code Import" ist implementiert, geprüft, veröffentlicht und nach
`main` gemergt.

- Pull Request: #28
- Implementierungs-Commit: `30a736ee7a8a8e1129148694f64d9263700cb471`
- Merge-Commit: `1fe04e6e152048919c69afeaa144736c11d57c78`

## Implementierter Scope

- versioniertes JSON-Austauschformat mit `schemaVersion = 1`
- Export ohne Passwörter sowie optionaler Klartext-Passwortexport mit Warnung
- Dateiimport und Import aus direkten HTTPS-URLs
- öffentliche Nextcloud- und Google-Drive-Freigabelinks
- Importmodi "Ergänzen / überschreiben" und "Alles ersetzen"
- Importzusammenfassung und zusätzliche Passwortwarnung
- sichere Ablage importierter Passwörter im bestehenden Credential Store
- Erhalt der WLAN-Zuordnungsreihenfolge
- strikte Importvalidierung und Größenlimit
- QR-Code-Import für HTTPS-Importquellen

## Bestätigte Prüfungen

```text
./gradlew testDebugUnitTest
./gradlew clean assembleDebug
./gradlew installDebug
git diff --check
```

Zusätzlich erfolgreich manuell geprüft:

- Export und Dateiimport
- Nextcloud-Import
- öffentlich freigegebener Google-Drive-Import
- QR-Code-Import
- Installation und Start auf Pixel 10 Pro XL mit Android 16

GitHub Actions "Android Build" für PR #26 und PR #28 waren erfolgreich.

## Offener Folgeschritt

Authentifizierte Importquellen sind nicht Bestandteil von Issue #25. Die
separate Planung liegt unter:

```text
docs/issues/024-authenticated-import-sources.md
```

## Nächste geplante Themen

```text
023 Settings Display And Action Details
024 Authenticated Import Sources Backlog
025 Dashboard Widget Layout
026 Settings UI Rework
```
