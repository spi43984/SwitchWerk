# AI Handoff

Stand: 26. Juni 2026

## Aktueller Stand

In Arbeit:

- Issue 037 „GitHub Release Update Support“
- GitHub-Issue: #144
- Branch: `github-release-update-support`
- Kein Commit, kein Push, kein Pull Request.
- Lokale Issue-Datei und `docs/issues/overview.txt` sind nicht abgeschlossen.

Umgesetzt:

- Update-Logik außerhalb Compose unter `data/update`.
- GitHub Releases API für `spi43984/SwitchWerk`.
- Pre-Releases und Drafts werden ignoriert.
- APK-Asset wird verbindlich über `SwitchWerk-<version>.apk` erkannt.
- Versionsvergleich für semantische Versionen.
- SharedPreferences-Cache mit letzter Prüfung und Tagesbegrenzung für automatische Checks.
- Manuelle Prüfung in den Einstellungen umgeht die Tagesbegrenzung.
- Debug-Builds werden nicht als updatefähige Release-Version behandelt.
- Download-Service speichert APK über app-eigenen Download-Bereich und FileProvider.
- Android Package Installer wird nur per Benutzer-Intent geöffnet.
- Einstellungen zeigen installierte Version, verfügbare Version, Release Notes, letzte Prüfung, Fehlerstatus und Downloadfortschritt.
- Dashboard zeigt einen kompakten Hinweis, wenn ein Update verfügbar ist.
- Hamburger-Menü enthält einen Eintrag `Updates`, der direkt zu
  `Einstellungen -> System` führt. Wenn ein Update verfügbar ist, lautet der
  Menüeintrag `Update verfügbar`.
- Hilfe- und Info-Texte wurden für Deutsch und Englisch um Updates ergänzt.
- Update-Check- und Downloadfehler werden nur im Update-Bereich angezeigt,
  nicht zusätzlich oben im allgemeinen Einstellungs-Fehlerbereich.
- Release-Dokumentation enthält ein korrigiertes Veröffentlichungsskript mit Asset-Name `SwitchWerk-${VERSION}.apk`.
- Neues versioniertes Release-Skript:
  `scripts/release-github.sh`.
  Es fragt die Version interaktiv ab oder akzeptiert sie als Argument,
  validiert `MAJOR.MINOR.PATCH`, prüft vorhandene Tags und GitHub-Releases,
  prüft `keystore.properties`, verifiziert die APK mit `apksigner` und lädt das
  Asset als `SwitchWerk-<version>.apk` hoch.
- Unit-Tests für Versionsvergleich, Release-Auswertung, Asset-Auswahl und Cache-Logik ergänzt.

## Prüfungen im Container

Erfolgreich:

- `./gradlew lintDebug`
- `./gradlew testDebugUnitTest`
- `./gradlew clean assembleDebug`
- Nach Menü-/Hilfetext-Ergänzung zusätzlich: `./gradlew assembleDebug`
- Nach Entfernen des doppelten Update-Fehlerhinweises zusätzlich:
  `./gradlew testDebugUnitTest`, `./gradlew assembleDebug`
- `git diff --check`
- `bash -n scripts/release-github.sh`

Keystore-Prüfung:

- `keystore.properties` existiert.
- Erwartete Schlüssel sind vorhanden: `storeFile`, `storePassword`,
  `keyAlias`, `keyPassword`.
- `keystore.properties` und Keystore-Dateiendungen werden von `.gitignore`
  ignoriert.
- Die in `keystore.properties` referenzierte Keystore-Datei war im Container
  nicht erreichbar. Auf dem Ubuntu-Host muss geprüft werden, ob der Pfad dort
  existiert.

Hinweise:

- `assembleDebug` meldet bestehende Deprecation-Warnungen zu
  `EncryptedSharedPreferences` / `MasterKey`, `WindowInsetsControllerCompat`
  und `LocalLifecycleOwner`.
- Die maßgebliche Bestätigung für Host-Build, Installation und manuelle Tests
  steht noch aus.

## Nächste Schritte

1. Auf dem Ubuntu-Host prüfen:
   `./gradlew clean assembleDebug`
2. Auf dem Ubuntu-Host installieren:
   `./gradlew installDebug`
3. Manuell prüfen:
   - Einstellungen öffnen.
   - System → Updates prüfen.
   - Debug-Hinweis kontrollieren.
   - Release-Build später mit GitHub-Release-Asset `SwitchWerk-<version>.apk`
     gegen eine ältere installierte Version testen.
4. Erst nach Host-Prüfung und ausdrücklicher Freigabe committen, pushen oder PR
   erstellen.
