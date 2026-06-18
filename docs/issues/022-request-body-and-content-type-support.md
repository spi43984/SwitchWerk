# Issue #22: Request Body And Content-Type Support

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

- [ ] Geräteaktionen können einen Request-Body speichern
- [ ] Mehrzeilige Eingabe ist möglich
- [ ] Geräteaktionen speichern einen Content-Type
- [ ] POST-Anfragen übertragen den gespeicherten Body
- [ ] POST-Anfragen übertragen den konfigurierten Content-Type
- [ ] Leere Bodies funktionieren weiterhin
- [ ] Vorhandene Geräte bleiben kompatibel
- [ ] Fehlerhafte Requests werden verständlich angezeigt
- [ ] Request-Bodies werden nicht im Klartext geloggt

## Testhinweise

- POST ohne Body
- POST mit JSON-Body
- POST mit mehrzeiligem JSON
- POST mit text/plain
- POST mit Sonderzeichen
- POST gegen Shelly RPC Endpoint
- GET-Aufruf weiterhin funktionsfähig
- App-Neustart nach Speicherung eines Request-Bodys
