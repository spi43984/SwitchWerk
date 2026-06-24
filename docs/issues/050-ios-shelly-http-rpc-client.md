# Issue 050 iOS: Shelly HTTP/RPC Client

## Metadaten

- Status: Backlog
- Priorität: P4
- Plattform: iOS
- iOS-Phase: 5 von 6
- Typ: Netzwerk / Planung

## Ziel

Eine iOS-Netzwerkschicht für lokale Shelly-HTTP- und RPC-Aufrufe planen. Sie entspricht fachlich der Android-Netzwerkschicht, verwendet jedoch URLSession und iOS-spezifische Netzwerkmechanismen.

## Scope

- HTTP-GET- und HTTP-POST-Anfragen sowie Shelly-RPC-Aufrufe.
- Request-Erzeugung aus konfigurierten Geräten und Kommando-Templates.
- Verbindungs-, Request- und Gesamt-Timeouts.
- Strukturierte Fehlerbehandlung für DNS-, Verbindungs-, TLS-, HTTP- und RPC-Fehler.
- Datenschutzgerechte Diagnose ohne SSIDs, Passwörter, Tokens, vollständige URLs oder lokale IP-Adressen in Logs.
- Verhalten auf Geräte-APs ohne Internet sowie die Ergebnisse der Machbarkeitsprüfung aus Issue 047.

## Architekturhinweise

- Views und ViewModels kennen keine URLSession-Details.
- Ein Repository kapselt Geräteaktionen; ein Client kapselt Request und Response.
- Cancellation muss von der Geräteaktion bis zu URLSession weitergegeben werden können.
- Nicht-idempotente Gerätebefehle werden nicht automatisch wiederholt.

## Abhängigkeiten

- Issue 047 bestimmt, welche Netzwerkpfade und Host-Auflösungen technisch zuverlässig nutzbar sind.
- Issue 048 kann die Definition von Gerätefeldern und Kommando-Templates bereitstellen.
- Issue 049 stellt die iOS-App-Schichten bereit.

## Nicht Bestandteil

- Android-Codeänderungen.
- Cloud-Proxies, externe APIs oder Telemetrie.
- WLAN-Join-UI; diese ist Gegenstand von Issue 051.

## Akzeptanzkriterien

- [ ] Der Umfang für GET, POST und RPC ist fachlich definiert.
- [ ] Timeout-, Fehler- und Cancellation-Verhalten sind eindeutig beschrieben.
- [ ] Die Diagnoseregeln schließen sensible Netzwerk- und Zugangsdaten aus.
- [ ] Der Umgang mit APs ohne Internet berücksichtigt die Ergebnisse von Issue 047.
- [ ] Die Abgrenzung zu URLSession und iOS-Schichten ist klar dokumentiert.
