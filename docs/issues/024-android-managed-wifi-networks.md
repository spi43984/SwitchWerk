# Issue 024: Android-managed WiFi networks

Priority: P1

## Ziel

SwitchWerk soll WLANs unterstützen, deren Zugangsdaten bereits im Android-System gespeichert sind.

Das betrifft insbesondere:

- Unternehmens-WLANs
- 802.1X / EAP-TLS
- PEAP / TTLS
- zertifikatsbasierte WLANs
- bereits bekannte private WLANs

SwitchWerk soll dafür keine Passwörter, Zertifikate oder EAP-Konfigurationen speichern müssen.

## Problem

Bisher geht die WLAN-Logik davon aus, dass SwitchWerk SSID und Passwort selbst verwaltet.

Das funktioniert nicht sauber für Android-WLANs mit komplexer Authentifizierung, insbesondere 802.1X/EAP mit Zertifikaten.

## Vorgeschlagene Lösung

Ein WLAN-Eintrag erhält einen Verbindungsmodus:

- `SwitchWerk verwaltet`
  - SSID + Passwort
  - aktuelles Verhalten

- `Android verwaltet`
  - nur SSID
  - Passwortfeld deaktiviert oder ausgeblendet
  - Android nutzt gespeicherte Credentials

## Schaltlogik

Beim Schalten eines Geräts:

1. Prüfen, ob das Gerät aktuell bereits mit der Ziel-SSID verbunden ist.
2. Falls ja:
   - HTTP/RPC-Befehl direkt ausführen.
3. Falls nein und WLAN-Modus `SwitchWerk verwaltet`:
   - Verbindung wie bisher mit gespeicherter SSID + Passwort anfordern.
4. Falls nein und WLAN-Modus `Android verwaltet`:
   - Netzwerkverbindung per Android-Netzwerkanforderung für die Ziel-SSID anstoßen.
   - Kein Passwort übergeben.
   - Android soll vorhandene System-Credentials verwenden.
5. Warten, bis die Ziel-SSID aktiv ist.
6. Wenn erfolgreich:
   - HTTP/RPC-Befehl ausführen.
7. Wenn nicht erfolgreich:
   - Fehlermeldung anzeigen.

## Wichtige Einschränkung

Android garantiert nicht, dass eine App ein bestimmtes gespeichertes WLAN hart erzwingen kann.

Die Verbindung zu einem Android-verwalteten WLAN ist daher best-effort:

- SwitchWerk fordert das gewünschte Netzwerk an.
- Android entscheidet final über Verbindung und Authentifizierung.
- Android verwendet vorhandene gespeicherte Zugangsdaten/Zertifikate.

## UI-Anforderung

In der WLAN-Konfiguration soll ein Auswahlfeld ergänzt werden:

- `SwitchWerk verwaltet dieses WLAN`
- `Android verwaltet dieses WLAN`

Bei `Android verwaltet dieses WLAN`:

- Passwortfeld deaktivieren oder ausblenden.
- Hinweis anzeigen:
  - "Dieses WLAN muss bereits in Android eingerichtet sein. SwitchWerk speichert keine Zugangsdaten."

## Akzeptanzkriterien

- WLAN-Einträge können als Android-verwaltet markiert werden.
- Für Android-verwaltete WLANs wird kein Passwort benötigt.
- Passwortfeld ist im UI deaktiviert oder ausgeblendet.
- Beim Schalten wird zuerst geprüft, ob die Ziel-SSID bereits aktiv ist.
- Falls nicht verbunden, wird eine Android-Netzwerkanforderung für die Ziel-SSID versucht.
- Android-verwaltete WLANs funktionieren ohne gespeichertes Passwort in SwitchWerk.
- Bei Fehlschlag erscheint eine verständliche Fehlermeldung.
- Bestehende SwitchWerk-verwaltete WLANs funktionieren unverändert weiter.
- Bestehende Tests bleiben grün oder werden passend erweitert.
