# Issue #27: WiFi Timeout Analysis And Stabilization

## Metadaten

- Status: Abgeschlossen
- Priorität: P0
- Typ: WLAN / Stabilität
- GitHub-Issue: #57
- Pull Request: #58
- Merge-Commit: `7779e1d`

## Ziel

Gelegentlich schlägt eine Schaltaktion mit Timeout fehl, obwohl das
zugeordnete WLAN vorhanden und grundsätzlich erreichbar ist.

Das Verhalten soll analysiert, reproduzierbar gemacht und anschließend
robuster behandelt werden.

## Hintergrund

Bei Geräten mit nur einem zugeordneten WLAN kommt es vereinzelt zu
Timeouts während des Verbindungsaufbaus oder unmittelbar vor dem
HTTP/RPC-Aufruf.

Der Fehler tritt nicht dauerhaft auf und deutet auf zeitliche
Abhängigkeiten zwischen Android-WLAN-Management, DHCP und HTTP-Kommunikation
hin.

## Fachliche Abhängigkeit

Issue 027 hängt fachlich an Issue 023, weil die WLAN-Timeout-Analyse die dort
geplante Detailanzeige mit Zeitstempeln nutzen soll.

Deshalb soll Issue 023 vor Issue 027 umgesetzt werden. Issue 023 kann dafür
bei Bedarf enger geschnitten werden: zuerst nur Diagnose, Detailanzeige und
Zeitstempel umsetzen. Die Theme-Umschaltung kann anschließend separat über
Issue 028 umgesetzt werden.

## Mögliche Ursachen

### Veraltete Scan-Ergebnisse

Android liefert möglicherweise veraltete Ergebnisse aus
`WifiManager.getScanResults()`.

Das gewünschte WLAN existiert bereits, wird aber noch nicht in den
aktuellen Scan-Ergebnissen angezeigt.

### Laufender WLAN-Scan

Android führt gerade einen Hintergrund-Scan durch.

Dadurch können:

- Verbindungsaufbau verzögert werden
- Scan-Ergebnisse verspätet erscheinen
- NetworkCallbacks verzögert eintreffen

### Verzögerte NetworkCallbacks

Das WLAN ist bereits verbunden, aber:

- `ConnectivityManager.NetworkCallback.onAvailable()`
  wird verspätet ausgelöst
- die App wartet auf ein Ereignis, das später eintrifft

### DHCP noch nicht abgeschlossen

Android meldet die WLAN-Verbindung bereits als hergestellt,
obwohl noch keine gültige IP-Adresse vorhanden ist.

Möglicher Ablauf:

1. WLAN verbunden
2. DHCP läuft
3. HTTP-Aufruf startet zu früh
4. Timeout

### Gerät aus Doze- oder Sleep-Zustand

Nach längerer Inaktivität benötigen folgende Komponenten
teilweise zusätzliche Zeit:

- WLAN-Subsystem
- ConnectivityManager
- DHCP
- DNS-Auflösung

### Shelly/Webserver noch nicht bereit

Das WLAN ist bereits erreichbar,
der HTTP- oder RPC-Endpunkt antwortet jedoch noch nicht.

### DNS-/Hostname-Auflösung

Bei Verwendung von Hostnamen statt IP-Adressen kann
eine verzögerte oder fehlgeschlagene Namensauflösung
zum Timeout führen.

## Anforderungen

### Diagnose verbessern

Die in Issue 023 geplante Detailanzeige soll genutzt werden.

Folgende Schritte müssen mit Zeitstempel protokolliert werden:

- Aktion gestartet
- WLAN-Anforderung gestartet
- WLAN gefunden
- WLAN verbunden
- IP-Adresse erhalten
- DNS-Auflösung gestartet
- DNS-Auflösung erfolgreich
- HTTP/RPC-Aufruf gestartet
- HTTP/RPC-Aufruf erfolgreich
- Fehler/Timeout

### Messung der Wartezeiten

Die Dauer zwischen den einzelnen Schritten soll erkennbar sein.

Beispiel:

12:00:01.123 Aktion gestartet
12:00:01.200 WLAN-Anforderung gestartet
12:00:03.150 WLAN verbunden
12:00:04.020 IP-Adresse erhalten
12:00:04.100 HTTP-Aufruf gestartet
12:00:04.320 HTTP-Aufruf erfolgreich

### Stabilisierung

Nach Abschluss der Analyse sollen gezielt Maßnahmen umgesetzt werden.

Mögliche Maßnahmen:

- längere Wartezeit auf NetworkCallback
- Prüfung auf gültige IP-Adresse
- Retry-Mechanismus
- HTTP-Aufruf erst nach erfolgreicher Netzvalidierung
- bessere Fehlerklassifizierung

## Nicht Bestandteil

- Änderung der bestehenden Gerätekonfiguration
- Unterstützung zusätzlicher WLAN-Typen
- Änderung des JSON-Exportformats

## Ergebnis der Analyse

Die Gerätemitschnitte zeigten mehrere voneinander unabhängige Wartephasen:

- Android konnte einen `NetworkRequest` annehmen, ohne zeitnah
  `onAvailable()` zu liefern.
- `onAvailable()` allein garantiert noch keine abgeschlossene IP-Konfiguration.
- DNS-Auflösung war bisher Teil des HTTP-Aufrufs und dadurch nicht separat
  messbar.
- Ein importierter, auf dem aktuellen Gerät noch nicht bestätigter
  Sicherheitstyp konnte einen irreführenden Android-Systemdialog auslösen,
  bevor der WPA2/WPA3-Fallback erfolgreich war.
- Bei deaktiviertem WLAN wurde zuvor nur ein allgemeiner Timeout sichtbar.

## Umgesetzte Stabilisierung

- vollständige Diagnoseereignisse für Aktion, WLAN-Anforderung, gefundenes
  WLAN, Verbindung, IP-Adresse, DNS sowie HTTP/RPC
- monotone Zeitdifferenz zwischen den einzelnen Diagnoseereignissen
- WLAN-Erfolg erst nach WiFi-Transport und gültiger Link-IP
- robustes Handling verspäteter, mehrfacher und verlorener NetworkCallbacks
- getrennte Klassifizierung von WLAN-Anforderungs-, DHCP/IP-, DNS- und
  HTTP-Timeouts
- netzgebundene DNS-Auflösung vor dem HTTP/RPC-Aufruf
- verständliche Sofortmeldung bei deaktiviertem WLAN
- gemeinsames 30-Sekunden-Budget für WPA2/WPA3-Fallback
- kein automatischer HTTP-Retry bei potenziell nicht-idempotenten
  Geräteaktionen

## Freigegebene Scope-Erweiterung

Die Erweiterung wurde während der Gerätetests ausdrücklich freigegeben:

- importierte Sicherheitstypen werden lokal als ungeprüft markiert
- vor der ersten Aktion eines importierten WLAN-Profils werden vorhandene
  Scan-Ergebnisse ausgewertet oder höchstens acht Sekunden auf einen
  Vordergrund-Scan gewartet
- der erkannte oder erfolgreich verwendete Sicherheitstyp wird lokal bestätigt
  und für spätere Aktionen gespeichert
- spätere Aktionen benötigen keinen erneuten Vorab-Scan
- Room-Datenbankversion 5 mit Migration 4 nach 5
- bestehende lokale WLAN-Profile gelten nach der Migration als geprüft
- `ACCESS_FINE_LOCATION` wird nur beim Import von WLAN-Profilen angefragt
- Scan-Ergebnisse und SSIDs werden weder zusätzlich gespeichert noch geloggt
- das JSON-Exportformat bleibt unverändert

## Bestätigte Prüfungen

- `git diff --check`
- `./gradlew testDebugUnitTest`
- `./gradlew clean assembleDebug`
- `./gradlew installDebug`
- bestehende Installation und Room-Migration erfolgreich
- Geräteaktion nach App-Neustart und Force-Stop erfolgreich
- Geräteaktion nach WLAN aus/an erfolgreich
- deaktiviertes WLAN verständlich gemeldet
- importiertes WPA3 für ein WPA2-WLAN wurde vor der ersten Aktion erkannt und
  anschließend lokal korrigiert
- weitere Geräteaktionen ohne erneuten Scan erfolgreich

## Akzeptanzkriterien

- [x] Diagnose zeigt den vollständigen Verbindungsablauf.
- [x] Zeitstempel und Zeitdifferenzen sind sichtbar.
- [x] Fehlerursachen können eindeutig eingegrenzt werden.
- [x] Spontane WLAN-Timeouts treten deutlich seltener auf.
- [x] Bekannte Fehlerfälle werden verständlich angezeigt.
