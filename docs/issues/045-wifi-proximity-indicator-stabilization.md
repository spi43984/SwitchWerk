# Issue 045: WiFi Proximity Indicator Stabilization

## Metadaten

- Status: Offen
- Priorität: P0
- Typ: Bugfix / Stabilisierung / GUI
- Bereich: Dashboard / WLAN / Hilfe

## Ziel

Die WLAN-Näheanzeige im Dashboard soll auch in Umgebungen mit vielen WLANs stabil und nachvollziehbar bleiben.

Aktuell kann der Geräte-Statuspunkt zwischen grün und rot wechseln oder dauerhaft rot bleiben, obwohl das zugeordnete Geräte-WLAN tatsächlich vorhanden ist. Ursache können unvollständige, verzögerte oder gecachte Android-WLAN-Scanergebnisse sein.

## Hintergrund

Issue 036 hat den Geräte-WLAN-Näheindikator eingeführt. In einfachen WLAN-Umgebungen funktioniert die Anzeige zuverlässig. In Werkstatt- oder Industrieumgebungen mit vielen Access Points kann Android jedoch wechselnde oder unvollständige Scanlisten liefern. Ein einzelner fehlender Scan-Eintrag darf daher nicht sofort als zuverlässiger Offline-Zustand interpretiert werden.

## Problemstellung

Beobachtetes Verhalten:

- Zuhause mit wenigen WLANs wird ein Shelly regelmäßig erkannt.
- In einer Werkstatt mit mehreren WLANs wechselt die Anzeige häufig zwischen grün und rot.
- Teilweise bleibt die Anzeige dauerhaft rot, obwohl das Geräte-WLAN vorhanden ist.
- Dadurch wirkt die App unzuverlässig, obwohl wahrscheinlich die Android-Scanergebnisse schwanken.

Mögliche Ursachen:

- Android liefert WLAN-Scanergebnisse verzögert.
- Android liefert nicht bei jedem Scan alle sichtbaren WLANs.
- Scan-Ergebnisse können gecacht oder veraltet sein.
- Die Scanfrequenz wird durch Android aus Akku- und Datenschutzgründen begrenzt.
- In WLAN-reichen Umgebungen können einzelne SSIDs temporär aus der Scanliste fehlen.
- Ein einzelner fehlender Scan kann aktuell zu schnell zu einem roten Status führen.

## Scope

- Last-Seen-Zeitstempel für erkannte Geräte-WLANs ergänzen.
- Hysterese gegen Statusflattern einführen.
- Kurzzeitig fehlende Scanergebnisse dürfen den Status nicht sofort auf rot setzen.
- Aktuell verbundenes WLAN, frische Scanergebnisse und zuletzt gesehene passende WLANs berücksichtigen.
- Fehlende Berechtigungen, deaktiviertes WLAN, deaktivierte System-Standortdienste und Scanfehler weiterhin sauber behandeln.
- Listen- und Widgetansicht müssen dieselbe stabilisierte Logik verwenden.
- Das bestehende Pulsieren während laufender Geräteaktionen bleibt erhalten.

## Hilfetexte und Benutzerverständnis

Die Hilfetexte müssen angepasst werden.

Zu erklären ist:

- was der Statuspunkt bedeutet
- dass die Anzeige auf Android-WLAN-Erkennung basiert
- dass Android WLAN-Listen verzögert oder unvollständig liefern kann
- dass grün eine wahrscheinliche WLAN-Nähe bedeutet
- dass rot nicht zwingend beweist, dass das Gerät physisch ausgeschaltet ist
- was ein optionaler Zwischenstatus bedeutet, falls eingeführt
- dass die Anzeige keine aktive HTTP-Erreichbarkeitsprüfung ersetzt
- dass keine WLAN-Daten extern übertragen werden

Betroffene Texte voraussichtlich:

- deutsche String-Ressourcen
- englische String-Ressourcen
- Hilfeansicht / Hilfedialoge
- Accessibility-Beschreibungen des Statuspunkts

## Datenschutz und Sicherheit

- Keine SSIDs, Passwörter oder vollständigen Scanlisten loggen.
- Keine Cloud-, Tracking- oder Analytics-Abhängigkeiten ergänzen.
- Keine Hintergrund-Standortüberwachung einführen.
- Keine neuen Berechtigungen ohne ausdrückliche Prüfung und Begründung.
- Kein dauerhafter Hintergrundscan.

## Nicht im Scope

- automatische Geräteaktion bei erkanntem WLAN
- automatischer WLAN-Wechsel aufgrund des Statuspunktes
- aktive HTTP-Erreichbarkeitsprüfung des Gerätes
- Ping-, DNS- oder Shelly-RPC-Diagnose
- dauerhafte Hintergrundüberwachung
- Anzeige vollständiger WLAN-Scanlisten
- Änderung der Geräte-WLAN-Reihenfolge

## Architekturhinweise

- Die Stabilisierung gehört in die gekapselte WLAN-Erkennungslogik, nicht direkt in Compose.
- Das ViewModel soll weiterhin nur fertigen UI-State veröffentlichen.
- Compose rendert ausschließlich den bereitgestellten Status.
- Bestehende Services und StateFlow-Strukturen aus Issue 036 sollen weiterverwendet werden.
- Die Lösung soll klein und nachvollziehbar bleiben.

Voraussichtlich betroffene Komponenten:

- `WifiProximityService`
- `AndroidWifiProximityService`
- `MainViewModel`
- Dashboard-Statusanzeige
- String-Ressourcen
- Hilfe-Komponenten
- Tests für ViewModel und Statusanzeige

## Akzeptanzkriterien

- [ ] Ein einzelnes fehlendes Android-Scanergebnis setzt den Gerätepunkt nicht sofort auf rot.
- [ ] Die Anzeige flackert in WLAN-reichen Umgebungen deutlich weniger.
- [ ] Ein aktuell verbundenes, zugeordnetes WLAN führt weiterhin zuverlässig zu einem grünen Status.
- [ ] Geräte ohne WLAN-Zuordnung erhalten keinen fälschlich grünen Status.
- [ ] Fehlende Berechtigungen, deaktiviertes WLAN, deaktivierte System-Standortdienste und Scanfehler werden weiterhin verständlich behandelt.
- [ ] Die Listen- und Widgetansicht verwenden dieselbe stabilisierte Logik.
- [ ] Während einer Geräteaktion pulsiert der Statuspunkt weiterhin.
- [ ] Accessibility-Beschreibungen erklären den tatsächlichen Status korrekt.
- [ ] Hilfetexte erklären die Bedeutung und Grenzen der WLAN-Näheanzeige.
- [ ] Es werden keine SSIDs, Passwörter oder Scanergebnisse geloggt.
- [ ] Es wird kein dauerhafter Hintergrundscan eingeführt.
- [ ] Es werden keine Cloud-, Tracking- oder Analytics-Abhängigkeiten ergänzt.
- [ ] Relevante Unit- und UI-Tests sind ergänzt oder angepasst.
- [ ] Build und Installation wurden auf dem Ubuntu-Host erfolgreich geprüft.

## Testhinweise

Manuell testen:

- Zuhause mit wenigen WLANs
- Werkstatt oder Umgebung mit vielen sichtbaren WLANs
- Gerät mit einem zugeordneten sichtbaren WLAN
- Gerät mit mehreren zugeordneten WLANs
- Gerät mit ausschließlich nicht sichtbaren WLANs
- Gerät ohne WLAN-Zuordnung
- Smartphone ist bereits mit einem zugeordneten WLAN verbunden
- WLAN am Smartphone deaktiviert
- notwendige Berechtigung verweigert
- System-Standortdienste deaktiviert
- App-Wechsel und Rückkehr zum Dashboard
- längere Beobachtung des Statuspunktes ohne Geräteaktion
- Listenansicht und Widgetansicht
- TalkBack beziehungsweise Semantik der Statusanzeige

Host-Prüfungen nach Implementierung:

```bash
./gradlew lintDebug
./gradlew testDebugUnitTest
./gradlew clean assembleDebug
./gradlew installDebug
```
