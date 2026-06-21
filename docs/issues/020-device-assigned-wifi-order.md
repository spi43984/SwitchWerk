# Issue #20: Device Assigned WiFi Order

## Metadaten

- Status: Abgeschlossen
- Priorität: P1
- Typ: WLAN / Gerätezuordnung

## Ziel

Die einem Gerät zugewiesenen WLAN-Profile können vom Anwender manuell in die
gewünschte Reihenfolge gebracht werden.

Diese Reihenfolge ist für den Schaltvorgang verbindlich, weil die zugewiesenen
WLAN-Profile beim Verbinden genau in dieser Reihenfolge durchprobiert werden.

## Scope

- Reihenfolge der einem Gerät zugewiesenen WLAN-Profile im Gerätedialog anzeigen
- WLAN-Profile innerhalb eines Geräts über Hoch/Runter-Pfeile sortieren
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
- Anwender können die Reihenfolge über Hoch/Runter-Pfeile ändern
- Kleine Scroll-Hinweise zeigen an, wenn oberhalb oder unterhalb weitere
  Zuordnungen vorhanden sind
- Die gespeicherte Reihenfolge ist eindeutig und stabil
- Die UI macht klar, dass diese Reihenfolge die spätere Verbindungsreihenfolge bestimmt
- Nicht zugewiesene WLAN-Profile erscheinen nicht in dieser Liste

## Akzeptanzkriterien

- [x] Die einem Gerät zugewiesenen WLAN-Profile werden in einer sortierbaren
      Liste angezeigt
- [x] Die Reihenfolge kann über robuste Hoch/Runter-Pfeile geändert werden
- [x] Die Reihenfolge wird dauerhaft pro Gerät gespeichert
- [x] Die gespeicherte Reihenfolge wird nach einem App-Neustart wiederhergestellt
- [x] Der Schaltvorgang verwendet die gespeicherte Zuordnungsreihenfolge
- [x] Die Änderung betrifft nur die Gerätezuordnung, nicht die globale WLAN-Liste
- [x] Die manuelle Dashboard-Reihenfolge aus Issue 014 wird nicht beeinflusst
- [x] Die Sortierung beeinflusst nicht SSID, Profilname oder gespeicherte Zugangsdaten
- [x] Es werden keine sensiblen WLAN-Daten geloggt

## Umsetzungsentscheidung

Eine Drag-and-Drop-Variante wurde auf dem Zielgerät getestet, erwies sich bei
langen Listen aber insbesondere beim Verschieben nach oben als weniger robust
und schlechter bedienbar. Auf ausdrückliche Anwenderentscheidung verwendet die
finale Umsetzung deshalb den bereits im Projekt etablierten Hoch/Runter-
Mechanismus. Die 300 dp hohe Liste bleibt scrollbar; kleine Pfeile am oberen
und unteren Rand zeigen weitere nicht sichtbare Zuordnungen an.

Die bestehende `priority`-Spalte der Geräte-WLAN-Zuordnung wird als persistierte
Sortierposition verwendet. Deshalb war keine Room-Migration erforderlich.

## Bestätigte Prüfungen

- gezielter ViewModel-Test für Reihenfolge und Speichern
- bestehender Test der verbindlichen WLAN-Verbindungsreihenfolge
- Build und Installation auf dem Ubuntu-Host
- manuelle Sortierung und Wiederherstellung nach App-Neustart
- Import und Export mit erhaltener Zuordnungsreihenfolge
- globale WLAN-Liste und Dashboard-Gerätereihenfolge unverändert

## Testhinweise

- mehrere WLAN-Profile sind einem Gerät zugewiesen und werden in der
  eingestellten Reihenfolge dargestellt
- ein WLAN-Profil wird innerhalb der Zuordnung nach oben und unten verschoben
- nach einem App-Neustart bleibt die Reihenfolge erhalten
- der Schaltvorgang probiert die WLAN-Profile in genau dieser Reihenfolge
- eine Änderung der Zuordnungsreihenfolge verändert nicht die globale
  WLAN-Liste
- Geräte im Dashboard und die WLAN-Sortierung aus Issue 019 bleiben unverändert
