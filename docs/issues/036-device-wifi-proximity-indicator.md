# Issue 036: Device WiFi Proximity Indicator

## Metadaten

- Status: Offen
- Priorität: P0
- Typ: Feature / GUI
- Bereich: Dashboard / WLAN

## Ziel

Neben dem Gerätenamen soll ein farbiger Punkt anzeigen, ob mindestens eines der
dem Gerät zugeordneten, in SwitchWerk konfigurierten WLANs aktuell in der Nähe
erkannt wird.

- Grün: Mindestens ein zugeordnetes WLAN wurde erkannt.
- Rot: Keines der zugeordneten WLANs wurde erkannt.

Die Anzeige dient nur als Hinweis auf die WLAN-Erreichbarkeit. Sie löst weder
eine Geräteaktion noch automatisch einen WLAN-Wechsel aus.

## Hintergrund

Vor einer Geräteaktion soll auf dem Dashboard schnell erkennbar sein, ob sich
das Smartphone wahrscheinlich in Reichweite eines für das Gerät vorgesehenen
WLANs befindet. Dadurch lassen sich voraussichtlich nicht erreichbare Geräte
früh erkennen, ohne zunächst eine Aktion auszuführen.

## Scope

### WLAN-Erkennung

- Die aktuell verbundenen WLAN-Informationen und verfügbare WLAN-Scanergebnisse
  werden mit den dem jeweiligen Gerät zugeordneten WLAN-Profilen verglichen.
- Der Vergleich erfolgt über die SSID.
- Bereits aktive, passende WLAN-Verbindungen zählen als erkannt.
- Ein Gerät erhält den Status `in der Nähe`, sobald mindestens eines seiner
  zugeordneten WLANs erkannt wurde.
- Ein Gerät erhält den Status `nicht in der Nähe`, wenn kein zugeordnetes WLAN
  erkannt wurde oder dem Gerät kein WLAN zugeordnet ist.
- Scanfehler und veraltete Ergebnisse werden sauber behandelt und dürfen keinen
  fälschlich grünen Status erzeugen.

### Dashboard-Anzeige

- Der Statuspunkt wird rechtsbündig auf derselben Zeile wie der Gerätename
  angezeigt.
- Die Position bleibt in Listen- und Widgetansicht konsistent.
- Lange Gerätenamen werden so gekürzt, dass der Statuspunkt sichtbar bleibt.
- Grün bedeutet: mindestens ein zugeordnetes WLAN ist in der Nähe.
- Rot bedeutet: kein zugeordnetes WLAN ist in der Nähe.
- Während eine Geräteaktion läuft, pulsiert der Statuspunkt und behält dabei
  seine aktuelle Farbe.
- Farbe und Animation dürfen nicht die einzigen Unterscheidungsmerkmale sein.
  Der Punkt erhält eine verständliche Accessibility-Beschreibung, zum Beispiel
  `WLAN in der Nähe`, `Kein WLAN in der Nähe` oder `Aktion läuft`.
- Die Anzeige muss in allen vorhandenen Dashboard-Darstellungen konsistent
  funktionieren.
- Lange Gerätenamen und kleine Displays bleiben nutzbar.

### Aktualisierung

- Der Status wird beim Öffnen beziehungsweise erneuten Aktivieren des
  Dashboards aktualisiert.
- Änderungen an Geräten oder WLAN-Zuordnungen aktualisieren die Anzeige.
- Die Aktualisierung berücksichtigt Androids Scan-Beschränkungen und startet
  keine unnötigen oder eng getakteten Scans.
- Es wird kein dauerhafter Hintergrundscan eingeführt.

### Fehler- und Berechtigungsbehandlung

- Fehlende oder verweigerte Berechtigungen führen nicht zu einem Absturz.
- Wenn Android keine verwertbaren WLAN-Informationen liefert, darf kein grüner
  Status angezeigt werden.
- Die UI soll verständlich darauf hinweisen, wenn die Erkennung wegen einer
  fehlenden Berechtigung oder deaktiviertem WLAN nicht verfügbar ist.
- Es werden keine SSIDs, Passwörter oder Scanergebnisse geloggt.

## Datenschutz und Android-Berechtigungen

- Es werden keine WLAN- oder Standortdaten an externe Dienste übertragen.
- Es werden nur die für die jeweilige Android-Version tatsächlich notwendigen
  Berechtigungen angefordert.
- Für neuere Android-Versionen ist vorrangig `NEARBY_WIFI_DEVICES` zu prüfen.
- Falls für unterstützte ältere Android-Versionen eine Standortberechtigung für
  WLAN-Scans erforderlich ist, muss deren Einführung vor der Implementierung
  ausdrücklich mit dem Benutzer abgestimmt werden. Hintergrund-Standortzugriff
  bleibt ausgeschlossen.

## Architekturhinweise

- Die WLAN-Erkennung wird außerhalb der Compose-UI gekapselt.
- Der ViewModel-Zustand wird über `StateFlow` bereitgestellt.
- Compose rendert ausschließlich den bereitgestellten Status.
- Vorhandene WLAN-Verbindungs- und Scanlogik soll wiederverwendet werden, sofern
  sie den benötigten Zustand zuverlässig liefert.
- Keine Cloud-, Tracking- oder Analytics-Abhängigkeit ergänzen.

## Nicht im Scope

- automatische Geräteaktion bei erkanntem WLAN
- automatischer WLAN-Wechsel aufgrund des Statuspunkts
- dauerhafte Hintergrundüberwachung
- Hintergrund-Standortzugriff
- Anzeige der Signalstärke oder Entfernung
- Auflistung sichtbarer SSIDs im Dashboard
- Änderung der WLAN-Reihenfolge eines Geräts

## Akzeptanzkriterien

- [ ] Neben jedem Gerätenamen wird rechtsbündig auf der Namenszeile ein WLAN-Statuspunkt angezeigt.
- [ ] Der Punkt bleibt auch bei langen Gerätenamen sichtbar.
- [ ] Der Punkt ist grün, wenn mindestens ein dem Gerät zugeordnetes WLAN erkannt wird.
- [ ] Der Punkt ist rot, wenn kein zugeordnetes WLAN erkannt wird.
- [ ] Der Punkt pulsiert während einer Geräteaktion und behält dabei seine aktuelle Farbe.
- [ ] Ein aktuell verbundenes, zugeordnetes WLAN führt zu einem grünen Status.
- [ ] Geräte ohne WLAN-Zuordnung erhalten keinen fälschlich grünen Status.
- [ ] Der Status wird beim Öffnen oder erneuten Aktivieren des Dashboards aktualisiert.
- [ ] Änderungen an WLAN-Zuordnungen werden in der Anzeige berücksichtigt.
- [ ] Fehlende Berechtigungen, deaktiviertes WLAN und Scanfehler werden verständlich behandelt.
- [ ] Die Anzeige ist per Accessibility-Beschreibung verständlich.
- [ ] Es findet kein dauerhafter Hintergrundscan statt.
- [ ] Es werden keine sensiblen WLAN-Daten geloggt oder extern übertragen.
- [ ] Bestehende Geräteaktionen und WLAN-Verbindungen funktionieren unverändert.
- [ ] Relevante Unit- und UI-Tests sind ergänzt.
- [ ] Build und Installation wurden auf dem Ubuntu-Host erfolgreich geprüft.

## Testhinweise

- Gerät mit einem zugeordneten und sichtbaren WLAN
- Gerät mit mehreren zugeordneten WLANs, davon mindestens eines sichtbar
- Gerät mit ausschließlich nicht sichtbaren WLANs
- Gerät ohne WLAN-Zuordnung
- Smartphone ist bereits mit einem zugeordneten WLAN verbunden
- WLAN am Smartphone deaktiviert
- notwendige Berechtigung verweigert
- Rückkehr aus den Android-Einstellungen nach Erteilen der Berechtigung
- Änderung einer WLAN-Zuordnung bei geöffnetem Dashboard
- App-Wechsel und erneutes Aktivieren des Dashboards
- mehrere Geräte mit unterschiedlichen WLAN-Zuordnungen
- lange Gerätenamen und kleine Displays
- TalkBack beziehungsweise Semantik der Statusanzeige
