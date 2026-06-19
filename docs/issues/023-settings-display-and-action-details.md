# Issue #23: Settings Display And Action Details

## Metadaten

- Status: Abgeschlossen
- Priorität: P0
- Typ: GUI / Diagnose

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
- Sortierung auswählbar:
  - Neueste Meldung oben
  - Neueste Meldung unten
- Standard ist „Neueste Meldung oben“
- Sortierung wird dauerhaft gespeichert
- Aktionsprotokoll kann über einen Mülleimer-Button geleert werden
- Diagnosemeldungen bleiben ausschließlich im Arbeitsspeicher

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
- Bei neuen Meldungen wird automatisch zum neuesten Eintrag gescrollt
- Sortierreihenfolge kann auch direkt im Detailbereich umgeschaltet werden
- Neue Geräteaktionen werden durch einen optischen Trenner voneinander getrennt

### Ereignisanzeige

Während einer Geräteaktion werden verständliche Statusmeldungen angezeigt, z. B.

- Geräteaktion „Gerätename“ gestartet
- WLAN-Profil „Shelly AP“ wird verbunden
- WLAN-Verbindung erfolgreich
- WLAN-Verbindung fehlgeschlagen
- konfigurierte Geräteadresse als IP-Adresse oder DNS-Name
- HTTP GET wird an der Geräteadresse ausgeführt
- HTTP POST wird an der Geräteadresse ausgeführt
- HTTP-Antwort mit Statuscode
- DNS-Name konnte nicht aufgelöst werden
- IP-Adresse ist nicht erreichbar
- Gerät erfolgreich geschaltet
- Verbindung nicht möglich
- Anfrage fehlgeschlagen
- Geräteaktion abgeschlossen

### Import und Export der App-Einstellungen

Folgende App-Einstellungen werden mit der Konfiguration exportiert und beim
Import übernommen:

- Darstellung
- Detailanzeige ein/aus
- Höhe des Detailbereichs
- Sortierreihenfolge

Das Exportformat verwendet dafür Schema-Version 2. Schema-Version 1 bleibt
importierbar. Fehlt der App-Einstellungsblock in einer Importdatei, bleiben die
aktuellen lokalen App-Einstellungen unverändert.

## Nicht im Scope

- Persistentes Log über App-Neustarts hinweg
- Export der Meldungen
- Cloud-Synchronisation
- Erweiterte Diagnosefunktionen
- Entwickleroptionen
- Speicherung sensibler Netzwerkdaten

## Sicherheitsanforderungen

Es dürfen niemals angezeigt oder gespeichert werden:

- WLAN-Zugangsdaten
- Tokens
- API-Keys
- vollständige Authorization-Header
- vollständige Request-Payloads

Die Meldungen müssen für normale Benutzer verständlich formuliert sein.

## Akzeptanzkriterien

- [x] Auswahl Systemvorgabe/Hell/Dunkel vorhanden
- [x] Auswahl wird dauerhaft gespeichert
- [x] Theme wird beim App-Start korrekt angewendet
- [x] Schalter für Detailanzeige vorhanden
- [x] Einstellung wird dauerhaft gespeichert
- [x] Dashboard zeigt Detailbereich nur bei aktivierter Option
- [x] Höhe des Detailbereichs ist konfigurierbar
- [x] Statusmeldungen werden während einer Geräteaktion angezeigt
- [x] Bereich ist scrollbar
- [x] Neue Geräteaktionen sind optisch getrennt
- [x] Gerätename, Geräteadresse, HTTP-Methode und HTTP-Status werden verständlich angezeigt
- [x] DNS-Auflösungsfehler und nicht erreichbare IP-Adressen sind unterscheidbar
- [x] Sortierung Neueste oben/unten ist auswählbar und dauerhaft gespeichert
- [x] Neueste oben ist die Standardeinstellung
- [x] Detailbereich scrollt automatisch zur neuesten Meldung
- [x] Aktionsprotokoll kann im UI geleert werden
- [x] App-Einstellungen werden exportiert und importiert
- [x] Importdateien ohne App-Einstellungen überschreiben lokale Einstellungen nicht
- [x] Keine sensiblen Daten werden angezeigt
- [x] Build erfolgreich

## Testhinweise

- Wechsel zwischen Hell und Dunkel
- Wechsel auf Systemvorgabe
- App-Neustart nach Änderung
- Detailanzeige ein-/ausschalten
- Verschiedene Höhen testen
- Erfolgreiche Geräteaktion beobachten
- Fehlerhafte WLAN-Verbindung beobachten
- Fehlerhaften HTTP-Aufruf beobachten
- Prüfen, dass keine Zugangsdaten oder Tokens sichtbar sind
