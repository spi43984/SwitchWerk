# Issue #27: WiFi Timeout Analysis And Stabilization

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
- Änderungen an Import/Export

## Akzeptanzkriterien

- Diagnose zeigt den vollständigen Verbindungsablauf.
- Zeitstempel sind sichtbar.
- Fehlerursachen können eindeutig eingegrenzt werden.
- Spontane WLAN-Timeouts treten deutlich seltener auf.
- Bekannte Fehlerfälle werden verständlich angezeigt.
