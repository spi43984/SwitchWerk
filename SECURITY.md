# SECURITY.md

## Sicherheitsziel

Die App steuert lokale Geräte im Heimnetz. Deshalb muss sie verhindern, dass versehentlich Zugangsdaten, interne IPs oder Steuerbefehle nach außen gelangen.

## Sicherheitslücken melden

Sicherheitslücken sollen nicht öffentlich als GitHub Issue mit technischen Details
gemeldet werden.

Bitte verwende bevorzugt GitHubs private Vulnerability-Reporting-Funktion, sofern
sie für dieses Repository aktiviert ist. Falls diese Funktion nicht verfügbar ist,
öffne nur ein allgemeines GitHub Issue ohne technische Details, Zugangsdaten,
interne IP-Adressen, reale Hostnamen, SSIDs, Tokens oder Exploit-Beschreibung und
bitte darin um einen geeigneten privaten Meldeweg.

Normale Fehler, Härtungsvorschläge und nicht-sensitive Sicherheitsverbesserungen
dürfen weiterhin als GitHub Issue gemeldet werden.

## Grundregeln

- Keine Cloud-Kommunikation ohne ausdrückliche Entscheidung.
- Keine Tracker.
- Keine Werbung.
- Keine unnötigen Berechtigungen.
- Keine sensiblen Daten im Log.
- Keine Passwörter oder Tokens im Quellcode.
- Sicherheitskritische One-Shot-Optionen, z. B. Schalter zum Einschließen oder
  Übernehmen von Passwörtern, gelten nur für die unmittelbar folgende Aktion und
  werden nach Abschluss, Abbruch oder Fehler automatisch auf den sicheren
  Default zurückgesetzt.

## Abhängigkeitssicherheit

- GitHub Dependabot Security Alerts mindestens monatlich und vor jedem Release
  prüfen.
- Neue kritische oder hohe Warnungen zeitnah bewerten.
- Bei jeder Warnung zwischen ausgelieferten App-Runtime-Abhängigkeiten und
  reinem Build-/Test-Tooling unterscheiden.
- Sichere Update-Pfade prüfen und notwendige Änderungen eng auf die betroffenen
  Abhängigkeiten begrenzen.
- Behebungen, verbleibende Risiken und begründete Zurückstellungen in einem
  passenden Issue dokumentieren.
- Nach Dependency- oder Toolchain-Änderungen die Dependabot-Auswertung auf
  `main` erneut prüfen.

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

## Code, Dokumentation und Issues

Keine personenbezogenen Daten, privaten Domains, realen Hostnamen, realen SSIDs,
lokalen IP-Adressen oder Zugangsdaten in Code, Dokumentation, Issues, Tests oder
Beispielkonfigurationen schreiben.

Für Beispiele neutrale Platzhalter verwenden:

- Hostname: `server.domain.com`
- lokaler Hostname: `device.local`
- IP-Adresse: `192.0.2.10`
- WLAN-Name: `Example WiFi`

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
