# AI Handoff

Stand: 26. Juni 2026

## Aktueller Stand

Abschluss vorbereitet:

- Issue 032 „Room Schema And Migration Test Coverage“
- GitHub-Issue: #142
- Branch: `room-schema-migration-tests`
- Kein Commit, kein Push, kein Pull Request.
- Lokale Issue-Datei ist auf `Abgeschlossen` gesetzt.
- `docs/issues/overview.txt` ist auf `abgeschlossen` gesetzt.
- Nächstes offenes Issue nach `docs/issues/overview.txt`:
  Issue 037 „GitHub Release Update Support“.

Umgesetzt:

- `app/build.gradle.kts` konfiguriert den Room-Schema-Export über KSP:
  `room.schemaLocation = app/schemas`.
- Aktuelle Room-Schema-Datei für Datenbankversion 8 liegt unter:
  `app/schemas/de.piecha.switchwerk.data.local.AppDatabase/8.json`.
- Neuer Android-Instrumentation-Test:
  `app/src/androidTest/java/de/piecha/switchwerk/data/local/AppDatabaseMigrationTest.kt`.
- Abgedeckt sind:
  - Migration 2 -> 3 mit erhaltenen WLAN-Profilen, Namen aus SSID,
    eindeutigen Namen bei gleicher SSID und Fallback für leere SSID.
  - Migration 3 -> 4 mit erhaltenen WLAN-Profilen beim Tabellenumbau.
  - Migration 4 -> 5 mit Default für `securityTypeVerifiedLocally`.
  - Migration 5 -> 6 mit Default für `connectionMode`.
  - Migration 6 -> 7 mit Default für `apiProtocol`.
  - Migration 7 -> 8 mit Defaults für `apiRequestBody` und
    `apiContentType`.
  - End-to-End-Migration von Version 2 bis zur aktuellen Version 8.

## Prüfungen

Container erfolgreich:

- `./gradlew :app:kspDebugKotlin`
- `./gradlew :app:compileDebugAndroidTestKotlin`
- `./gradlew :app:testDebugUnitTest`
- `./gradlew :app:lintDebug`
- `./gradlew :app:assembleDebug`
- `git diff --check`

Host erfolgreich:

- `./gradlew clean assembleDebug`
- `./gradlew installDebug`
- `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=de.piecha.switchwerk.data.local.AppDatabaseMigrationTest`

Host eingeschränkt:

- Der vollständige Lauf `./gradlew connectedDebugAndroidTest` scheitert in
  bestehenden UI-Tests außerhalb dieses Issues:
  `DeviceWifiProximityIndicatorTest` schlägt auf `Pixel 10 Pro XL - 16` mit
  `NoSuchMethodException: android.hardware.input.InputManager.getInstance`
  aus Espresso/Compose-Testinfrastruktur fehl.

## Start für nächste Codex-Session

1. `AGENTS.md` lesen.
2. `AI_HANDOFF.md` lesen.
3. Für Issue-Arbeit die konkrete Datei unter `docs/issues` lesen.
4. Aktuellen Status mit `git status --short --branch` prüfen.
5. Ohne ausdrückliche Anweisung nicht committen, pushen, PR erstellen, mergen,
   GitHub-Issue schließen oder Branch löschen.
