# AI Handoff

Stand: 14. Juni 2026

## Aktuelle Arbeit

Issue 011 "Device Action With WiFi Fallback" ist auf dem Feature-Branch
`device-action-with-wifi-fallback` implementiert und im Container geprüft.

- GitHub-Issue: #21
- Issue-URL: https://github.com/spi43984/SwitchWerk/issues/21
- lokales Issue: `docs/issues/011-device-action-with-wifi-fallback.md`
- kein Commit
- kein Push
- kein Pull Request
- lokale Issue-Datei noch nicht abgehakt

Die Veröffentlichung und der Abschluss dürfen erst nach ausdrücklicher
Anforderung des Benutzers gemäß Phase 2 aus `AGENTS.md` erfolgen.

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

Der Container-Build ist erfolgreich. Gemäß Projektregel gilt der Build erst
nach erfolgreicher Rückmeldung des Benutzers für Build, Installation und
manuelle Tests auf dem Host als bestätigt.

## Ausstehende Host-Prüfung

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

Nach erfolgreicher Host-Rückmeldung kann der Benutzer ausdrücklich die
Veröffentlichung von Issue 011 anfordern. Erst dann committen, pushen und einen
Pull Request erstellen. Merge, Abhaken der lokalen Issue-Datei, Schließen von
GitHub-Issue #21 und Löschen des Branches benötigen die in `AGENTS.md`
vorgesehenen ausdrücklichen Freigaben.

Das nächste fachliche Issue nach Abschluss von Issue 011 ist:

```text
docs/issues/012-import-export.md
```

Vor jeder weiteren Analyse, Planung oder Implementierung müssen `AGENTS.md`,
`ai-context.md`, `AI_SESSION_PROMPT.md`, diese Datei sowie die in
`AI_SESSION_PROMPT.md` vorgeschriebenen Projektdokumente vollständig gelesen
werden.
