# Issue #11: Device Action With WiFi Fallback

## Ziel

Eine Geräteaktion verbindet die vorhandenen Bausteine zu einem vollständigen
Schaltvorgang.

Der API-Aufruf erfolgt direkt über das aktuelle WLAN, wenn dieses WLAN einem
dem Gerät zugeordneten WLAN-Profil entspricht. Andernfalls werden die
zugeordneten WLAN-Profile in ihrer gespeicherten Reihenfolge durchprobiert.

## Scope

- aktuelles WLAN mit den dem Gerät zugeordneten WLAN-Profilen abgleichen
- aktuelles WLAN einschließlich SSID und zugehörigem Android-`Network` über
  eine für die Android-Version geeignete API bestimmen
- fehlende Berechtigung oder nicht verfügbare SSID als unbekannt behandeln
- direkten, an das aktuelle WLAN-`Network` gebundenen API-Aufruf ausführen,
  wenn das aktuelle WLAN zugeordnet ist
- zugeordnete WLAN-Profile in ihrer gespeicherten Reihenfolge durchlaufen
- WLAN-Verbindung über `WifiConnectionService` herstellen
- API-Aufruf über das von `WifiConnectionResult.Success` gelieferte `Network`
  ausführen
- GET und POST anhand der gespeicherten Geräteaktion unterstützen
- strukturierten Lade-, Erfolgs- und Fehlerzustand im ViewModel bereitstellen
- Ergebnis der Geräteaktion im Dashboard verständlich anzeigen
- WLAN-Verbindung nach Abschluss der Geräteaktion sauber freigeben
- Coroutine-Abbruch unterstützen
- keine SSIDs, Passwörter, vollständigen URLs oder Payloads loggen

## Ablauf

1. Dem Gerät zugeordnete WLAN-Profile in ihrer gespeicherten Reihenfolge laden.
2. Aktuell verbundenes WLAN bestimmen.
3. Entspricht das aktuelle WLAN einem zugeordneten Profil, den API-Aufruf direkt
   über das konkrete WLAN-`Network` ausführen.
4. Ist das aktuelle WLAN nicht zugeordnet oder nicht eindeutig bestimmbar, die
   WLAN-Profile der Reihe nach verbinden.
5. Nach erfolgreicher WLAN-Verbindung den API-Aufruf gezielt über das gelieferte
   `Network` ausführen.
6. Bei erfolgreichem API-Aufruf die Geräteaktion als erfolgreich abschließen.
7. Bei einem Fehler anhand der Regeln zur Fehlerbehandlung entscheiden, ob das
   nächste WLAN-Profil versucht oder die Aktion beendet wird.
8. Nach Erfolg oder endgültigem Fehler die angeforderte WLAN-Verbindung
   freigeben und das Ergebnis anzeigen.

## Fehlerbehandlung

### Direkter API-Aufruf im bereits verbundenen, zugeordneten WLAN

- Erfolg beendet die Geräteaktion.
- Ein HTTP-Fehler beendet die Geräteaktion und wird angezeigt.
- Ein ungültiger Request beendet die Geräteaktion und wird angezeigt.
- Ein eindeutig vor Ausführung der Geräteaktion erkannter DNS- oder
  Verbindungsfehler startet den WLAN-Fallback mit den weiteren zugeordneten
  Profilen.
- Ein API-Timeout beendet die Geräteaktion, weil nicht sicher feststeht, ob der
  Schaltbefehl bereits ausgeführt wurde.

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

## Reihenfolge und aktuelles WLAN

- Die gespeicherte Reihenfolge der Geräteverbindungen ist verbindlich.
- Das aktuell verbundene WLAN wird nur direkt verwendet, wenn es einem
  zugeordneten WLAN-Profil entspricht.
- Für diesen Abgleich gilt eine pragmatische SSID-Übereinstimmung: Stimmt die
  aktuelle SSID mit der SSID mindestens eines zugeordneten WLAN-Profils
  überein, gilt das aktuelle WLAN als zugeordnet.
- Profilname, Passwort und Profil-ID werden nicht zur Erkennung des aktuell
  verbundenen WLANs verwendet.
- Sind mehrere zugeordnete Profile mit derselben SSID vorhanden, gilt das
  aktuelle WLAN ebenfalls als zugeordnet. Für den direkten API-Aufruf ist keine
  Unterscheidung zwischen diesen Profilen erforderlich.
- Das aktuell verbundene und bereits direkt getestete Profil wird im
  anschließenden Fallback nicht erneut versucht. Bei mehreren Profilen mit
  derselben SSID werden alle Profile mit dieser SSID als bereits abgedeckt
  behandelt.
- Ist kein aktuelles WLAN bestimmbar oder zugeordnet, beginnt der Fallback mit
  dem ersten gespeicherten WLAN-Profil.
- Ein fehlgeschlagener WLAN-Verbindungsaufbau führt zum nächsten Profil.
- Erst wenn kein weiterer zulässiger Versuch vorhanden ist, wird die gesamte
  Geräteaktion als fehlgeschlagen gemeldet.

## Nicht im Scope

- Änderung der Geräte- oder WLAN-Profilverwaltung
- Änderung der gespeicherten Reihenfolge von Geräteverbindungen
- adaptive WPA2/WPA3-Erkennung aus Issue 018
- automatische Wiederholung desselben API-Aufrufs im selben Netzwerk
- stille Wiederholung kritischer Schaltaktionen
- Einführung von Standortberechtigungen ohne gesonderte Prüfung und
  ausdrückliche Freigabe
- Import oder Export

## Android-Berechtigungen

Die aktuelle SSID kann von Android bei unzureichenden Berechtigungen als
unbekannt zurückgegeben werden. Die Implementierung muss vorab prüfen, welche
minimalen Berechtigungen für die unterstützten Android-Versionen erforderlich
sind.

Ohne gesonderte ausdrückliche Freigabe wird keine Standortberechtigung
eingeführt. Ist die SSID deshalb nicht verfügbar, wird kein direkter API-Aufruf
ausgeführt; die Aktion beginnt stattdessen mit dem ersten zugeordneten
WLAN-Profil.

## Akzeptanzkriterien

- [ ] Das aktuelle WLAN wird bestimmt
- [ ] Eine nicht verfügbare aktuelle SSID wird sicher als unbekannt behandelt
- [ ] Ein direkter API-Aufruf erfolgt nur bei einem dem Gerät zugeordneten WLAN
- [ ] Eine Übereinstimmung der SSID mit mindestens einem zugeordneten Profil
      gilt als Zuordnung
- [ ] Mehrere zugeordnete Profile mit derselben SSID führen nicht zu
      mehrfachen direkten API-Aufrufen
- [ ] Der direkte API-Aufruf ist an das konkrete aktuelle WLAN-`Network`
      gebunden
- [ ] Bei einem nicht zugeordneten aktuellen WLAN wird kein direkter API-Aufruf
      ausgeführt
- [ ] Zugeordnete WLAN-Profile werden in ihrer gespeicherten Reihenfolge
      versucht
- [ ] GET-Geräteaktionen werden unterstützt
- [ ] POST-Geräteaktionen werden unterstützt
- [ ] Der API-Aufruf nach WLAN-Verbindung verwendet das gelieferte `Network`
- [ ] Ein fehlgeschlagener WLAN-Verbindungsaufbau führt zum nächsten Profil
- [ ] Ein HTTP-Fehler führt nicht zum nächsten WLAN-Profil
- [ ] Ein ungültiger Request führt nicht zum nächsten WLAN-Profil
- [ ] Ein eindeutiger DNS- oder Verbindungsfehler kann zum nächsten WLAN-Profil
      führen
- [ ] Ein API-Timeout löst keinen erneuten Schaltversuch über ein anderes WLAN
      aus
- [ ] Ein bereits direkt getestetes WLAN-Profil wird nicht erneut versucht
- [ ] Erfolg und Fehler werden im Dashboard verständlich angezeigt
- [ ] Mehrfache gleichzeitige Aktionen desselben Geräts werden verhindert
- [ ] Die angeforderte WLAN-Verbindung wird nach Abschluss freigegeben
- [ ] Coroutine-Abbruch beendet die laufende Aktion sauber
- [ ] Es werden keine sensiblen Netzwerk- oder API-Daten geloggt

## Testhinweise

- aktuelles WLAN ist dem Gerät zugeordnet und API-Aufruf ist erfolgreich
- mehrere Profile mit derselben aktuellen SSID sind zugeordnet und es erfolgt
  genau ein direkter API-Aufruf
- aktuelles WLAN ist zugeordnet, DNS-Auflösung oder Verbindungsaufbau scheitert
  eindeutig und das nächste Profil funktioniert
- aktuelles WLAN ist nicht zugeordnet und das erste Profil funktioniert
- aktuelles WLAN ist nicht bestimmbar und der Fallback beginnt beim ersten
  Profil
- erstes WLAN ist nicht verfügbar und das zweite Profil funktioniert
- WLAN-Verbindung ist erfolgreich, API liefert `401`, `404` oder `500` und es
  wird kein weiteres WLAN versucht
- WLAN-Verbindung ist erfolgreich, DNS-Auflösung oder Verbindungsaufbau
  scheitert eindeutig und das nächste Profil wird versucht
- API-Aufruf läuft in einen Timeout und es wird kein weiteres WLAN versucht
- alle WLAN-Verbindungen oder API-Erreichbarkeitsversuche schlagen fehl
- Aktion wird während WLAN-Verbindung oder API-Aufruf abgebrochen
- wiederholter Button-Klick startet keine parallele zweite Geräteaktion
