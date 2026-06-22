# TESTING.md

## Minimaler Testansatz

Für diese App reichen zunächst einfache, gezielte Tests.

## Lokale Prüfungen vor Veröffentlichung

GitHub Actions prüft Android-Änderungen nur noch im Pull Request. Vor einer
Veröffentlichung auf dem Ubuntu-Host ausführen:

    ./gradlew lintDebug
    ./gradlew testDebugUnitTest
    ./gradlew clean assembleDebug
    ./gradlew installDebug

`clean` gehört nur zur lokalen, vollständigen Build-Prüfung; GitHub Actions
führt es nicht aus, damit der Gradle-Cache wirksam bleibt.

## Unit Tests

Testen:

- ViewModels
- Repository-Logik
- URL-/Request-Erzeugung
- Fehlerbehandlung

## Manuelle Tests

Vor jedem Release prüfen:

- Gerät einschalten
- Gerät ausschalten
- Gerät toggeln
- Gerät nicht erreichbar
- falsche IP-Adresse
- WLAN aus
- App-Neustart
- Android Dark Mode

## UI Tests

Optional für wichtige Screens:

- Geräteliste wird angezeigt
- Button löst Aktion aus
- Fehler wird angezeigt

## Testgeräte

Mindestens testen auf:

- eigenem Pixel-Gerät
- Emulator
- Shelly-Gerät im lokalen WLAN
