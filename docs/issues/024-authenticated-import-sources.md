# Issue #24: Authenticated Import Sources Backlog

## Status

Zurückgestellt / Backlog

## Ziel

Prüfung einer zukünftigen Unterstützung für den Import von Konfigurationsdateien über URLs, die eine Authentifizierung erfordern.

## Hintergrund

Derzeit unterstützt SwitchWerk ausschließlich öffentlich erreichbare Import-Quellen.

Unterstützt werden:

- öffentliche HTTPS-URLs
- öffentliche Nextcloud-Freigabelinks
- Google-Drive-Links mit Freigabe „Jeder mit dem Link“

Nicht unterstützt werden Quellen, die eine Authentifizierung erfordern.

## Bewusste Einschränkung

Aktuell sendet die App keine:

- Benutzernamen
- Passwörter
- HTTP-Basic-Auth-Daten
- OAuth-Tokens
- API-Tokens
- Cookies
- Browser-Sitzungen

Dadurch bleiben Architektur und Sicherheitsmodell einfach und nachvollziehbar.

## Aktuelles Verhalten

Kann eine Datei nur nach erfolgreicher Anmeldung heruntergeladen werden, wird der Import abgelehnt.

Der Benutzer erhält eine verständliche Fehlermeldung.

Beispiel:

> Die angegebene Datei ist nicht öffentlich erreichbar. Bitte verwenden Sie einen öffentlichen Freigabelink.

## Gründe für die Zurückstellung

Die Unterstützung authentifizierter Quellen würde zusätzliche Anforderungen verursachen:

### Sicherheit

- sichere Speicherung von Zugangsdaten
- Token-Management
- Datenschutzbetrachtung
- Risikoanalyse

### Architektur

- Erweiterung der Import-Schnittstellen
- Authentifizierungs-Framework
- Fehlerbehandlung für Login- und Token-Abläufe

### Wartungsaufwand

- OAuth-Flows ändern sich regelmäßig
- API-Abhängigkeiten zu Drittanbietern
- zusätzlicher Testaufwand

## Mögliche zukünftige Verfahren

- HTTP Basic Authentication
- Bearer Token
- API-Key
- OAuth 2.0
- Google Drive API
- Nextcloud WebDAV

## Akzeptanzkriterien

Für die aktuelle Version:

- Authentifizierte URLs werden nicht unterstützt.
- Öffentliche HTTPS-Links funktionieren weiterhin.
- Öffentliche Nextcloud-Links funktionieren weiterhin.
- Öffentliche Google-Drive-Links funktionieren weiterhin.
- Benutzer erhält eine verständliche Fehlermeldung.

## Priorität

Niedrig

## Abhängigkeiten

Keine

## Entscheidung

Dieses Thema wird zunächst zurückgestellt und nur bei konkretem Bedarf erneut bewertet.
