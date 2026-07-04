# Issue 073: Farbige Geräte

## Metadaten

- Status: Offen
- Priorität: P2
- Typ: UX / UI
- Bereich: Geräteverwaltung / Dashboard / Konfiguration

## Ziel

Geräte sollen optional eine Farbe erhalten, damit Anwender Geräte im Dashboard und in der Geräteverwaltung schneller wiedererkennen können.

## Hintergrund

Bei mehreren Geräten wird die Liste unübersichtlich. Farben helfen, Geräte nach Bedeutung, Bereich oder Art visuell zu unterscheiden, ohne zusätzliche Komplexität einzuführen.

## Scope

### Farbauswahl

- Pro Gerät optional eine Farbe speichern.
- Eine kleine, feste Palette anbieten.
- Standard ist keine besondere Farbe beziehungsweise die bestehende Darstellung.
- Farbauswahl in der Gerätebearbeitung integrieren.

### Darstellung

- Gerätefarbe im Dashboard sichtbar machen.
- Gerätefarbe in der Geräteverwaltung sichtbar machen.
- Darstellung soll in Listen- und Widget-Dashboard funktionieren.
- Farben müssen im hellen und dunklen Theme lesbar bleiben.

### Import und Export

- Gerätefarbe in Konfigurationsexport aufnehmen.
- Import älterer Konfigurationen ohne Farbe muss kompatibel bleiben.
- Merge- und Replace-Import sollen Farben konsistent übernehmen.

### Barrierearmut

- Farbe darf nicht die einzige Information für sicherheitskritische Bedeutung sein.
- Kontraste prüfen.
- Geräte bleiben auch ohne Farberkennung bedienbar.

## Nicht im Scope

- Keine frei wählbaren RGB-Farben.
- Keine farbabhängige Automatisierung.
- Keine Gruppen- oder Tag-Funktion.
- Keine Änderung an Geräteaktionslogik.
- Keine Cloud- oder Account-Funktion.

## Architekturhinweise

- Gerätefarbe als einfache optionale Eigenschaft im bestehenden Gerätemodell ablegen.
- Migration für lokale Speicherung vorsehen, falls das Schema erweitert wird.
- Bestehende Import-/Export-Kompatibilität erhalten.
- UI-State über bestehende ViewModels führen.
- Keine Business-Logik in Composables verschieben.

## Akzeptanzkriterien

- [ ] Geräte können optional eine Farbe aus einer festen Palette erhalten.
- [ ] Ohne Farbauswahl bleibt die bisherige Standarddarstellung erhalten.
- [ ] Farbe ist im Dashboard sichtbar.
- [ ] Farbe ist in der Geräteverwaltung sichtbar.
- [ ] Darstellung funktioniert im hellen und dunklen Theme.
- [ ] Darstellung bleibt bei größerer Android-Schriftgröße bedienbar.
- [ ] Farben werden exportiert und importiert.
- [ ] Ältere Konfigurationen ohne Farbinformation bleiben importierbar.
- [ ] Farbe ist nicht die einzige Information für kritische Bedeutung.
- [ ] Deutsche und englische Texte sind konsistent gepflegt.

## Testhinweise

- Gerät ohne Farbe anzeigen.
- Gerät mit jeder angebotenen Farbe anzeigen.
- Dashboard-Liste und Dashboard-Widgetdarstellung prüfen.
- Helles und dunkles Theme prüfen.
- Größere Android-Schriftgröße prüfen.
- Konfiguration exportieren und erneut importieren.
- Ältere Konfiguration ohne Farbfeld importieren.
