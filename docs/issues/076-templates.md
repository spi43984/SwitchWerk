# Issue 076: Templates

## Metadaten

- Status: Backlog
- Priorität: P2
- Typ: Feature / UX
- Bereich: Gerätevorlagen / Geräteverwaltung / Einrichtung

## Ziel

SwitchWerk soll Gerätevorlagen anbieten, damit neue Geräte schneller und fehlerärmer eingerichtet werden können.

## Hintergrund

Viele Geräte verwenden wiederkehrende HTTP- oder RPC-Muster. Anwender sollen nicht jedes Mal technische Pfade, Methoden oder JSON-Strukturen selbst zusammensuchen müssen.

Templates senken die Einstiegshürde, ohne die lokale und cloudfreie Architektur zu verändern.

## Scope

### Vorlagen

- Eine kleine initiale Liste vordefinierter Gerätevorlagen bereitstellen.
- Vorlagen für naheliegende lokale HTTP-/RPC-Geräte vorbereiten.
- Pro Vorlage sinnvolle Standardaktionen anbieten.
- Vorlagen dürfen nur neutrale Platzhalter enthalten.

### Einrichtung

- Beim Anlegen eines Geräts optional eine Vorlage auswählen.
- Vorlage befüllt technische Felder wie Gerätetyp, Methode, Pfad, Body oder Content-Type, soweit passend.
- Anwender kann alle übernommenen Werte vor dem Speichern prüfen und ändern.

### Pflege

- Vorlagen lokal in der App halten.
- Keine Online-Vorlagen oder Cloud-Kataloge.
- Vorlagen so strukturieren, dass spätere Erweiterungen möglich bleiben.

### Texte

- Verständliche deutsche und englische Beschreibungen für Vorlagen pflegen.
- Hilfe- oder Info-Texte ergänzen, damit Anwender verstehen, dass Vorlagen nur Startwerte sind.

## Nicht im Scope

- Keine automatische Geräteerkennung.
- Keine Cloud-Vorlagen.
- Kein Download externer Template-Kataloge.
- Keine Ausführung ungespeicherter Template-Befehle ohne Benutzerprüfung.
- Keine Änderung an WLAN-Verbindungslogik.
- Keine herstellerspezifische Speziallogik, die die Architektur unnötig verkompliziert.

## Architekturhinweise

- Templates als lokale, versionierbare App-Daten modellieren.
- Keine sensiblen Daten in Templates speichern.
- Template-Auswahl darf bestehende Geräteaktionslogik nicht duplizieren.
- Sichtbare Texte in Android-String-Ressourcen pflegen.
- Bestehende MVVM- und Compose-Struktur beibehalten.

## Akzeptanzkriterien

- [ ] Beim Anlegen eines Geräts kann optional eine Vorlage ausgewählt werden.
- [ ] Vorlagen befüllen passende technische Felder mit neutralen Standardwerten.
- [ ] Anwender kann alle übernommenen Werte vor dem Speichern prüfen und ändern.
- [ ] Mindestens eine sinnvolle Vorlage für lokale HTTP-/RPC-Geräte ist vorhanden.
- [ ] Es werden keine sensiblen Daten oder realen internen Werte in Vorlagen gespeichert.
- [ ] Vorlagen funktionieren ohne Cloud, Account oder externen Dienst.
- [ ] Bestehende manuelle Geräteeinrichtung bleibt weiterhin möglich.
- [ ] Deutsche und englische Texte sind konsistent gepflegt.
- [ ] Hilfe-, Info- und Tooltip-Texte wurden geprüft und bei Bedarf aktualisiert.

## Testhinweise

- Neues Gerät ohne Vorlage anlegen.
- Neues Gerät mit Vorlage anlegen.
- Übernommene technische Felder prüfen.
- Übernommene Werte vor dem Speichern ändern.
- Gerät speichern und Aktion testen.
- App neu starten und gespeichertes Gerät prüfen.
