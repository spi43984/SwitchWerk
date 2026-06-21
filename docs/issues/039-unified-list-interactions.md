# Issue #39: Unified List Interactions

## Metadaten

* Status: offen
* Priorität: P0
* Typ: GUI / Bedienkonzept

## Ziel

Alle Konfigurationslisten sollen ein einheitliches Bedienkonzept verwenden.

Einträge sollen direkt durch Antippen bearbeitbar sein. Löschen soll einheitlich über Swipe erfolgen. Separate Bearbeiten-Icons sollen dort entfernt werden, wo der Listeneintrag selbst eindeutig antippbar ist.

Dadurch soll die Bedienung konsistenter, schneller und besser auf mobilen Geräten nutzbar werden.

## Ausgangslage

Nach Issue 016 können Einträge teilweise bereits durch Klick auf den Namen bearbeitet werden.

Nach Issue 026 sind Dialoge, Buttons und Einstellungsbereiche optisch vereinheitlicht.

Die Liste der einem Gerät zugeordneten WLANs weicht aktuell noch ab:

* Bearbeiten erfolgt über ein Bleistift-Icon.
* Der Listeneintrag selbst ist nicht einheitlich direkt editierbar.
* Löschen ist nicht einheitlich per Swipe umgesetzt.

## Scope

### Einheitliches Listenverhalten

Für Konfigurationslisten gilt künftig:

* Tap auf Listeneintrag öffnet den Bearbeitungsdialog.
* Swipe löscht den Eintrag oder startet die vorhandene Löschbestätigung.
* Bearbeiten-Icons werden entfernt, sofern sie durch Tap auf den Eintrag ersetzt werden können.
* Das Verhalten soll in allen betroffenen Listen gleich sein.

### Betroffene Listen

Mindestens zu prüfen und bei Bedarf anzupassen:

* WLAN-Profile
* Geräte
* einem Gerät zugeordnete WLAN-Profile
* zukünftige Konfigurationslisten

### Geräte-WLAN-Zuordnungen

Die Liste der einem Gerät zugeordneten WLANs ist explizit Bestandteil dieses Issues.

Anforderungen:

* Antippen einer Zuordnung öffnet die Bearbeitung der Zuordnung.
* Löschen erfolgt per Swipe mit vorhandener Sicherheitslogik.
* Die bestehende Sortierung über Hoch-/Runter-Pfeile bleibt erhalten.
* Die Reihenfolge der zugeordneten WLANs darf durch Swipe-Löschen oder Tap-Bearbeiten nicht verändert werden.
* Die Bedienung muss mit der bestehenden 300-dp-Liste und den Scroll-Hinweisen kompatibel bleiben.

### Sicherheit bei Löschaktionen

Bei Lösch- oder Sicherheitsdialogen bleibt die bestehende UI-Regel gültig:

* links: Bestätigen / Löschen
* rechts: Nein / Abbrechen

## Nicht Bestandteil

* Änderung der WLAN-Verbindungslogik
* Änderung der Geräteaktionslogik
* Änderung der persistierten Reihenfolge zugeordneter WLANs
* Drag-and-Drop-Sortierung
* neue Gerätefunktionen
* neue WLAN-Funktionen
* Änderung der Import-/Export-Logik

## Architektur

Die Änderung soll möglichst in der Compose-Schicht erfolgen.

Falls möglich, soll ein wiederverwendbares Listen-Interaktionsmuster unter:

ui/components/

verwendet oder ergänzt werden.

ViewModels, Repositories und Datenmodell sollen nur geändert werden, wenn dies für die UI-Interaktion zwingend erforderlich ist.

Bestehende Sortierlogik für zugeordnete WLANs bleibt unverändert.

## Akzeptanzkriterien

* [ ] WLAN-Profile sind per Tap auf den Listeneintrag editierbar
* [ ] Geräte sind per Tap auf den Listeneintrag editierbar
* [ ] einem Gerät zugeordnete WLANs sind per Tap auf den Listeneintrag editierbar
* [ ] Löschaktionen erfolgen einheitlich per Swipe
* [ ] vorhandene Löschbestätigungen bleiben erhalten
* [ ] sichere Abbruchaktion steht rechts
* [ ] unnötige Bleistift-Icons sind entfernt
* [ ] Hoch-/Runter-Pfeile für Geräte-WLAN-Zuordnungen bleiben erhalten
* [ ] Reihenfolge zugeordneter WLANs bleibt unverändert persistiert
* [ ] Verhalten ist im Dark Mode und Light Mode konsistent
* [ ] Bedienung funktioniert auf kleinen Displays

## Testhinweise

* WLAN-Profil per Tap bearbeiten
* WLAN-Profil per Swipe löschen
* Gerät per Tap bearbeiten
* Gerät per Swipe löschen
* Zugeordnetes WLAN per Tap bearbeiten
* Zugeordnetes WLAN per Swipe löschen
* Reihenfolge mit Hoch-/Runter-Pfeilen ändern
* Gerät speichern und erneut öffnen
* Reihenfolge prüfen
* Löschdialoge prüfen
* Light Mode prüfen
* Dark Mode prüfen
* kleines Display prüfen
