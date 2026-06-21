# 042 Action Cancellation And Fast-Fail WiFi

## Status

offen

## Priorität

P1

## Ziel

Laufende Geräteaktionen sollen vom Benutzer jederzeit abgebrochen werden können.

Zusätzlich sollen offensichtlich nicht erreichbare WLAN-Profile schneller behandelt werden, damit Schaltvorgänge bei mehreren zugeordneten WLANs nicht unnötig lange blockieren.

Die bestehende fachliche Schaltlogik bleibt unverändert:

- WLAN-Reihenfolge bleibt verbindlich
- alle zugeordneten WLANs bleiben grundsätzlich Kandidaten
- geschaltet wird weiterhin über das erste erfolgreich erreichbare WLAN

## Problem

Ein Schaltvorgang kann lange dauern, wenn einem Gerät mehrere WLAN-Profile zugeordnet sind und keines erreichbar ist.

Beispiele:

- Gerät ist ausgeschaltet
- Geräte-AP ist außer Reichweite
- WLAN existiert nicht mehr
- Passwort wurde geändert
- Android wartet auf einen Verbindungs-Timeout

Währenddessen kann der Benutzer die laufende Aktion nicht aktiv abbrechen.

Dadurch entsteht der Eindruck, dass die App hängt.

## Scope

### 1. Manuelles Abbrechen laufender Aktionen

Während eine Geräteaktion läuft:

- Dashboard-Button zeigt Ladezustand
- Spinner wird angezeigt
- sichtbare Abbrechen-Aktion wird eingeblendet
- bevorzugt als klar erkennbares X oder Material-3-Icon-Button

Der Benutzer kann die laufende Aktion jederzeit abbrechen.

### 2. Saubere Cancellation

Beim Abbruch:

- laufende Coroutine beenden
- aktive WLAN-Verbindungsvorgänge abbrechen
- aktive NetworkCallbacks freigeben
- laufende HTTP/RPC-Aufrufe abbrechen
- UI-Zustand zurücksetzen

Statusmeldung: Aktion abgebrochen

### 3. Fast-Fail für offensichtlich nicht erreichbare WLANs

Die bestehende WLAN-Reihenfolge bleibt erhalten.

Es dürfen keine WLAN-Profile dauerhaft übersprungen werden.

Ziel ist ausschließlich eine schnellere Fehlererkennung.

Mögliche Fast-Fail-Situationen:

- WLAN ist deaktiviert
- Authentifizierungsfehler wurde erkannt
- Android meldet Netzwerk nicht verfügbar
- aktueller Scan zeigt WLAN eindeutig nicht sichtbar

Wichtig:

Eine nicht sichtbare SSID darf nicht pauschal als endgültiger Fehler gewertet werden, wenn der Scan unsicher, veraltet oder nicht verfügbar ist.

In unsicheren Fällen soll das WLAN weiterhin versucht werden, aber mit kürzerem Fast-Fail-Verhalten statt langem Blockieren.

### 4. Keine globale harte Aktionszeitbegrenzung

Nicht Bestandteil dieses Issues ist ein fixes globales Aktionszeitbudget.

Grund:

Bei vielen bewusst zugeordneten WLAN-Profilen, z. B. 5 WLANs, könnte ein starres globales Limit verhindern, dass spätere WLANs überhaupt versucht werden.

Die Optimierung soll daher pro WLAN-Profil erfolgen und nicht die gesamte Geräteaktion hart begrenzen.

## UI

Während laufender Aktion soll der Dashboard-Eintrag sichtbar im Arbeitszustand sein.

Beispielhafte Darstellung:

[ Spinner ] Schalten...

                         [ X ]

oder äquivalente Material-3-Darstellung.

Die Abbrechen-Aktion muss klar erkennbar sein.

Kein Doppelklick als primärer Abbruchmechanismus, da dies schwer auffindbar und fehleranfällig ist.

## Diagnose

Bei aktivierter Detailanzeige sollen entsprechende Meldungen erscheinen.

Beispiele:

Verbinde mit WLAN "Shelly Garage"

WLAN nicht sichtbar
Nächstes WLAN wird versucht

Authentifizierung fehlgeschlagen
Nächstes WLAN wird versucht

Aktion durch Benutzer abgebrochen

## Akzeptanzkriterien

- Benutzer kann laufende Geräteaktionen abbrechen
- Dashboard zeigt während laufender Aktion Spinner oder gleichwertigen Ladezustand
- Dashboard zeigt während laufender Aktion eine klare Abbrechen-Aktion
- Abbruch beendet nur den aktuellen Schaltvorgang
- Abbruch ändert keine WLAN-Reihenfolge
- Abbruch löscht keine Konfiguration
- keine hängenden NetworkCallbacks nach Abbruch
- keine hängenden HTTP/RPC-Aufrufe nach Abbruch
- WLAN-Reihenfolge bleibt unverändert
- alle zugeordneten WLANs bleiben grundsätzlich nutzbar
- Fast-Fail reduziert unnötige Wartezeiten
- nicht sichtbare SSIDs werden nur vorsichtig behandelt und nicht dauerhaft ausgeschlossen
- bestehende fachliche Schaltlogik bleibt unverändert
