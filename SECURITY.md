# SECURITY.md

## Sicherheitsziel

Die App steuert lokale Geräte im Heimnetz. Deshalb muss sie verhindern, dass versehentlich Zugangsdaten, interne IPs oder Steuerbefehle nach außen gelangen.

## Grundregeln

- Keine Cloud-Kommunikation ohne ausdrückliche Entscheidung.
- Keine Tracker.
- Keine Werbung.
- Keine unnötigen Berechtigungen.
- Keine sensiblen Daten im Log.
- Keine Passwörter oder Tokens im Quellcode.

## Netzwerk

- Lokale HTTP-Aufrufe zu Shelly-Geräten sind erlaubt.
- HTTPS bevorzugen, wenn das Gerät es unterstützt.
- Keine unsicheren externen API-Endpunkte einbauen.
- Timeouts setzen.
- Fehler sauber behandeln.

## Logging

Nie loggen:

- Passwörter
- Tokens
- vollständige Authorization Header
- persönliche Daten
- sensible Gerätekonfiguration

## Speicherung

Unkritisch:

- Gerätename
- Raum
- Gerätetyp
- lokale IP-Adresse

Sensibel:

- Passwörter
- Tokens
- API Keys

Sensible Daten mit Android Keystore / verschlüsselter Speicherung schützen.

## Android-Berechtigungen

Nur anfordern, wenn wirklich nötig.

Typisch nötig:

- INTERNET
- ACCESS_NETWORK_STATE

Nicht ohne Rückfrage:

- Standort
- Kontakte
- SMS
- Mikrofon
- Kamera
- Hintergrund-Standort

## Shelly-Steuerung

Bei Steuerbefehlen:

- klare Fehleranzeige
- keine stillen Wiederholungen bei kritischen Aktionen
- kurze Timeouts
- Status nach Möglichkeit erneut prüfen
