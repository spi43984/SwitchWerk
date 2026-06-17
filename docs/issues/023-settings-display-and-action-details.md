# Issue #23: Settings Display And Action Details

## Ziel

Die App soll konfigurierbare Darstellungsoptionen erhalten und auf Wunsch
detaillierte Informationen über laufende Geräteaktionen anzeigen.

## Hintergrund

Für Fehlersuche, Einrichtung und Betrieb soll nachvollziehbar sein, welche
Schritte bei einer Geräteaktion ausgeführt werden.

Zusätzlich soll die App unabhängig von der Android-Systemeinstellung in einem
festen hellen oder dunklen Design betrieben werden können.

## Scope

### Darstellung

- Auswahl der App-Darstellung:
  - Systemvorgabe
  - Hell
  - Dunkel
- Auswahl wird dauerhaft gespeichert
- Darstellung wird beim App-Start automatisch angewendet

### Detaillierte Anzeige

- Schalter zum Ein-/Ausschalten der Detailanzeige
- Einstellung wird dauerhaft gespeichert

### Zeitstempel in Diagnosemeldungen

Alle Diagnosemeldungen sollen einen Zeitstempel im Format `HH:mm:ss.SSS`
erhalten.

Beispiel:

12:00:01.123 Aktion gestartet
12:00:01.456 WLAN "Shelly-AP" angefordert
12:00:03.012 WLAN verbunden
12:00:03.987 IP-Adresse erhalten
12:00:04.102 HTTP GET gestartet
12:00:04.284 HTTP GET erfolgreich

Ziel:

- Analyse von Verbindungsproblemen
- Erkennen von Verzögerungen beim WLAN-Wechsel
- Erkennen von DHCP-Problemen
- Erkennen von HTTP-Timeouts
- Nachvollziehbarkeit für Anwender und Entwickler

### Detailbereich im Dashboard

- Optionaler Detailbereich am unteren Rand des Dashboards
- Höhe konfigurierbar:
  - 20 %
  - 30 %
  - 40 %
- Bereich ist scrollbar

### Ereignisanzeige

Während einer Geräteaktion werden verständliche Statusmeldungen angezeigt, z. B.

- Geräteaktion gestartet
- WLAN-Profil „Shelly AP“ wird verbunden
- WLAN-Verbindung erfolgreich
- WLAN-Verbindung fehlgeschlagen
- HTTP GET wird ausgeführt
- HTTP POST wird ausgeführt
- Gerät erfolgreich geschaltet
- Verbindung nicht möglich
- Anfrage fehlgeschlagen
- Geräteaktion abgeschlossen

## Nicht im Scope

- Persistentes Log über App-Neustarts hinweg
- Export der Meldungen
- Cloud-Synchronisation
- Erweiterte Diagnosefunktionen
- Entwickleroptionen
- Speicherung sensibler Netzwerkdaten

## Sicherheitsanforderungen

Es dürfen niemals angezeigt oder gespeichert werden:

- WLAN-Passwörter
- Tokens
- API-Keys
- vollständige Authorization-Header
- vollständige Request-Payloads

Die Meldungen müssen für normale Benutzer verständlich formuliert sein.

## Akzeptanzkriterien

- [ ] Auswahl Systemvorgabe/Hell/Dunkel vorhanden
- [ ] Auswahl wird dauerhaft gespeichert
- [ ] Theme wird beim App-Start korrekt angewendet
- [ ] Schalter für Detailanzeige vorhanden
- [ ] Einstellung wird dauerhaft gespeichert
- [ ] Dashboard zeigt Detailbereich nur bei aktivierter Option
- [ ] Höhe des Detailbereichs ist konfigurierbar
- [ ] Statusmeldungen werden während einer Geräteaktion angezeigt
- [ ] Bereich ist scrollbar
- [ ] Keine sensiblen Daten werden angezeigt
- [ ] Build erfolgreich

## Testhinweise

- Wechsel zwischen Hell und Dunkel
- Wechsel auf Systemvorgabe
- App-Neustart nach Änderung
- Detailanzeige ein-/ausschalten
- Verschiedene Höhen testen
- Erfolgreiche Geräteaktion beobachten
- Fehlerhafte WLAN-Verbindung beobachten
- Fehlerhaften HTTP-Aufruf beobachten
- Prüfen, dass keine Passwörter oder Tokens sichtbar sind
