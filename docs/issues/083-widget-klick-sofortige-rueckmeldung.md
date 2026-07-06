# Issue 083: Widget-Klick sofort sichtbar machen

## Metadaten

- Status: Abgeschlossen
- Priorität: P0
- Typ: Bug / UX / Android Integration
- Bereich: Android Homescreen Widgets / Aktionsausführung / Feedback

## Ziel

Homescreen-Widgets sollen unmittelbar sichtbar auf einen Klick reagieren, damit Anwender erkennen, dass die Aktion angenommen wurde und gestartet wird.

Die erste sichtbare Rückmeldung darf nicht davon abhängen, ob im Hintergrund bereits WLAN-, Proximity-, Geräte- oder Gruppenlogik läuft.

## Hintergrund

Mit Issue 081 wurden Homescreen-Widgets eingeführt. Die Widgets funktionieren grundsätzlich.

Beim Antippen einer Widget-Schaltfläche kann es jedoch mehrere Sekunden dauern, bis eine sichtbare Reaktion auf dem Widget erscheint. Für Anwender ist dadurch unklar, ob der Klick angenommen wurde, ob bereits eine WLAN-Verbindung versucht wird oder ob die Aktion noch nicht gestartet wurde.

Die Ursache ist voraussichtlich, dass der laufende Widget-Status zwar gesetzt wird, das anschließende Widget-Rendering aber einen vollständigen Status-/Proximity-Refresh ausführt. Dieser darf die erste sichtbare Rückmeldung nicht verzögern.

## Scope

### Sofortige Klick-Rückmeldung

- Beim Antippen einer Widget-Aktion sofort einen sichtbaren Zwischenzustand anzeigen.
- Die sichtbare Rückmeldung soll möglichst unmittelbar nach dem Klick erscheinen.
- Die erste Rückmeldung darf nicht auf WLAN-Scan, WLAN-Verbindungsaufbau, HTTP/RPC-Ausführung oder Gruppenverarbeitung warten.
- Der laufende Zustand soll verständlich sein, z. B. durch:
  - geänderte Button-Darstellung,
  - laufenden Status-Text,
  - Spinner oder Progress-Indikator, falls mit RemoteViews praktikabel,
  - oder eine andere einfache native Widget-Darstellung.
- Während eine Aktion läuft, soll dieselbe Widget-Aktion nicht mehrfach parallel gestartet werden.

### Rendering

- Für den Startzustand eine schnelle Widget-Aktualisierung bereitstellen, die keinen vollständigen WLAN-Proximity-Refresh erzwingt.
- Bestehende Erfolg- und Fehler-Rückmeldung nach Abschluss erhalten.
- Bestehende Widget-Farben für WLAN-Nähe, Erfolg und Fehler nicht unnötig ändern.
- Nach Ablauf der bestehenden Feedbackdauer wieder in den normalen Widget-Zustand zurückkehren.

### Ausführung

- Die eigentliche Geräte- und Gruppenaktionslogik bleibt unverändert.
- Widget-Aktionen delegieren weiterhin ausschließlich an `DeviceActionService` und `SwitchGroupActionService`.
- Keine neue WLAN-, HTTP-, RPC- oder Gruppen-Sonderlogik im Widget-Code einführen.
- Fehler vor dem eigentlichen Service-Aufruf weiterhin sicher behandeln.

### Texte

- Falls neue sichtbare Texte nötig sind, Default-, deutsche und englische String-Ressourcen konsistent pflegen.
- Hilfe-, Info- und Tooltip-Texte prüfen und nur bei Bedarf anpassen.

## Abgrenzung zu bestehenden Issues

- Issue 081 hat Homescreen-Widgets eingeführt und bleibt abgeschlossen.
- Issue 082 behandelt gemeinsame Aktionsdetails für alle Einstiegspfade.
- Dieses Issue behandelt ausschließlich die unmittelbare visuelle Reaktion im Widget nach dem Antippen.
- Dieses Issue soll keine gemeinsame Aktionshistorie und keinen Detail-Store einführen.

## Architekturhinweise

- Bestehende Architektur beibehalten.
- Android-AppWidget/RemoteViews bleiben reine Einstiegsschicht und Darstellung.
- Keine Netzwerk-, WLAN-, HTTP- oder RPC-Logik direkt in Widget-Rendering oder Widget-UI verschieben.
- Für das Sofort-Feedback bevorzugt eine kleine, gezielte Render-Variante oder einen Render-Parameter verwenden, der den aktuellen gespeicherten Widget-Status nutzt, aber keinen frischen WLAN-Proximity-Scan startet.
- Statusänderungen müssen threadsicher genug bleiben, damit schnelle Mehrfachklicks keine widersprüchlichen Zustände erzeugen.
- Keine sensiblen Daten in Widget-Labels, PendingIntents, Extras, Logs oder Dokumentation speichern.

## Datenschutz und Sicherheit

Es werden keine neuen Android-Berechtigungen benötigt.

Widget-PendingIntents dürfen weiterhin nur Widget-ID und Eintragsindex enthalten. Es dürfen keine Ziel-IDs, URLs, Befehle, Hosts, SSIDs, Passwörter, Tokens oder sonstige sensiblen Daten in PendingIntents, Logs oder sichtbaren Texten ergänzt werden.

## Import und Export

Keine Änderung an Konfigurationsimport oder -export.

Das Verhalten betrifft nur flüchtigen Laufzeitstatus eines lokal vorhandenen Android-Homescreen-Widgets.

## Nicht im Scope

- Keine neue Widget-Konfiguration
- Keine neuen Widget-Größen
- Keine neue freie Widget-Positionierung
- Keine Änderungen an Dashboard, App Shortcuts oder externen Intents
- Keine gemeinsame Aktionsdetails-Implementierung
- Keine persistente Aktionshistorie
- Keine neue WLAN-, HTTP-, RPC- oder Gruppenlogik
- Keine Cloud-, Tracking-, Analytics- oder Account-Funktion

## Akzeptanzkriterien

- [x] Nach Antippen einer Widget-Aktion erscheint ohne spürbare Verzögerung eine sichtbare Rückmeldung.
- [x] Die erste Widget-Rückmeldung wartet nicht auf WLAN-Scan, WLAN-Verbindung, HTTP/RPC-Aufruf oder Gruppenverarbeitung.
- [x] Anwender erkennen eindeutig, dass der Klick angenommen wurde.
- [x] Laufende Widget-Aktionen werden verständlich dargestellt.
- [x] Dieselbe Widget-Aktion kann während laufender Ausführung nicht mehrfach parallel gestartet werden.
- [x] Erfolg und Fehler werden nach Abschluss weiterhin sichtbar dargestellt.
- [x] Die bestehende Rücksetzung nach kurzer Feedbackdauer bleibt erhalten.
- [x] Geräteaktionen verwenden weiterhin `DeviceActionService`.
- [x] Schaltgruppenaktionen verwenden weiterhin `SwitchGroupActionService`.
- [x] Es wird keine neue Netzwerk-, WLAN-, HTTP- oder RPC-Logik im Widget-Code eingeführt.
- [x] Keine neuen Android-Berechtigungen werden eingeführt.
- [x] Keine sensiblen Daten werden angezeigt, gespeichert oder geloggt.
- [x] Default-, deutsche und englische Texte sind bei Bedarf konsistent gepflegt.
- [x] Hilfe-, Info- und Tooltip-Texte sind geprüft und bei Bedarf aktualisiert.

## Abschluss

- Der laufende Zustand wird vor Proximity-, Geräte- und Gruppenverarbeitung ohne WLAN-Refresh gerendert.
- Eine atomare Statusübernahme verhindert parallele Wiederholung derselben laufenden Widget-Aktion.
- Geräte- und Gruppenaktionen verwenden weiterhin die bestehenden Action-Services.
- Sofortige sichtbare Rückmeldung wurde auf dem Gerät erfolgreich bestätigt.
- Widget-Hilfe sowie Default-, deutsche und englische Texte wurden konsistent aktualisiert.

## Testhinweise

- Widget mit einzelner Geräteaktion antippen und prüfen, dass sofort eine sichtbare Reaktion erscheint.
- Widget mit einzelner Schaltgruppenaktion antippen und prüfen, dass sofort eine sichtbare Reaktion erscheint.
- Während einer laufenden Widget-Aktion mehrfach tippen und prüfen, dass keine parallelen Mehrfachausführungen entstehen.
- Verhalten bei erfolgreicher Aktion prüfen.
- Verhalten bei nicht erreichbarem Zielgerät prüfen.
- Verhalten bei WLAN aus prüfen.
- Verhalten bei leerer oder nicht mehr ausführbarer Schaltgruppe prüfen.
- Prüfen, dass Erfolg oder Fehler nach der bestehenden Feedbackdauer wieder zurückgesetzt wird.
- Prüfen, dass die erste Rückmeldung auch dann sofort erscheint, wenn ein WLAN-Scan oder Verbindungsaufbau länger dauert.
- Widget-Größen `1x1`, `1x2`, `2x1` und vergrößerte Widgets prüfen.
- Deutsche und englische Darstellung prüfen.
