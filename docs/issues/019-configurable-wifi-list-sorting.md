# Issue #19: Configurable WiFi List Sorting

## Ziel

WLAN-Profile können nach einem vom Anwender ausgewählten Kriterium sortiert
werden.

Geräte in der Geräteverwaltungsliste werden immer alphabetisch nach ihrer
eindeutigen ID angezeigt und besitzen keine Sortier-Schaltfläche.

## Scope

- Sortier-Schaltfläche neben `+` ausschließlich in der WLAN-Liste
- Auswahl des WLAN-Sortierkriteriums über ein verständliches Menü
- WLAN-Profile alphabetisch nach eindeutigem Profilnamen sortieren
- WLAN-Profile alphabetisch nach SSID sortieren
- Geräte in der Geräteverwaltungsliste immer alphabetisch nach eindeutiger
  Geräte-ID sortieren
- aktive WLAN-Sortierung in der UI kenntlich machen
- WLAN-Sortierauswahl lokal speichern
- gespeicherte WLAN-Sortierung nach App-Neustart wiederherstellen
- Sortierung im ViewModel beziehungsweise Repository
- keine Sortierlogik direkt in Composables
- vorhandene Sortier-Schaltfläche in der Geräteverwaltungsliste entfernen

## Sortierkriterien für WLAN-Profile

- eindeutiger Profilname, A-Z
- SSID, A-Z

Die Sortierung erfolgt ohne Beachtung der Groß- und Kleinschreibung. Bei
gleichen Werten dient die eindeutige Profil-ID als zweites Kriterium.

## Sortierung der Geräteverwaltungsliste

- Geräte in der Geräteverwaltungsliste werden automatisch nach ihrer
  eindeutigen ID von A-Z sortiert.
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
- Die WLAN-Liste wird unmittelbar neu sortiert.
- In der Geräteverwaltungsliste wird kein Sortier-Icon angezeigt.
- Die Bedienung bleibt auf kleinen Displays verständlich und zugänglich.

## Akzeptanzkriterien

- [ ] WLAN-Profile können nach eindeutigem Profilnamen sortiert werden
- [ ] WLAN-Profile können nach SSID sortiert werden
- [ ] Die WLAN-Liste besitzt eine Sortier-Schaltfläche neben `+`
- [ ] Das aktive WLAN-Sortierkriterium ist erkennbar
- [ ] Die WLAN-Liste reagiert unmittelbar auf eine Änderung
- [ ] Die WLAN-Sortierauswahl bleibt nach einem App-Neustart erhalten
- [ ] Geräte in der Geräteverwaltungsliste werden immer alphabetisch nach
      eindeutiger ID sortiert
- [ ] In der Geräteverwaltungsliste wird kein Sortier-Icon angezeigt
- [ ] Groß- und Kleinschreibung beeinflussen die Reihenfolge nicht
- [ ] Die Reihenfolge bleibt bei gleichen Werten stabil
- [ ] Die manuelle Dashboard-Reihenfolge aus Issue 014 wird nicht überschrieben
