# Issue #22: Request Body And Content-Type Support

## Metadaten

- Status: Abgeschlossen
- Priorität: P2
- Typ: Geräteaktion / API

## Ziel

Geräteaktionen sollen neben einfachen GET- und POST-Aufrufen auch
konfigurierbare Request-Bodies unterstützen.

Dadurch können REST- und RPC-Geräte mit JSON-Payloads und anderen
Content-Types angesprochen werden.

## Scope

- Geräteaktionen können optional einen Request-Body speichern
- mehrzeiliges Eingabefeld für den Request-Body
- Geräteaktionen speichern einen Content-Type
- Content-Type kann ausgewählt werden
- POST-Anfragen senden den gespeicherten Body
- POST-Anfragen senden den konfigurierten Content-Type
- leerer Body bleibt weiterhin zulässig
- vorhandene Geräte bleiben kompatibel

## Unterstützte Content-Types

- application/json
- text/plain

Weitere Content-Types können später ergänzt werden.

## Nicht im Scope

- JSON-Editor
- Syntax-Highlighting
- JSON-Schema-Validierung
- XML-Unterstützung
- Multipart Uploads
- Binärdaten
- Template-Systeme
- Variablenersetzung

## Akzeptanzkriterien

- [x] Geräteaktionen können einen Request-Body speichern
- [x] Mehrzeilige Eingabe ist möglich
- [x] Geräteaktionen speichern einen Content-Type
- [x] POST-Anfragen übertragen den gespeicherten Body
- [x] POST-Anfragen übertragen den konfigurierten Content-Type
- [x] Leere Bodies funktionieren weiterhin
- [x] Vorhandene Geräte bleiben kompatibel
- [x] Fehlerhafte Requests werden verständlich angezeigt
- [x] Request-Bodies werden nicht im Klartext geloggt

## Abschlussnotizen

- GitHub-Issue: #127
- Geräteaktionen speichern optionalen Request-Body und Content-Type.
- Unterstützte Content-Types: `application/json` und `text/plain`.
- Content-Type-Auswahl ist in der UI nur bei `POST` aktiv; bei `GET` bleibt sie
  sichtbar, aber deaktiviert.
- `application/json` ist der Standardwert.
- Room-Migration 7 -> 8 ergänzt leeren Body und `APPLICATION_JSON` für
  vorhandene Geräte.
- Import/Export-Schema-Version 4 speichert Request-Body und Content-Type;
  ältere Konfigurationen bleiben kompatibel.
- Hilfe-, Info- und Tooltip-Texte wurden geprüft; relevante Hilfe-/Infotexte
  im Gerätebereich wurden aktualisiert.
- Request-Bodies werden nicht geloggt.

## Testhinweise

- POST ohne Body
- POST mit JSON-Body
- POST mit mehrzeiligem JSON
- POST mit text/plain
- POST mit Sonderzeichen
- POST gegen Shelly RPC Endpoint
- GET-Aufruf weiterhin funktionsfähig
- App-Neustart nach Speicherung eines Request-Bodys
