# Issue #11: Device Action With WiFi Fallback

## Ziel

Eine Geräteaktion verbindet die vorhandenen Bausteine zu einem vollständigen
Schaltvorgang.

Geräteaktionen verwenden ausschließlich die dem Gerät explizit zugeordneten
WLAN-Profile. Diese Profile werden in ihrer gespeicherten Reihenfolge
angefordert und der API-Aufruf wird nur über das dabei gelieferte
Android-`Network` ausgeführt.

## Scope

- zugeordnete WLAN-Profile in ihrer gespeicherten Reihenfolge durchlaufen
- für jedes Profil eine WLAN-Verbindung über `WifiConnectionService`
  anfordern
- API-Aufruf ausschließlich über das von `WifiConnectionResult.Success`
  gelieferte `Network` ausführen
- gebundene lokale HTTP-Aufrufe über `Network.openConnection()` ausführen
- GET und POST anhand der gespeicherten Geräteaktion unterstützen
- strukturierten Lade-, Erfolgs- und Fehlerzustand im ViewModel bereitstellen
- Ergebnis der Geräteaktion im Dashboard verständlich anzeigen
- WLAN-Verbindung nach jedem Versuch sauber freigeben
- Coroutine-Abbruch unterstützen
- keine SSIDs, Passwörter, vollständigen URLs oder Payloads loggen

Das aktuell verbundene WLAN wird weder bestimmt noch für die Ausführung einer
Geräteaktion berücksichtigt. Insbesondere findet kein SSID-Abgleich statt und
es gibt keinen direkten API-Aufruf über das bereits aktive WLAN.

## Ablauf

1. Dem Gerät zugeordnete WLAN-Profile in ihrer gespeicherten Reihenfolge laden.
2. Erstes WLAN-Profil über `WifiConnectionService` verbinden.
3. Nach erfolgreicher WLAN-Verbindung den API-Aufruf über das gelieferte
   `Network` ausführen.
4. Bei Erfolg die Geräteaktion abschließen.
5. Bei einem zulässigen Erreichbarkeitsfehler das nächste WLAN-Profil
   versuchen.
6. Nach Erfolg oder endgültigem Fehler die angeforderte WLAN-Verbindung
   freigeben und das Ergebnis anzeigen.

## Fehlerbehandlung

### WLAN-Verbindungsaufbau

- Ein fehlgeschlagener WLAN-Verbindungsaufbau führt zum nächsten Profil.
- Eine fehlende Berechtigung beendet die Geräteaktion und wird angezeigt.
- Eine nicht unterstützte Android-Version beendet die Geräteaktion und wird
  angezeigt.

### API-Aufruf nach erfolgreichem WLAN-Verbindungsaufbau

- Erfolg beendet die Geräteaktion.
- Ein HTTP-Fehler beendet die Geräteaktion und wird angezeigt.
- Ein ungültiger Request beendet die Geräteaktion und wird angezeigt.
- Nur ein eindeutig vor Ausführung der Geräteaktion erkannter DNS- oder
  Verbindungsfehler erlaubt den Versuch mit dem nächsten zugeordneten
  WLAN-Profil.
- Ein API-Timeout beendet die Geräteaktion, um eine mögliche doppelte
  Ausführung zu verhindern.

Als Fehler, bei denen das nächste WLAN versucht werden darf, gelten:

- DNS-Fehler
- Verbindungsaufbau abgelehnt
- kein Routing zum Ziel
- vergleichbare Fehler, bei denen der Request nachweislich nicht an das Gerät
  gesendet wurde

HTTP-Statuscodes wie `401`, `403`, `404` oder `500` sind keine
Erreichbarkeitsfehler und lösen keinen Wechsel zum nächsten WLAN-Profil aus.
Timeouts beim API-Aufruf sind ebenfalls kein sicherer Nachweis und lösen keinen
weiteren Versuch aus.

## Reihenfolge

- Die gespeicherte Reihenfolge der Geräteverbindungen ist verbindlich.
- Jedes zugeordnete Profil wird unabhängig von seiner SSID als eigener
  Verbindungsversuch behandelt.
- Auch mehrere Profile mit derselben SSID werden in ihrer gespeicherten
  Reihenfolge verarbeitet.
- Erst wenn kein weiterer zulässiger Versuch vorhanden ist, wird die gesamte
  Geräteaktion als fehlgeschlagen gemeldet.

## Nicht im Scope

- Bestimmung oder Auswertung des aktuell verbundenen WLANs
- SSID-Abgleich mit dem aktuell verbundenen WLAN
- direkter API-Aufruf über das aktuell aktive WLAN
- Änderung der Geräte- oder WLAN-Profilverwaltung
- Änderung der gespeicherten Reihenfolge von Geräteverbindungen
- adaptive WPA2/WPA3-Erkennung aus Issue 018
- automatische Wiederholung desselben API-Aufrufs im selben Netzwerk
- stille Wiederholung kritischer Schaltaktionen
- Einführung von Standortberechtigungen
- Import oder Export

## Akzeptanzkriterien

- [x] Geräteaktionen werden ausschließlich über explizit angeforderte
      WLAN-Verbindungen der zugeordneten WLAN-Profile ausgeführt
- [x] Zugeordnete WLAN-Profile werden in ihrer gespeicherten Reihenfolge
      versucht
- [x] Profile mit identischer SSID werden als getrennte Verbindungsversuche
      verarbeitet
- [x] GET-Geräteaktionen werden unterstützt
- [x] POST-Geräteaktionen werden unterstützt
- [x] Der API-Aufruf verwendet ausschließlich das von
      `WifiConnectionResult.Success` gelieferte `Network`
- [x] Gebundene lokale HTTP-Aufrufe verwenden `Network.openConnection()`
- [x] Ein fehlgeschlagener WLAN-Verbindungsaufbau führt zum nächsten Profil
- [x] Ein HTTP-Fehler führt nicht zum nächsten WLAN-Profil
- [x] Ein ungültiger Request führt nicht zum nächsten WLAN-Profil
- [x] Ein eindeutiger DNS- oder Verbindungsfehler kann zum nächsten WLAN-Profil
      führen
- [x] Ein API-Timeout löst keinen erneuten Schaltversuch über ein anderes WLAN
      aus
- [x] Erfolg und Fehler werden im Dashboard verständlich angezeigt
- [x] Mehrfache gleichzeitige Aktionen desselben Geräts werden verhindert
- [x] Die angeforderte WLAN-Verbindung wird nach jedem Versuch freigegeben
- [x] Coroutine-Abbruch beendet die laufende Aktion sauber
- [x] Es werden keine sensiblen Netzwerk- oder API-Daten geloggt

## Testhinweise

- erstes zugeordnetes WLAN funktioniert
- erstes WLAN ist nicht verfügbar und das zweite Profil funktioniert
- zwei Profile mit derselben SSID werden nacheinander angefordert
- der API-Aufruf verwendet das vom erfolgreichen Verbindungsaufbau gelieferte
  `Network`
- gebundener HTTP-Aufruf über `Network.openConnection()`
- WLAN-Verbindung ist erfolgreich, API liefert `401`, `404` oder `500` und es
  wird kein weiteres WLAN versucht
- WLAN-Verbindung ist erfolgreich, DNS-Auflösung oder Verbindungsaufbau
  scheitert eindeutig und das nächste Profil wird versucht
- API-Aufruf läuft in einen Timeout und es wird kein weiteres WLAN versucht
- alle WLAN-Verbindungen oder API-Erreichbarkeitsversuche schlagen fehl
- Aktion wird während WLAN-Verbindung oder API-Aufruf abgebrochen
- wiederholter Button-Klick startet keine parallele zweite Geräteaktion
