# Issue 033: Android-managed WiFi networks

## Metadata

- Status: Completed
- Priority: P1
- Type: Feature
- Area: WiFi

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

- SwitchWerk verwaltet
  - SSID + Passwort
  - aktuelles Verhalten

- Android verwaltet
  - nur SSID
  - Passwortfeld deaktiviert oder ausgeblendet
  - SwitchWerk speichert keine Zugangsdaten
  - SwitchWerk nutzt das WLAN, wenn Android bereits damit verbunden ist
  - falls Android nicht verbunden ist, wird der Benutzer zum manuellen WLAN-Wechsel geführt

## Komfortfunktion SSID-Auswahl

Zusätzlich soll die SSID nicht nur manuell eingegeben werden können.

Neue Funktion:

SSID auswählen

Beim Betätigen wird ein WLAN-Scan durchgeführt und die aktuell sichtbaren SSIDs angezeigt.

Beispiel:

TEAMWERK
TEAMWERK-IOT
ShellyPlus1-ABCDEF
FritzBox-1234

Der Benutzer kann eine SSID auswählen und in das Profil übernehmen.

Wichtig:

- Es werden ausschließlich aktuell sichtbare WLANs angezeigt.
- Es werden keine in Android gespeicherten WLAN-Profile ausgelesen.
- Es werden keine Passwörter ausgelesen.
- Die Funktion dient ausschließlich als Komfortfunktion zur Übernahme der SSID.
- Die manuelle Eingabe der SSID bleibt weiterhin möglich.

## Schaltlogik

Beim Schalten eines Geräts:

1. Prüfen, ob das Gerät aktuell bereits mit der Ziel-SSID verbunden ist.
2. Falls ja:
   - HTTP/RPC-Befehl direkt ausführen.
3. Falls nein und WLAN-Modus SwitchWerk verwaltet:
   - Verbindung wie bisher mit gespeicherter SSID + Passwort anfordern.
4. Falls nein und WLAN-Modus Android verwaltet:
   - keine Zugangsdaten oder EAP-Konfiguration erzeugen.
   - keine automatische Verbindung garantieren.
   - verständliche Meldung anzeigen, dass das WLAN in Android ausgewählt werden muss.
   - optional Android-WLAN-Einstellungen öffnen.
5. Wenn nach einem manuellen Wechsel die Ziel-SSID aktiv ist:
   - HTTP/RPC-Befehl ausführen.
6. Wenn die Ziel-SSID nicht aktiv ist:
   - Fehlermeldung anzeigen.

## Wichtige Einschränkung

Android garantiert nicht, dass eine normale App ein bestimmtes gespeichertes WLAN hart erzwingen kann.

Die Verbindung zu einem Android-verwalteten WLAN ist daher best-effort:

- SwitchWerk prüft, ob das gewünschte Netzwerk bereits aktiv ist.
- Android entscheidet final über Verbindung und Authentifizierung.
- Android verwendet vorhandene gespeicherte Zugangsdaten/Zertifikate.
- SwitchWerk speichert und erzeugt keine Enterprise- oder Zertifikatskonfiguration.

Eine reine SSID in SwitchWerk bedeutet nicht, dass Android per öffentlicher App-API zuverlässig mit vorhandenen System-Credentials zu diesem WLAN umgeschaltet werden kann.

Besonders bei 802.1X/EAP, Zertifikaten und Enterprise-WLANs bleibt der manuelle Android-WLAN-Wechsel der robuste Pfad.

## UI-Anforderung

In der WLAN-Konfiguration soll ein Auswahlfeld ergänzt werden:

- SwitchWerk verwaltet dieses WLAN
- Android verwaltet dieses WLAN

Bei Android verwaltet dieses WLAN:

- Passwortfeld deaktivieren oder ausblenden.
- Hinweis anzeigen:
  - Dieses WLAN muss in Android eingerichtet und verbunden sein. SwitchWerk speichert keine Zugangsdaten.
- Aktion anbieten:
  - Android-WLAN-Einstellungen öffnen.
- Option anbieten:
  - SSID aus sichtbaren WLANs auswählen.

## Datenmodell

Erweiterung von WifiProfile:

enum class WifiConnectionMode {
    SWITCHWERK_MANAGED,
    ANDROID_MANAGED
}

Neues Feld:

connectionMode

## Datenbank

Room-Migration:

Version +1

Neue Spalte:

connectionMode

Default:

SWITCHWERK_MANAGED

Dadurch bleiben bestehende Installationen unverändert funktionsfähig.

## Akzeptanzkriterien

- WLAN-Einträge können als Android-verwaltet markiert werden.
- Für Android-verwaltete WLANs wird kein Passwort benötigt.
- Passwortfeld ist im UI deaktiviert oder ausgeblendet.
- WLAN-Scan zur Auswahl sichtbarer SSIDs funktioniert.
- SSID kann aus Scan-Ergebnissen übernommen werden.
- Die manuelle Eingabe der SSID bleibt möglich.
- Beim Schalten wird zuerst geprüft, ob die Ziel-SSID bereits aktiv ist.
- Falls die Ziel-SSID aktiv ist, wird der HTTP/RPC-Befehl ausgeführt.
- Falls die Ziel-SSID nicht aktiv ist, wird keine automatische Verbindung garantiert.
- Bei nicht aktiver Ziel-SSID erscheint eine verständliche Meldung mit Hinweis auf den Android-WLAN-Wechsel.
- Die App kann die Android-WLAN-Einstellungen öffnen.
- Android-verwaltete WLANs funktionieren ohne gespeichertes Passwort in SwitchWerk, sobald Android bereits mit der Ziel-SSID verbunden ist.
- Room-Migration funktioniert für bestehende Installationen.
- Bestehende SwitchWerk-verwaltete WLANs funktionieren unverändert weiter.
- Bestehende Tests bleiben grün oder werden passend erweitert.
