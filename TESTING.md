# TESTING.md

## Minimaler Testansatz

Für diese App reichen zunächst einfache, gezielte Tests.

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
