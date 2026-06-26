# AI Handoff

Stand: 26. Juni 2026

## Aktueller Stand

Abschluss durchgeführt:

- Issue 037 „GitHub Release Update Support“
- GitHub-Issue: #144
- Pull Request: #145
- Feature-Branch: `github-release-update-support`
- Merge-Commit auf `main`: `489476c1064c02217cd2a122b2097b563797c0d7`

Umgesetzt und gemergt:

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
- Einstellungen zeigen installierte Version, verfügbare Version, Release Notes,
  letzte Prüfung, Fehlerstatus und Downloadfortschritt.
- Dashboard zeigt einen kompakten Hinweis, wenn ein Update verfügbar ist.
- Hamburger-Menü enthält `Updates` bzw. bei verfügbarem Update `Update verfügbar`
  und führt direkt zu `Einstellungen -> System`.
- Hilfe- und Info-Texte wurden für Deutsch und Englisch um Updates ergänzt.
- Release-Skript `scripts/release-github.sh` erzwingt das Asset-Format
  `SwitchWerk-<version>.apk`.
- Unit-Tests für Versionsvergleich, Release-Auswertung, Asset-Auswahl und
  Cache-Logik ergänzt.

## Prüfungen

Container erfolgreich:

- `./gradlew lintDebug`
- `./gradlew testDebugUnitTest`
- `./gradlew clean assembleDebug`
- `./gradlew assembleDebug`
- `bash -n scripts/release-github.sh`
- `git diff --check`

Host erfolgreich laut Benutzer:

- `./gradlew clean assembleDebug`
- `./gradlew installDebug`

Manuell erfolgreich laut Benutzer:

- Release-Build-Test ohne neues GitHub-Release.
- Hamburger-Menü `Updates` öffnet `Einstellungen -> System`.
- Update-Fehler wird nur noch im Update-Bereich angezeigt.

## Nächste Session

Nächstes offenes Issue nach `docs/issues/overview.txt`:

1. Issue 041 „Dashboard Drag And Drop“

Ohne ausdrückliche Anweisung nicht committen, pushen, PR erstellen, mergen,
GitHub-Issue schließen oder Branch löschen.
