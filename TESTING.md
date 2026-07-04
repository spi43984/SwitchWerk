# TESTING.md

## Minimaler Testansatz

Für diese App reichen zunächst einfache, gezielte Tests.

## Lokale Prüfungen vor Veröffentlichung

GitHub Actions prüft Android-Änderungen nur noch im Pull Request. Vor einer
Veröffentlichung auf dem Ubuntu-Host ausführen. Die Variante für Build und
Installation muss zur aktuell auf dem Testgerät installierten Variante passen.

Debug:

    ./gradlew lintDebug
    ./gradlew testDebugUnitTest
    ./gradlew clean assembleDebug
    ./gradlew installDebug

Release:

    ./gradlew lintRelease
    ./gradlew testDebugUnitTest
    ./gradlew clean assembleRelease
    ./gradlew installRelease

Das Projekt stellt keinen Task `testReleaseUnitTest` bereit. Deshalb werden
auch bei der Release-Prüfung die Unit-Tests mit `testDebugUnitTest` ausgeführt.
`installRelease` setzt die konfigurierte Release-Signierung voraus. Eine bereits
installierte App mit abweichender Signatur muss vor dem Variantenwechsel vom
Testgerät entfernt werden; dabei werden deren lokale App-Daten gelöscht.

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
