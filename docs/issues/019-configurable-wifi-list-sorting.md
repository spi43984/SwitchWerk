# Issue #19: Configurable WiFi List Sorting

## Metadaten

- Status: Abgeschlossen
- Priorität: P2
- Typ: GUI / WLAN-Verwaltung

## Ziel

WLAN-Profile können nach einem vom Anwender ausgewählten Kriterium sortiert
werden.

Geräte in der Geräteverwaltungsliste werden immer alphabetisch nach ihrem
sichtbaren Gerätenamen angezeigt und besitzen keine Sortier-Schaltfläche.

## Scope

- Sortier-Schaltfläche links neben `+` (also in der Reihenfolge i,
  Sortier-Schaltfläche, +) ausschließlich in der WLAN-Liste
- Auswahl des WLAN-Sortierkriteriums über ein verständliches Menü
- WLAN-Profile alphabetisch nach eindeutigem Profilnamen sortieren
- WLAN-Profile alphabetisch nach SSID sortieren
- WLAN-Profile pro Sortierkriterium auf- oder absteigend sortieren
- Geräte in der Geräteverwaltungsliste immer alphabetisch nach Gerätename
  sortieren
- aktive WLAN-Sortierung in der UI kenntlich machen
- WLAN-Sortierauswahl lokal speichern
- gespeicherte WLAN-Sortierung nach App-Neustart wiederherstellen
- Sortierung im ViewModel beziehungsweise Repository
- keine Sortierlogik direkt in Composables
- vorhandene Sortier-Schaltfläche in der Geräteverwaltungsliste entfernen

## Sortierkriterien für WLAN-Profile

- eindeutiger Profilname, A-Z oder Z-A
- SSID, A-Z oder Z-A

Die Sortierung erfolgt ohne Beachtung der Groß- und Kleinschreibung. Bei
gleichen Werten dient die eindeutige Profil-ID in aufsteigender Reihenfolge als
zweites Kriterium.

## Sortierung der Geräteverwaltungsliste

- Geräte in der Geräteverwaltungsliste werden automatisch nach ihrem Namen von
  A-Z sortiert. Bei gleichen Namen dient die interne Geräte-ID als zweiter
  Schlüssel.
- Anwender können die Gerätesortierung nicht ändern.
- In der Geräteverwaltungsliste wird kein Sortier-Icon angezeigt.

## Nicht im Scope

- manuelle Sortierung per Drag & Drop
- auswählbare Sortierkriterien für die Geräteverwaltungsliste
- Änderung bestehender IDs, Profilnamen oder SSIDs
- Filter- oder Suchfunktion
- Gruppierung nach Raum oder Gerätetyp
- Änderung der manuellen Dashboard-Reihenfolge aus Issue 014
- Cloud-Synchronisation der Sortierauswahl

## UI-Verhalten

- Die WLAN-Sortier-Schaltfläche steht direkt neben der `+`-Schaltfläche.
- Ein Klick öffnet die verfügbaren WLAN-Sortierkriterien.
- Das aktuell ausgewählte Kriterium ist eindeutig markiert.
- Die aktive Sortierrichtung ist eindeutig markiert.
- Die WLAN-Liste wird unmittelbar neu sortiert.
- In der Geräteverwaltungsliste wird kein Sortier-Icon angezeigt.
- Die Bedienung bleibt auf kleinen Displays verständlich und zugänglich.

## Akzeptanzkriterien

- [x] WLAN-Profile können nach eindeutigem Profilnamen sortiert werden
- [x] WLAN-Profile können nach SSID sortiert werden
- [x] WLAN-Profile können pro Kriterium auf- und absteigend sortiert werden
- [x] Die WLAN-Liste besitzt eine Sortier-Schaltfläche neben `+`
- [x] Das aktive WLAN-Sortierkriterium ist erkennbar
- [x] Die WLAN-Liste reagiert unmittelbar auf eine Änderung
- [x] Die WLAN-Sortierauswahl bleibt nach einem App-Neustart erhalten
- [x] Geräte in der Geräteverwaltungsliste werden immer alphabetisch nach
      Gerätename sortiert
- [x] In der Geräteverwaltungsliste wird kein Sortier-Icon angezeigt
- [x] Groß- und Kleinschreibung beeinflussen die Reihenfolge nicht
- [x] Die Reihenfolge bleibt bei gleichen Werten stabil
- [x] Die manuelle Dashboard-Reihenfolge aus Issue 014 wird nicht überschrieben
