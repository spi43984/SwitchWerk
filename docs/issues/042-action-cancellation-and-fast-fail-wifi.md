# Issue #42: Action Cancellation And Fast-Fail WiFi

## Metadaten

- Status: Offen
- Priorität: P1
- Typ: WLAN / UX / Geräteaktion
- Bereich: Dashboard / WLAN-Verbindung / HTTP-RPC-Aufruf

## Ziel

Laufende Geräteaktionen sollen vom Benutzer jederzeit abgebrochen werden können.

Zusätzlich sollen offensichtlich nicht erreichbare WLAN-Profile schneller behandelt werden, damit Schaltvorgänge bei mehreren zugeordneten WLANs nicht unnötig lange blockieren.

Die bestehende fachliche Schaltlogik bleibt unverändert:

- WLAN-Reihenfolge bleibt verbindlich.
- Alle zugeordneten WLANs bleiben grundsätzlich Kandidaten.
- Geschaltet wird weiterhin über das erste erfolgreich erreichbare WLAN.

## Hintergrund

Ein Schaltvorgang kann lange dauern, wenn einem Gerät mehrere WLAN-Profile zugeordnet sind und keines erreichbar ist.

Beispiele:

- Gerät ist ausgeschaltet.
- Geräte-AP ist außer Reichweite.
- WLAN existiert nicht mehr.
- Passwort wurde geändert.
- Android wartet auf einen Verbindungs-Timeout.

Währenddessen kann der Benutzer die laufende Aktion nicht aktiv abbrechen.

Dadurch entsteht der Eindruck, dass die App hängt.

## Scope

### Manuelles Abbrechen laufender Aktionen

Während eine Geräteaktion läuft:

- Dashboard-Button zeigt Ladezustand.
- Spinner oder gleichwertiger Ladeindikator wird angezeigt.
- Sichtbare Abbrechen-Aktion wird eingeblendet.
- Bevorzugt als klar erkennbares X oder Material-3-Icon-Button.

Der Benutzer kann die laufende Aktion jederzeit abbrechen.

### Saubere Cancellation

Beim Abbruch:

- laufende Coroutine beenden
- aktive WLAN-Verbindungsvorgänge abbrechen
- aktive NetworkCallbacks freigeben
- laufende HTTP/RPC-Aufrufe abbrechen
- UI-Zustand zurücksetzen

Statusmeldung:

Aktion abgebrochen

### Fast-Fail für offensichtlich nicht erreichbare WLANs

Die bestehende WLAN-Reihenfolge bleibt erhalten.

Es dürfen keine WLAN-Profile dauerhaft übersprungen werden.

Ziel ist ausschließlich eine schnellere Fehlererkennung.

Mögliche Fast-Fail-Situationen:

- WLAN ist deaktiviert.
- Authentifizierungsfehler wurde erkannt.
- Android meldet Netzwerk nicht verfügbar.
- Aktueller Scan zeigt WLAN eindeutig nicht sichtbar.

Eine nicht sichtbare SSID darf nicht pauschal als endgültiger Fehler gewertet werden, wenn der Scan unsicher, veraltet oder nicht verfügbar ist.

In unsicheren Fällen soll das WLAN weiterhin versucht werden, aber mit kürzerem Fast-Fail-Verhalten statt langem Blockieren.

## Nicht im Scope

- Fixes globales Aktionszeitbudget.
- Automatische Prioritätsänderungen.
- Automatische WLAN-Umsortierung.
- Entfernen von WLAN-Profilen.
- Änderung der bestehenden WLAN-Reihenfolge.
- Änderung der fachlichen Geräteaktionslogik.

## Architekturhinweise

- Cancellation muss durch ViewModel, Repository, WLAN-Service und HTTP/RPC-Aufruf sauber weitergereicht werden.
- NetworkCallbacks müssen auch bei Abbruch zuverlässig abgemeldet werden.
- HTTP/RPC-Aufrufe dürfen nach Abbruch nicht weiterlaufen.
- Fast-Fail darf die bestehende WLAN-Fallback-Reihenfolge nicht verändern.
- Keine neuen Frameworks oder Cloud-Abhängigkeiten einführen.

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

- [ ] Benutzer kann laufende Geräteaktionen abbrechen.
- [ ] Dashboard zeigt während laufender Aktion Spinner oder gleichwertigen Ladezustand.
- [ ] Dashboard zeigt während laufender Aktion eine klare Abbrechen-Aktion.
- [ ] Abbruch beendet nur den aktuellen Schaltvorgang.
- [ ] Abbruch ändert keine WLAN-Reihenfolge.
- [ ] Abbruch löscht keine Konfiguration.
- [ ] Nach Abbruch bleiben keine hängenden NetworkCallbacks zurück.
- [ ] Nach Abbruch bleiben keine hängenden HTTP/RPC-Aufrufe zurück.
- [ ] WLAN-Reihenfolge bleibt unverändert.
- [ ] Alle zugeordneten WLANs bleiben grundsätzlich nutzbar.
- [ ] Fast-Fail reduziert unnötige Wartezeiten.
- [ ] Nicht sichtbare SSIDs werden nur vorsichtig behandelt und nicht dauerhaft ausgeschlossen.
- [ ] Bestehende fachliche Schaltlogik bleibt unverändert.
- [ ] Build und Installation wurden auf dem Ubuntu-Host erfolgreich geprüft.

## Testhinweise

- Geräteaktion starten und während WLAN-Verbindungsversuch abbrechen.
- Geräteaktion starten und während HTTP/RPC-Aufruf abbrechen.
- Mehrere zugeordnete WLANs testen, von denen keines erreichbar ist.
- Nicht sichtbare SSID testen.
- Falsch konfiguriertes Passwort testen.
- Erfolgreiche Aktion nach vorherigem Abbruch erneut starten.
- Dashboard-Zustand nach Abbruch prüfen.
- Detailanzeige nach Abbruch und Fast-Fail prüfen.
