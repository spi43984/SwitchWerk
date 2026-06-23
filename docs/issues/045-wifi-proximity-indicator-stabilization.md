# Issue 045: WiFi Proximity Indicator Stabilization

## Metadaten

- Status: Abgeschlossen
- Priorität: P0
- Typ: Bugfix / Stabilisierung / GUI
- Bereich: Dashboard / WLAN / Hilfe

## Ziel

Der Statuspunkt darf keine unzuverlässige physische WLAN-Nähe behaupten. Er
zeigt stattdessen einen nachvollziehbaren, bestätigten Verbindungszustand.

## Erkenntnisse aus der Untersuchung am Pixel

- WLAN, Standortdienste sowie `ACCESS_FINE_LOCATION`, `NEARBY_WIFI_DEVICES`
  und die WLAN-Berechtigungen waren aktiv.
- Der Systemscan führte das Geräte-WLAN wiederholt als sichtbar, während
  `WifiManager.getScanResults()` für SwitchWerk keine passende SSID lieferte.
  Der rote Punkt war somit kein verlässlicher Reichweitennachweis.
- Dieser Unterschied trat in mehreren direkten Vergleichsmessungen auf. Ein
  fehlender App-Scan-Eintrag rechtfertigt daher keinen Offline-Status.
- Der Scan-Cache schwankte; ein frischer Systemscan konnte das WLAN finden,
  obwohl vorherige Cache-Abfragen es nicht enthielten.
- Android drosselt App-Scans. Der frühere Startscan plus 30-Sekunden-Takt
  überschreitet die dokumentierte Grenze von vier Vordergrundscans in zwei
  Minuten.
- Eine `WifiNetworkSpecifier`-Anfrage öffnet erwartungsgemäß einen
  Android-Systemdialog. Sie ist die verlässliche Prüfung, ob Android das
  konfigurierte WLAN tatsächlich verbinden kann; der Dialog selbst ist weder
  ein Fehler noch ein Sichtbarkeitsbeweis.
- Ein Test lieferte `SecurityTypesFailed`: Beide Sicherheitsvarianten konnten
  nicht verbunden werden. Das ist ein Verbindungsfehler, kein Beleg dafür,
  dass die SSID physisch nicht sichtbar ist.
- Keine SSIDs, Passwörter, BSSIDs, IP-Adressen oder vollständigen Scanlisten
  wurden geloggt.

## Verbindliche Statusentscheidung

- **Grau – WLAN noch nicht bestätigt:** Standard nach App-Start oder wenn
  Android nur unzuverlässige Scaninformationen liefert.
- **Grün – WLAN bestätigt:** Ein zugeordnetes WLAN wurde positiv erkannt oder
  durch eine erfolgreiche Android-Verbindung bestätigt.
- **Rot – WLAN-Verbindung nicht möglich:** Nur nach einer konkreten
  fehlgeschlagenen `WifiNetworkSpecifier`-Anfrage, nicht wegen eines einzelnen
  fehlenden Scan-Eintrags.
- Der Statuspunkt löst niemals selbst eine WLAN-Verbindung oder Geräteaktion
  aus. Die Geräteaktion bleibt unabhängig vom Punkt bedienbar und wird durch
  Grau oder Rot nicht blockiert.

## Scope

- Keine periodischen aktiven WLAN-Scans als Voraussetzung für einen roten
  Status verwenden; Scan-Drosselung nicht durch Entwickleroptionen umgehen.
- Passive, frische positive Scan-Treffer dürfen ohne vorherigen
  Verbindungsfehler Grün liefern; fehlende Treffer bleiben unbestätigt. Ein
  sichtbarer Scan-Treffer überschreibt keinen konkret fehlgeschlagenen
  Verbindungsversuch.
- Verbindungsservice und Nähe-Service über einen kleinen, in-memory
  Bestätigungszustand verbinden: Erfolg bestätigt Grün, nicht verfügbare
  Netzwerk-Anfrage bzw. kombinierter Sicherheitsfehler bestätigt Rot.
- Bestehende Fehler für deaktiviertes WLAN, fehlende Berechtigung und
  deaktivierte Standortdienste verständlich anzeigen.
- Listen- und Widgetansicht verwenden denselben `MainViewModel`-Status.
- Hilfe-, Accessibility- und deutsch/englische String-Texte an das neue
  Statusmodell anpassen.

## Nicht im Scope

- Deaktivieren der Android-WLAN-Scan-Drosselung beim Anwender.
- Automatische Geräteaktion, automatischer WLAN-Wechsel oder aktiver Ping,
  DNS-, HTTP- oder Shelly-RPC-Check.
- Hintergrundüberwachung, zusätzliche Berechtigungen, Cloud, Tracking oder
  Analytics.
- Anzeige vollständiger Scanlisten oder Protokollierung sensitiver WLAN-Daten.

## Architektur

- Statusableitung bleibt in Netzwerk-/Service-Schicht; Compose rendert nur den
  fertigen `StateFlow` aus dem ViewModel.
- Bestehende Koin-, MVVM- und Flow-Struktur weiterverwenden.
- Ein kleiner `WifiProximityConfirmationStore` hält nur SSID und
  Bestätigungsart im Speicher der laufenden App; keine Persistenz und kein
  Logging.
- Der bestehende `WifiNetworkSpecifier`-Callback ist Quelle für verbindliche
  Erfolg-/Fehlerbestätigungen.
- Der primäre Sicherheitsversuch nutzt den Android-Plattform-Timeout; ein
  WPA3-Fallback erfolgt nur bei bestätigter WPA3-Fähigkeit. Eine in-memory
  Request-ID dokumentiert den Callback-Ablauf ohne WLAN- oder Gerätedaten zu
  loggen.

## Akzeptanzkriterien

- [x] Ein einzelner fehlender Scan setzt keinen Punkt auf Rot.
- [x] Nach App-Start ohne bestätigtes Ergebnis ist der Punkt grau und erklärt
  „WLAN noch nicht bestätigt“.
- [x] Erfolgreiche WLAN-Verbindung setzt zugeordnete Geräte auf Grün.
- [x] `NetworkRequestTimeout`, `Unavailable` und `SecurityTypesFailed` setzen
  das betroffene WLAN auf Rot mit „WLAN-Verbindung nicht möglich“.
- [x] Eine erfolgreiche Verbindung hebt Rot wieder auf Grün; ein bloß
  sichtbarer Scan-Treffer nicht.
- [x] Nicht zugeordnete Geräte werden nie fälschlich Grün.
- [x] Der Statuspunkt blockiert keine Geräteaktion.
- [x] Der erwartete Android-Systemdialog wird nicht als Scan- oder
  Sichtbarkeitsfehler interpretiert.
- [x] Listen- und Widgetansicht sowie Accessibility verwenden denselben Status.
- [x] Aktionspulsieren bleibt erhalten.
- [x] Keine dauerhaften Hintergrundscans und keine sensitiven Logs.
- [x] Relevante Unit-/UI-Tests decken unbekannten, bestätigten und
  fehlgeschlagenen Status ab.
- [x] Host-Prüfungen sind erfolgreich bestätigt.

## Abschluss

- GitHub-Issue: #93
- Pull Request: #94
- Merge-Commit: `e012223`

## Testhinweise

- App-Start ohne vorherige Aktion: grau statt rot.
- Erfolg mit einem ungefährlichen Testgerät oder bei gefahrlos möglicher
  Geräteaktion: danach grün.
- Fehlgeschlagene Verbindung: rot mit Verbindungsfehlermeldung.
- Rückkehr zu erfolgreicher Verbindung: wieder grün.
- WLAN aus, Berechtigung verweigert, Standortdienste aus, Listen-/Widgetansicht
  und TalkBack prüfen.

```bash
./gradlew lintDebug
./gradlew testDebugUnitTest
./gradlew clean assembleDebug
./gradlew installDebug
```
