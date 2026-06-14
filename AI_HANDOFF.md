# AI Handoff

Stand: 14. Juni 2026

## Abgeschlossene Arbeit

Issue 012 "Import / Export" ist implementiert, geprüft, veröffentlicht und nach
`main` gemergt.

- GitHub-Issue: #25
- Pull Request: #26
- Implementierungs-Commit: `a67232db728808aadedb62bba6a88e69bcf790a2`
- Merge-Commit: `0f4fd413323d33e0a1fe8a12537f78d5491e43c8`

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
- Installation und Start auf Pixel 10 Pro XL mit Android 16

GitHub Actions "Android Build" für PR #26 war erfolgreich.

## Offener Folgeschritt

Authentifizierte Importquellen sind nicht Bestandteil von Issue #25. Die
separate Planung liegt unter:

```text
docs/issues/024-authenticated-import-sources.md
```
