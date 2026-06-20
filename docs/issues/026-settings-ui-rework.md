# Issue #26: Settings UI Rework

## Metadaten

* Status: abgeschlossen
* Priorität: P0
* Typ: GUI / Einstellungen

## Ziel

Die Einstellungen sollen übersichtlicher, moderner und konsistenter gestaltet werden.

Die Navigation soll vereinheitlicht werden und alle Konfigurationsdialoge sollen ein gemeinsames Erscheinungsbild erhalten.

Zusätzlich soll die Einstellungsansicht nicht mehr aus einer langen vertikalen Liste aller Bereiche bestehen. Stattdessen soll eine Bereichsauswahl mit Tabs verwendet werden, damit WLAN- und Gerätelisten deutlich mehr Platz erhalten und weniger gescrollt werden muss.

## Scope

### Navigation

* Einstellungen nicht mehr als eigener Dashboard-Button
* Hamburger-Menü rechts oben in der App-Bar
* Menüeinträge:

  * Einstellungen
  * Hilfe (entspricht Hilfe im Einstellungsmenü, System)
* Architektur für spätere weitere Menüpunkte vorbereiten

### Einstellungen als Bereichsansicht

Die Einstellungen sollen in fachliche Bereiche gegliedert werden.

Oben wird eine Tab-/Segment-Auswahl angezeigt:

* WLAN-Profile
* Geräte
* System
* Backup

Anforderungen:

* Der aktive Bereich wird deutlich hervorgehoben
* Es wird immer nur der aktuell ausgewählte Bereich angezeigt
* Keine lange vertikale Gesamtliste aller Bereiche
* WLAN- und Gerätelisten erhalten dadurch möglichst viel vertikalen Platz
* Weniger Scrollen zwischen unterschiedlichen Themenbereichen
* Mobilfreundliche Umsetzung
* Keine Mehrspaltenansicht erforderlich

Zuordnung:

#### WLAN-Profile

* Verwaltung aller WLAN-Profile

#### Geräte

* Verwaltung aller Geräte

#### System

Enthält alle appweiten Einstellungen, die nicht direkt Geräte,
WLAN-Profile oder Backup betreffen.

Aktueller Umfang:

##### Darstellung

* Theme-Einstellungen

  * Systemvorgabe
  * Hell
  * Dunkel

##### Aktionsdetails

* Aktionsdetails anzeigen
* Höhe des Detailbereichs

  * 20 %
  * 30 %
  * 40 %
* Sortierung Aktionsdetails

  * Neueste oben
  * Neueste unten

##### Hilfe

* Hilfe anzeigen (öffnet dieselbe Hilfeansicht wie der Menüpunkt Hilfe im Hamburger-Menü)
* Versionsnummer
* Link zum GitHub-Projekt
* kurze Erklärung der App

Zukünftige appweite Einstellungen sollen ebenfalls in diesem
Bereich eingeordnet werden.

#### Backup

##### Export

* Exportieren
* Exportieren mit Passwörtern

##### Import

* Datei importieren
* URL importieren
* QR-Code importieren

Die Bereiche Export und Import optisch voneinander trennen.

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

Keine Option und kein Aktionsbutton soll vorausgewählt (und damit in anderer Farbe) dargestellt werden.

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

Die Hilfe soll als eigenständige wiederverwendbare UI-Komponente implementiert werden.

Sie muss erreichbar sein über:

* Hamburger-Menü → Hilfe
* Einstellungen → System → Hilfe

Beide Wege müssen dieselbe Hilfeansicht anzeigen.

Initialer Umfang:

* Versionsnummer anzeigen
* Link zum GitHub-Projekt
* kurze Erklärung der App
* ggf. zukünftig weitere Hilfethemen

### Wiederverwendbare UI-Komponenten

Für eine konsistente Oberfläche sollen gemeinsame Compose-Komponenten eingeführt werden:

* Standard-Konfigurationsdialog
* Standard-Dialogbuttons
* gemeinsame Layout- und Abstandskonventionen
* wiederverwendbare Einstellungs-Tabs/Bereichsauswahl
* wiederverwendbare Hilfe-Komponente

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

```text
ui/components/
```

abgelegt werden.

ViewModels und Repositories sollen möglichst unverändert bleiben.

Die Bereichsauswahl der Einstellungen soll als wiederverwendbare Compose-Komponente umgesetzt werden.

Die Hilfe soll als wiederverwendbare UI-Komponente umgesetzt werden und sowohl vom Hamburger-Menü als auch vom System-Bereich verwendet werden.

## Akzeptanzkriterien

* [x] Hamburger-Menü vorhanden
* [x] Einstellungen über Menü erreichbar
* [x] Hilfe über Menü erreichbar
* [x] Hilfe über System-Bereich erreichbar
* [x] Beide Wege zeigen dieselbe Hilfeansicht
* [x] Bereichsauswahl mit WLAN-Profile, Geräte, System und Backup vorhanden
* [x] Es wird immer nur der ausgewählte Bereich angezeigt
* [x] System-Bereich enthält Darstellungsoptionen
* [x] System-Bereich enthält Aktionsdetail-Einstellungen
* [x] Backup-Bereich enthält Import und Export
* [x] WLAN-Dialoge verwenden ein einheitliches Layout
* [x] Geräte-Dialoge verwenden ein einheitliches Layout
* [x] Buttons haben einheitliche Darstellung
* [x] Import und Export sind optisch getrennt
* [x] Wiederverwendbare Dialogkomponenten vorhanden
* [x] Wiederverwendbare Bereichsauswahl vorhanden
* [x] Wiederverwendbare Hilfe-Komponente vorhanden
* [x] Alle Dialoge bleiben auf kleinen Displays nutzbar
* [x] Dark Mode funktioniert weiterhin

## Testhinweise

* Navigation über Hamburger-Menü testen
* Hilfe über Hamburger-Menü öffnen
* Hilfe über Einstellungen → System → Hilfe öffnen
* Prüfen, dass beide Wege dieselbe Hilfeansicht anzeigen
* Einstellungen öffnen
* Zwischen allen Einstellungsbereichen wechseln
* WLAN-Profil anlegen
* WLAN-Profil bearbeiten
* Gerät anlegen
* Gerät bearbeiten
* Import-Bereich prüfen
* Export-Bereich prüfen
* Darstellung auf kleinem Display prüfen
* Darstellung im Dark Mode prüfen
* Darstellung im Light Mode prüfen
