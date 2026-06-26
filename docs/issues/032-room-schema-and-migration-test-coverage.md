# Issue #32: Room Schema And Migration Test Coverage

## Metadaten

- Status: Abgeschlossen
- Priorität: P2
- Typ: Tests / Datenbank

## Ziel

Room-Schema-Änderungen und Datenbankmigrationen sollen überprüfbar bleiben.

Die App nutzt Room-Migrationen für lokale Konfigurationsdaten. Diese
Migrationen sollen durch Schema-Dateien und Tests abgesichert werden, damit
künftige Änderungen keine bestehenden Installationen beschädigen.

## Problem

Die Datenbank ist mit `exportSchema = true` konfiguriert, im Repository liegen
aber keine Room-Schema-Dateien.

Außerdem gibt es aktuell keine gezielten Migrationstests für die vorhandenen
Migrationen. Das ursprüngliche Issue fokussierte vor allem die Migration
2 -> 3. Inzwischen existieren weitere Datenbankversionen und Migrationen bis
zur aktuellen Datenbankversion. Der Testumfang muss deshalb alle bestehenden
Migrationen berücksichtigen.

## Scope

- Room-Schema-Export korrekt konfigurieren.
- Room-Schema-Dateien für die aktuellen Datenbankversionen einchecken.
- Migrationstests für alle vorhandenen Migrationen ergänzen.
- Migration 2 -> 3 gezielt prüfen:
  - bestehende WLAN-Profile bleiben erhalten
  - `name` wird aus der SSID erzeugt
  - doppelte SSIDs erzeugen eindeutige Namen
  - leere SSIDs erhalten einen verständlichen Fallback-Namen
- Spätere Migrationen gezielt prüfen:
  - 3 -> 4 erhält WLAN-Profile beim Tabellenumbau
  - 4 -> 5 setzt `securityTypeVerifiedLocally` nachvollziehbar
  - 5 -> 6 setzt `connectionMode` nachvollziehbar
  - 6 -> 7 setzt `apiProtocol` nachvollziehbar
  - 7 -> 8 setzt `apiRequestBody` und `apiContentType` nachvollziehbar
- Einen End-to-End-Migrationstest von der ältesten unterstützten Version bis
  zur aktuellen Datenbankversion ergänzen.
- Prüfen, ob vorhandene Migrationen weiterhin erfolgreich laufen.
- Nur lokal vorbereiten, keine Veröffentlichung ohne ausdrückliche Freigabe.

## Nicht im Scope

- Neue fachliche Datenbankfelder.
- Änderung der WLAN-Verbindungslogik.
- Änderung von Import/Export-Verhalten.
- Änderung des Setup-Wizard-Verhaltens.
- Löschen oder Neuaufsetzen bestehender Benutzerdaten.
- GitHub-Issue oder Pull Request ohne ausdrückliche Freigabe.

## Akzeptanzkriterien

- [x] Room-Schema-Export ist im Gradle-Projekt nachvollziehbar konfiguriert.
- [x] Aktuelle Room-Schema-Dateien liegen im Repository.
- [x] Migrationstest für 2 -> 3 existiert.
- [x] Migration 2 -> 3 erhält vorhandene WLAN-Profile.
- [x] Migration 2 -> 3 erzeugt eindeutige Profilnamen bei gleichen SSIDs.
- [x] Migration 2 -> 3 behandelt leere SSIDs mit Fallback-Namen.
- [x] Migrationstests für alle aktuell vorhandenen späteren Migrationen existieren.
- [x] Ein End-to-End-Migrationstest bis zur aktuellen Datenbankversion existiert.
- [x] Bestehende Unit-Tests laufen weiterhin erfolgreich.

## Umsetzung

- Room-Schema-Export über KSP nach `app/schemas` konfiguriert.
- Aktuelles Room-Schema für Datenbankversion 8 eingecheckt.
- Android-Instrumentation-Test `AppDatabaseMigrationTest` ergänzt.
- Migrationen 2 -> 3, 3 -> 4, 4 -> 5, 5 -> 6, 6 -> 7 und 7 -> 8 gezielt
  geprüft.
- End-to-End-Migration von Version 2 bis Version 8 geprüft.

## Prüfergebnis

- Container:
  - `./gradlew :app:kspDebugKotlin`
  - `./gradlew :app:compileDebugAndroidTestKotlin`
  - `./gradlew :app:testDebugUnitTest`
  - `./gradlew :app:lintDebug`
  - `./gradlew :app:assembleDebug`
  - `git diff --check`
- Host:
  - `./gradlew clean assembleDebug`
  - `./gradlew installDebug`
  - `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=de.piecha.switchwerk.data.local.AppDatabaseMigrationTest`

Der vollständige Host-Lauf `./gradlew connectedDebugAndroidTest` ist durch
bestehende UI-Tests außerhalb dieses Issues blockiert
(`DeviceWifiProximityIndicatorTest`,
`NoSuchMethodException: android.hardware.input.InputManager.getInstance`).
Die neuen Room-Migrationstests liefen gezielt erfolgreich.

## Testhinweise

- Migrationstest mit zwei Profilen gleicher SSID ausführen.
- Migrationstest mit leerer SSID ausführen.
- Migrationstests für vorhandene Geräte- und WLAN-Profil-Felder ausführen.
- End-to-End-Migration von der ältesten unterstützten Version bis zur aktuellen
  Datenbankversion ausführen.
- Vollständigen Android-Build auf dem Host prüfen lassen, wenn Code geändert
  wurde.
