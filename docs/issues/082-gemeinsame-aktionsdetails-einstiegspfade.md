# Issue 082: Gemeinsame Aktionsdetails für alle Einstiegspfade

## Metadaten

- Status: Offen
- Priorität: P2
- Typ: Architektur / Diagnose / UX
- Bereich: Aktionsdetails / Dashboard / Widgets / Shortcuts / Intents

## Ziel

Alle lokal gestarteten Geräte- und Schaltgruppenaktionen sollen ihre
Diagnoseereignisse über eine gemeinsame Erfassungsschicht in den bestehenden
Aktionsdetails bereitstellen.

Dies gilt unabhängig davon, ob eine Aktion über das Dashboard, ein
Homescreen-Widget, einen App Shortcut oder einen externen Intent gestartet
wurde.

## Hintergrund

Die Aktionsdetails wurden mit Issue 023 als flüchtige Diagnoseanzeige im
Dashboard eingeführt. Der aktuelle Zustand wird jedoch überwiegend im
Dashboard-/ViewModel-Pfad geführt.

Homescreen-Widget-Aktionen delegieren zwar an dieselben
`DeviceActionService`- und `SwitchGroupActionService`-Implementierungen, reichen
deren Diagnoseereignisse aber nicht an die Aktionsdetails weiter. Nach Ablauf
der kurzen Widget-Rückmeldung sind Fehler deshalb nicht mehr in der App
nachvollziehbar. Auch andere Einstiegspfade sollen nicht jeweils eigene
Diagnoselösungen pflegen.

## Abgrenzung zu bestehenden Issues

- Issue 023 definiert Inhalt, Sortierung und flüchtige Anzeige der
  Aktionsdetails.
- Issue 034 behandelt ausschließlich Öffnen, Minimieren und Layout des
  Aktionsdetails-Panels.
- Issue 071 und 079 behandeln App Shortcuts für Geräte und Schaltgruppen.
- Issue 072 behandelt Validierung und Ausführung externer Intents.
- Issue 081 behandelt Homescreen-Widgets und deren unmittelbare Rückmeldung.
- Issue 082 zentralisiert nur die Erfassung und Bereitstellung gemeinsamer
  Aktionsdetails für diese vorhandenen Einstiegspfade.

## Scope

### Gemeinsame Erfassungsschicht

- Eine kleine, gemeinsam nutzbare Komponente für flüchtige Aktionsdetails
  bereitstellen.
- Bestehende Diagnoseereignisse aus `DeviceActionService` und
  `SwitchGroupActionService` weiterverwenden.
- Dashboard-/ViewModel-spezifische Ereignisverwaltung so weit entkoppeln, dass
  auch Hintergrund- und externe Einstiegspfade Ereignisse sicher hinzufügen
  können.
- Bestehende Sortierung, Zeitstempel, Trenner und Löschfunktion erhalten.
- Gleichzeitige oder kurz aufeinanderfolgende Aktionen eindeutig und
  threadsicher voneinander trennen.

### Unterstützte Einstiegspfade

- Geräteaktion aus dem Dashboard.
- Schaltgruppenaktion aus dem Dashboard.
- Geräte- und Schaltgruppenaktion aus Homescreen-Widgets.
- Geräte- und Schaltgruppenaktion aus App Shortcuts.
- Geräte- und Schaltgruppenaktion aus externen Intents, soweit der jeweilige
  Einstiegspfad bereits unterstützt wird.

### Herkunft und Ergebnis

- Ein Aktionsblock soll seine Herkunft verständlich kennzeichnen, zum Beispiel
  Dashboard, Widget, App Shortcut oder externer Intent.
- Start, relevante Zwischenschritte und Abschlussstatus sollen nachvollziehbar
  sein.
- Fehler vor dem eigentlichen Service-Aufruf, etwa ein nicht mehr vorhandenes
  Ziel, sollen ebenfalls einen verständlichen Eintrag erzeugen.
- Gelöschte oder ungültige Ziele dürfen keine sensiblen oder veralteten
  Konfigurationsdaten in den Aktionsdetails offenlegen.

### Lebensdauer

- Aktionsdetails bleiben entsprechend Issue 023 ausschließlich im
  Arbeitsspeicher.
- Kein persistentes Protokoll über Prozess- oder App-Neustarts hinweg.
- Das manuelle Leeren entfernt weiterhin alle aktuell gehaltenen Einträge.

### Texte

- Herkunfts-, Status- und Fehlermeldungen in Default, Deutsch und Englisch
  konsistent pflegen.
- Hilfe-, Info- und Tooltip-Texte prüfen und an das gemeinsame Verhalten
  anpassen.

## Architekturhinweise

- Die gemeinsame Komponente soll über Koin dort bereitgestellt werden, wo
  mehrere Einstiegspfade dieselbe Instanz benötigen.
- Coroutines und Flow für threadsichere Beobachtung und Aktualisierung
  verwenden.
- Widget-, Shortcut- und Intent-Code bleiben dünne Einstiegs- und
  Delegationsschichten.
- Keine Netzwerk-, WLAN-, HTTP- oder RPC-Logik in die Diagnosekomponente
  verschieben.
- Bestehende Action-Services nicht duplizieren und keine parallelen
  Diagnosemodelle pro Einstiegspfad einführen.
- Vor der Implementierung prüfen, ob bestehende Diagnosemodelle direkt
  wiederverwendet oder über eine kleine gemeinsame Darstellung abgebildet
  werden sollen.

## Datenschutz und Sicherheit

Aktionsdetails dürfen insbesondere nicht enthalten:

- Passwörter, Tokens, API-Keys oder Authorization-Header
- vollständige Request-Bodies
- reale SSIDs
- vollständige lokale IP-Adressen, private Domains oder sensible Hostnamen
- PendingIntent-Extras oder andere interne Transportdaten

Vorhandene datenschutzfreundliche Aufbereitung von Diagnosewerten bleibt
verbindlich. Es werden keine zusätzlichen Android-Berechtigungen benötigt.

## Import und Export

Die Aktionsdetails und ihre gemeinsame Erfassung werden nicht in
Konfigurationsimport/-export aufgenommen. Es handelt sich um flüchtige
Laufzeitdiagnose ohne übertragbaren Konfigurationswert.

## Nicht im Scope

- Persistente Aktionshistorie oder Datenbanktabelle
- Export oder Teilen von Diagnoseprotokollen
- Cloud-Synchronisation, Telemetrie, Analytics oder Crash-Reporting
- Neue Geräte-, Gruppen-, Widget-, Shortcut- oder Intent-Funktionen
- Neue WLAN-, HTTP- oder RPC-Logik
- Änderung der Aufbewahrungsdauer über den aktuellen Prozess hinaus
- Neugestaltung des Aktionsdetails-Panels

## Akzeptanzkriterien

- [ ] Alle unterstützten Einstiegspfade schreiben in denselben Detail-Store.
- [ ] Dashboard-Geräteaktionen erscheinen weiterhin vollständig in den Aktionsdetails.
- [ ] Dashboard-Schaltgruppenaktionen erscheinen weiterhin vollständig in den Aktionsdetails.
- [ ] Widget-Geräteaktionen erscheinen in den Aktionsdetails.
- [ ] Widget-Schaltgruppenaktionen erscheinen in den Aktionsdetails.
- [ ] App-Shortcut-Aktionen erscheinen in den Aktionsdetails.
- [ ] Unterstützte externe Intent-Aktionen erscheinen in den Aktionsdetails.
- [ ] Die Herkunft eines Aktionsblocks ist verständlich erkennbar.
- [ ] Start, Erfolg und Fehler werden pro Aktion nachvollziehbar getrennt.
- [ ] Gleichzeitige Aktionen beschädigen oder vermischen keine Einträge.
- [ ] Das manuelle Leeren funktioniert weiterhin für alle Einträge.
- [ ] Aktionsdetails bleiben ausschließlich im Arbeitsspeicher.
- [ ] Die gemeinsame Erfassung verändert keine Geräteaktionslogik.
- [ ] Keine sensiblen Daten werden angezeigt, gespeichert oder geloggt.
- [ ] Keine neuen Berechtigungen werden eingeführt.
- [ ] Aktionsdetails werden nicht in Konfigurationsimport/-export aufgenommen.
- [ ] Default-, deutsche und englische Texte sind konsistent gepflegt.
- [ ] Hilfe-, Info- und Tooltip-Texte sind geprüft und bei Bedarf aktualisiert.

## Testhinweise

- Dieselbe Geräteaktion nacheinander über Dashboard, Widget, App Shortcut und
  externen Intent starten und Herkunft sowie Ereignisfolge vergleichen.
- Eine Schaltgruppe über alle vorhandenen Einstiegspfade starten.
- Erfolgreiche Aktion, WLAN-Fehler, HTTP-Fehler und ungültiges oder gelöschtes
  Ziel prüfen.
- Zwei Aktionen schnell nacheinander beziehungsweise gleichzeitig starten und
  getrennte Aktionsblöcke prüfen.
- Aktionsdetails während und nach einer Widget-Aktion öffnen.
- Sortierung „Neueste oben“ und „Neueste unten“ prüfen.
- Aktionsdetails leeren und erneute Aktion starten.
- App-Prozess beenden und prüfen, dass flüchtige Einträge nicht wiederhergestellt
  werden.
- Deutsche und englische Darstellung prüfen.
- Diagnoseausgaben gezielt auf sensible Daten kontrollieren.
