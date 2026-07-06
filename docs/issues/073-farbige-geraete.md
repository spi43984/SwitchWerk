# Issue 073: Farbige Geräte

## Metadaten

- Status: Abgeschlossen
- Priorität: P2
- Typ: UX / UI
- Bereich: Geräte- und Gruppenverwaltung / Dashboard / Konfiguration

## Ziel

Geräte und Schaltgruppen sollen optional eine Farbe erhalten, damit Anwender
Einträge im Dashboard und in der Verwaltung schneller wiedererkennen können.

## Hintergrund

Bei mehreren Geräten wird die Liste unübersichtlich. Farben helfen, Geräte nach Bedeutung, Bereich oder Art visuell zu unterscheiden, ohne zusätzliche Komplexität einzuführen.

## Scope

### Farbauswahl

- Pro Gerät und Schaltgruppe optional eine Farbe speichern.
- Eine kleine, feste Palette anbieten.
- Standard ist keine besondere Farbe beziehungsweise die bestehende Darstellung.
- Farbauswahl in der Geräte- und Gruppenbearbeitung integrieren.

### Darstellung

- Farbe als kontrastierende Kachelfarbe im Dashboard sichtbar machen.
- Farbe in der Geräte- und Gruppenverwaltung sichtbar machen.
- Darstellung soll in Listen- und Widget-Dashboard funktionieren.
- Farben müssen im hellen und dunklen Theme lesbar bleiben.

### Import und Export

- Geräte- und Gruppenfarbe in Konfigurationsexport aufnehmen.
- Import älterer Konfigurationen ohne Farbe muss kompatibel bleiben.
- Merge- und Replace-Import sollen Farben konsistent übernehmen.

### Barrierearmut

- Farbe darf nicht die einzige Information für sicherheitskritische Bedeutung sein.
- Kontraste prüfen.
- Geräte bleiben auch ohne Farberkennung bedienbar.

## Nicht im Scope

- Keine frei wählbaren RGB-Farben.
- Keine farbabhängige Automatisierung.
- Keine neue Gruppen- oder Tag-Funktion; bestehende Schaltgruppen erhalten nur
  dieselbe Farbauswahl wie Geräte.
- Keine Änderung an Geräteaktionslogik.
- Keine Cloud- oder Account-Funktion.

## Architekturhinweise

- Farbe als stabile optionale Enum-Eigenschaft in den bestehenden Geräte- und
  Gruppenmodellen ablegen; kein frei gespeicherter RGB-Wert.
- Migration für lokale Speicherung vorsehen, falls das Schema erweitert wird.
- Bestehende Import-/Export-Kompatibilität erhalten.
- UI-State über bestehende ViewModels führen.
- Keine Business-Logik in Composables verschieben.

## Akzeptanzkriterien

- [x] Geräte und Schaltgruppen können optional eine Farbe aus einer festen Palette erhalten.
- [x] Ohne Farbauswahl bleibt die bisherige Standarddarstellung erhalten.
- [x] Farbe ist im Dashboard sichtbar.
- [x] Farbe ist in der Geräte- und Gruppenverwaltung sichtbar.
- [x] Darstellung funktioniert im hellen und dunklen Theme.
- [x] Darstellung bleibt bei größerer Android-Schriftgröße bedienbar.
- [x] Farben werden exportiert und importiert.
- [x] Ältere Konfigurationen ohne Farbinformation bleiben importierbar.
- [x] Farbe ist nicht die einzige Information für kritische Bedeutung.
- [x] Default-, deutsche und englische Texte sind konsistent gepflegt.

## Umsetzung

- Feste Palette mit stabilem `DeviceColor`-Schlüssel; `NONE` ist der Standard.
- Room-Version 14 mit Migrationen für Geräte- und Gruppenfarben.
- Konfigurationsschema 12 mit rückwärtskompatiblen optionalen Farbfeldern.
- Merge- und Replace-Import übernehmen Geräte- und Gruppenfarben konsistent.
- Dashboard-Kacheln und Verwaltungszeilen verwenden die gewählte Farbe als
  Hintergrund sowie eine kontrastierende schwarze oder weiße Inhaltsfarbe.
- Der WLAN-Statuspunkt behält seine eigenständige Bedeutung und erhält auf
  Farbkacheln einen kontrastierenden Rahmen.
- Ein beim Gerätetest sichtbarer, unabhängiger WLAN-Scan-Absturz durch doppelte
  Broadcasts wurde mit einer atomaren Abschluss-Sperre behoben und per ADB
  erneut geprüft.

## Testhinweise

- Gerät und Schaltgruppe ohne Farbe anzeigen.
- Gerät und Schaltgruppe mit jeder angebotenen Farbe anzeigen.
- Dashboard-Liste und Dashboard-Widgetdarstellung prüfen.
- Helles und dunkles Theme prüfen.
- Größere Android-Schriftgröße prüfen.
- Konfiguration mit Geräte- und Gruppenfarben exportieren und erneut importieren.
- Ältere Konfiguration ohne Farbfeld importieren.
