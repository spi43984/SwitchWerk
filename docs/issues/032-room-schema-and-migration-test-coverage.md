# Issue #32: Room Schema And Migration Test Coverage

## Metadaten

- Status: Offen
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

Außerdem gibt es aktuell keine gezielten Migrationstests für die Migrationen,
insbesondere für die Erweiterung der WLAN-Profile um den eindeutigen Namen.

## Scope

- Room-Schema-Export korrekt konfigurieren.
- Room-Schema-Dateien für die aktuellen Datenbankversionen einchecken.
- Migrationstest für bestehende Versionen ergänzen.
- Migration 2 -> 3 prüfen:
  - bestehende WLAN-Profile bleiben erhalten
  - `name` wird aus der SSID erzeugt
  - doppelte SSIDs erzeugen eindeutige Namen
  - leere SSIDs erhalten einen verständlichen Fallback-Namen
- Prüfen, ob vorhandene Migrationen weiterhin erfolgreich laufen.
- Nur lokal vorbereiten, keine Veröffentlichung ohne ausdrückliche Freigabe.

## Nicht im Scope

- Neue fachliche Datenbankfelder.
- Änderung der WLAN-Verbindungslogik.
- Änderung von Import/Export-Verhalten.
- Löschen oder Neuaufsetzen bestehender Benutzerdaten.
- GitHub-Issue oder Pull Request ohne ausdrückliche Freigabe.

## Akzeptanzkriterien

- [ ] Room-Schema-Export ist im Gradle-Projekt nachvollziehbar konfiguriert.
- [ ] Aktuelle Room-Schema-Dateien liegen im Repository.
- [ ] Migrationstest für 2 -> 3 existiert.
- [ ] Migration 2 -> 3 erhält vorhandene WLAN-Profile.
- [ ] Migration 2 -> 3 erzeugt eindeutige Profilnamen bei gleichen SSIDs.
- [ ] Migration 2 -> 3 behandelt leere SSIDs mit Fallback-Namen.
- [ ] Bestehende Unit-Tests laufen weiterhin erfolgreich.

## Testhinweise

- Migrationstest mit zwei Profilen gleicher SSID ausführen.
- Migrationstest mit leerer SSID ausführen.
- `./gradlew testDebugUnitTest` im Container ausführen, sofern verfügbar.
- Vollständigen Android-Build auf dem Host prüfen lassen, wenn Code geändert
  wurde.
