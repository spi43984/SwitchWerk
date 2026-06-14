# AI Handoff

Stand: 14. Juni 2026

## Aktuelle Arbeit

Issue 011 "Device Action With WiFi Fallback" ist auf dem Feature-Branch
`device-action-with-wifi-fallback` implementiert und im Container geprüft.

Die HTTP-Methodik war bereits gleichwertig: `Network.openConnection()`, GET und
automatische Ergänzung von `http://`. Im Host-Feld genügt daher
`192.168.33.1`; der Pfad bleibt
`/rpc/Switch.Set?id=0&on=true&toggle_after=1`.

Die Android-WLAN-Historie zeigte einen erfolgreichen Aufbau der lokalen
Zweitverbindung zu `TEAMWERK-Tor`. Die aktuell installierte SwitchWerk-
Datenbank enthielt bei der Analyse jedoch weder dieses WLAN noch
`192.168.33.1`, sondern ausschließlich Testprofile und Testadressen. Vor dem
nächsten Gerätetest muss die reale Konfiguration in der installierten App
erneut geprüft werden.

Ein späterer Live-Test mit korrekter Konfiguration hat die eigentliche Ursache
gezeigt: NetGuard betreibt auf dem Testgerät ein nicht umgehbares VPN.
SwitchWerk-UID 10300 liegt im VPN-Bereich, ShellyPulse-UID 10602 ist vom VPN
ausgenommen. Das Binden des HTTP-Sockets an das angeforderte WLAN scheitert
deshalb mit `EPERM (Operation not permitted)`. SwitchWerk zeigt dafür nun
gezielt an, dass VPN oder Firewall den lokalen Netzwerkzugriff blockiert.

Die während der Fehlersuche vorgenommene Angleichung des `NetworkRequest` an
ShellyPulse wurde anschließend zurückgenommen, da sie nicht ursächlich war.
SwitchWerk fordert das Geräte-WLAN weiterhin ausdrücklich als lokales Netzwerk
ohne `NET_CAPABILITY_INTERNET` an und behält den Coroutine-Timeout bei.

Diagnoselogs verwenden den Tag `SwitchWerkNetwork` und enthalten keine SSIDs,
Passwörter, IP-Adressen, URLs oder Payloads. Abruf:

```bash
adb logcat -c
adb logcat -v time -s SwitchWerkNetwork:*
```

- GitHub-Issue: #21
- Issue-URL: https://github.com/spi43984/SwitchWerk/issues/21
- lokales Issue: `docs/issues/011-device-action-with-wifi-fallback.md`
- Host-Gerätetest erfolgreich, nachdem SwitchWerk in NetGuard vom VPN
  ausgenommen wurde
- Benutzer hat Commit, Push, Pull Request und Merge ausdrücklich freigegeben
- Akzeptanzkriterien in der lokalen Issue-Datei sind abgehakt

## Implementierter Umfang

- das aktuell verbundene WLAN wird nicht bestimmt oder berücksichtigt
- kein SSID-Abgleich und kein direkter API-Aufruf über das aktive WLAN
- Geräteaktionen verwenden ausschließlich explizit über
  `WifiConnectionService` angeforderte WLAN-Verbindungen
- Verarbeitung aller zugeordneten Profile in der gespeicherten Reihenfolge
- Profile mit identischer SSID bleiben getrennte Verbindungsversuche
- HTTP-Aufrufe verwenden ausschließlich das von
  `WifiConnectionResult.Success` gelieferte `Network`
- gebundene lokale GET-/POST-Aufrufe verwenden wie die funktionierende
  ShellyPulse-Referenz `Network.openConnection()` statt OkHttp mit
  `Network.socketFactory` und eigener DNS-Auflösung
- `AndroidWifiConnectionService` liefert das angeforderte `Network` bei
  `onAvailable()`
- `NEARBY_WIFI_DEVICES` wird ab Android 13 zur Laufzeit angefordert
- `CHANGE_WIFI_STATE`, `ACCESS_WIFI_STATE` und `ACCESS_NETWORK_STATE` sind im
  Manifest ergänzt
- keine Standortberechtigung eingeführt
- Passwortzugriff über den bestehenden verschlüsselten Credential Store
- GET- und POST-Aufrufe über den bestehenden `HttpApiCallService`
- Host-/Pfad-Zusammenführung mit OkHttp `HttpUrl`
- nächstes WLAN nur nach fehlgeschlagenem WLAN-Aufbau oder eindeutigem DNS-,
  Verbindungs- bzw. Routingfehler
- kein weiterer Schaltversuch nach HTTP-Fehler, ungültigem Request, generischem
  Netzwerkfehler oder API-Timeout
- Freigabe jeder angeforderten WLAN-Verbindung in `finally`
- Coroutine-Abbruch wird weitergereicht und gibt das WLAN frei
- globale Serialisierung der Aktionsorchestrierung zum Schutz des
  zustandsbehafteten WLAN-Verbindungsdienstes
- pro Gerät strukturierter Lade-, Erfolgs- und Fehlerzustand im
  `MainViewModel`
- wiederholte Klicks desselben Geräts starten keine parallele zweite Aktion
- verständliche Statusanzeige direkt in der Dashboard-Gerätekarte
- Koin-Registrierung aller neuen Services
- keine Logs für SSIDs, Passwörter, URLs oder Payloads

## Tests und Verifikation

Im Container erfolgreich:

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
git diff --check
```

Die neuen Tests decken ab:

- ausschließlich explizit angeforderte WLAN-Verbindungen
- Verwendung des vom Verbindungsdienst gelieferten `Network`
- gebundener HTTP-Aufruf über `Network.openConnection()`
- exakte Shelly-GET-URL
  `http://192.168.33.1/rpc/Switch.Set?id=0&on=true&toggle_after=1`
- geordnete Verarbeitung der zugeordneten WLAN-Profile
- getrennte Verarbeitung mehrerer Profile mit derselben SSID
- GET und POST einschließlich Payload-Weitergabe
- nächstes WLAN nach eindeutigem DNS-Fehler
- kein Retry nach HTTP-Fehler oder Timeout
- WLAN-Freigabe bei Coroutine-Abbruch
- Schutz vor wiederholtem Button-Klick desselben Geräts

Lint meldet 0 Fehler. Verbleibende Warnungen betreffen überwiegend bereits
bekannte veraltete Abhängigkeiten, ungenutzte Template-Ressourcen und
Projektversionen. Der bekannte Room-Schemaexport-Hinweis sowie die
Deprecation-Warnungen für `EncryptedSharedPreferences` und `MasterKey` bestehen
weiterhin.

Container-Build, Installation und manueller Schaltversuch auf dem Host wurden
erfolgreich bestätigt.

## Bestätigte Host-Prüfung

Mindestens auszuführen:

```bash
cd /workspace
./gradlew clean testDebugUnitTest lintDebug assembleDebug
./gradlew installDebug
```

Manuell insbesondere prüfen:

- Schalten über das erste zugeordnete Geräte-WLAN
- Fallback auf das zweite zugeordnete WLAN
- auch bei bereits passender aktiver SSID wird eine explizite Verbindung
  angefordert
- Erfolgsmeldung im Dashboard
- Fehleranzeige bei nicht erreichbarem Gerät
- Timeout führt zu keinem zweiten Schaltversuch
- schneller Doppelklick startet nur eine Aktion
- Rückkehr ins vorherige WLAN nach Abschluss der angeforderten Verbindung

## Nächste Schritte

Issue 011 wird gemäß ausdrücklicher Benutzerfreigabe veröffentlicht und nach
erfolgreichem Pull-Request-Check nach `main` gemergt.

Das nächste fachliche Issue nach Abschluss von Issue 011 ist:

```text
docs/issues/012-import-export.md
```

Vor jeder weiteren Analyse, Planung oder Implementierung müssen `AGENTS.md`,
`ai-context.md`, `AI_SESSION_PROMPT.md`, diese Datei sowie die in
`AI_SESSION_PROMPT.md` vorgeschriebenen Projektdokumente vollständig gelesen
werden.
