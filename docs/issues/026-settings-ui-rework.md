# Issue #26: Settings UI Rework

## Ziel

Die Einstellungen sollen übersichtlicher, moderner und konsistenter gestaltet werden.

Die Navigation soll vereinheitlicht werden und alle Konfigurationsdialoge sollen ein gemeinsames Erscheinungsbild erhalten.

## Scope

### Navigation

* Einstellungen nicht mehr als eigener Dashboard-Button
* Hamburger-Menü rechts oben in der App-Bar
* Menüeinträge:

  * Einstellungen
  * Hilfe
* Architektur für spätere weitere Menüpunkte vorbereiten

### Einheitliche Dialoge

Alle Bearbeitungsdialoge sollen ein einheitliches Erscheinungsbild besitzen:

* WLAN-Profil anlegen
* WLAN-Profil bearbeiten
* Gerät anlegen
* Gerät bearbeiten
* zukünftige Konfigurationsdialoge

Anforderungen:

* gleicher Dialogaufbau
* gleiche Abstände
* gleiche Titelgestaltung
* gleiche Scrollbarkeit
* gleiche Button-Anordnung
* gleiche Dialogbreiten
* einheitliches Verhalten bei kleinen Displays

### Einheitliche Buttons

Aktionsbuttons sollen einheitlich dargestellt werden:

* gleiche Höhe
* gleiche Mindestbreite
* Text immer zentriert
* keine linksbündig wirkenden Umbrüche
* längere Beschriftungen durch Layout lösen

Beispiele:

* Speichern
* Abbrechen
* Passwort leeren

sollen optisch gleichwertig erscheinen.

### Import / Export Bereich

Import und Export sollen optisch klar getrennt werden.

#### Export

* Exportieren
* Exportieren mit Passwörtern

#### Import

* Datei importieren
* URL importieren
* QR-Code importieren

Anforderungen:

* eigene Abschnittsüberschriften
* optische Trennung
* Aktionen eindeutig zuordenbar
* konsistente Abstände und Gruppierung

### Hilfe

Neuer Menüpunkt Hilfe.

Initialer Umfang:

* Versionsnummer anzeigen
* Link zum GitHub-Projekt
* kurze Erklärung der App

### Wiederverwendbare UI-Komponenten

Für eine konsistente Oberfläche sollen gemeinsame Compose-Komponenten eingeführt werden:

* Standard-Konfigurationsdialog
* Standard-Dialogbuttons
* gemeinsame Layout- und Abstandskonventionen

## Nicht Bestandteil

* Änderung der WLAN-Logik
* Änderung der Geräteaktionslogik
* Änderung der Import-/Export-Logik
* Änderung der Passwortspeicherung
* neue Gerätefunktionen
* neue WLAN-Funktionen

## Architektur

UI-Änderungen erfolgen ausschließlich in der Compose-Schicht.

Neue wiederverwendbare Komponenten sollen unter:

ui/components/

abgelegt werden.

ViewModels und Repositories sollen möglichst unverändert bleiben.

## Akzeptanzkriterien

* [ ] Hamburger-Menü vorhanden
* [ ] Einstellungen über Menü erreichbar
* [ ] Hilfe über Menü erreichbar
* [ ] WLAN-Dialoge verwenden ein einheitliches Layout
* [ ] Geräte-Dialoge verwenden ein einheitliches Layout
* [ ] Buttons haben einheitliche Darstellung
* [ ] Import und Export sind optisch getrennt
* [ ] Wiederverwendbare Dialogkomponenten vorhanden
* [ ] Alle Dialoge bleiben auf kleinen Displays nutzbar
* [ ] Dark Mode funktioniert weiterhin

## Testhinweise

* Navigation über Hamburger-Menü testen
* Einstellungen öffnen
* Hilfe öffnen
* WLAN-Profil anlegen
* WLAN-Profil bearbeiten
* Gerät anlegen
* Gerät bearbeiten
* Import-Bereich prüfen
* Export-Bereich prüfen
* Darstellung auf kleinem Display prüfen
* Darstellung im Dark Mode prüfen
* Darstellung im Light Mode prüfen

