# Issue #20: Device Assigned WiFi Order

## Metadaten

- Status: Offen
- Priorität: P1
- Typ: WLAN / Gerätezuordnung

## Ziel

Die einem Gerät zugewiesenen WLAN-Profile können vom Anwender manuell in die
gewünschte Reihenfolge gebracht werden.

Diese Reihenfolge ist für den Schaltvorgang verbindlich, weil die zugewiesenen
WLAN-Profile beim Verbinden genau in dieser Reihenfolge durchprobiert werden.

## Scope

- Reihenfolge der einem Gerät zugewiesenen WLAN-Profile im Gerätedialog anzeigen
- WLAN-Profile innerhalb eines Geräts per Drag & Drop sortieren
- gespeicherte Zuordnungsreihenfolge pro Gerät dauerhaft in Room sichern
- Reihenfolge beim Schaltvorgang als Eingangsreihenfolge für Issue 011 verwenden
- Sortierung nur für die Gerätezuordnung, nicht für die globale WLAN-Liste
- Änderungen der Reihenfolge im UI unmittelbar sichtbar machen
- keine globale Änderung der WLAN-Profilverwaltung

## Nicht im Scope

- Änderung der globalen WLAN-Liste aus Issue 019
- Änderung der Geräte-Reihenfolge im Dashboard aus Issue 014
- automatische Sortierung nach SSID oder Profilname
- Änderung von SSID, Profilname oder gespeicherten Zugangsdaten
- neue Berechtigungen
- Cloud-Synchronisation

## UI-Verhalten

- In der Gerätebearbeitung ist die Reihenfolge der zugewiesenen WLANs sichtbar
- Anwender können die Reihenfolge per Drag & Drop ändern
- Die gespeicherte Reihenfolge ist eindeutig und stabil
- Die UI macht klar, dass diese Reihenfolge die spätere Verbindungsreihenfolge bestimmt
- Nicht zugewiesene WLAN-Profile erscheinen nicht in dieser Liste

## Akzeptanzkriterien

- [ ] Die einem Gerät zugewiesenen WLAN-Profile werden in einer sortierbaren
      Liste angezeigt
- [ ] Die Reihenfolge kann per Drag & Drop geändert werden
- [ ] Die Reihenfolge wird dauerhaft pro Gerät gespeichert
- [ ] Die gespeicherte Reihenfolge wird nach einem App-Neustart wiederhergestellt
- [ ] Der Schaltvorgang verwendet die gespeicherte Zuordnungsreihenfolge
- [ ] Die Änderung betrifft nur die Gerätezuordnung, nicht die globale WLAN-Liste
- [ ] Die manuelle Dashboard-Reihenfolge aus Issue 014 wird nicht beeinflusst
- [ ] Die Sortierung beeinflusst nicht SSID, Profilname oder gespeicherte Zugangsdaten
- [ ] Es werden keine sensiblen WLAN-Daten geloggt

## Testhinweise

- mehrere WLAN-Profile sind einem Gerät zugewiesen und werden in der
  eingestellten Reihenfolge dargestellt
- ein WLAN-Profil wird innerhalb der Zuordnung nach oben und unten verschoben
- nach einem App-Neustart bleibt die Reihenfolge erhalten
- der Schaltvorgang probiert die WLAN-Profile in genau dieser Reihenfolge
- eine Änderung der Zuordnungsreihenfolge verändert nicht die globale
  WLAN-Liste
- Geräte im Dashboard und die WLAN-Sortierung aus Issue 019 bleiben unverändert
