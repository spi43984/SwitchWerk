# Issue #31: Import Enforces Unique WiFi Profile Names

## Metadaten

- Status: Abgeschlossen
- Priorität: P2
- Typ: Import / WLAN-Verwaltung

## Ziel

Der Konfigurationsimport darf keine doppelten WLAN-Profilnamen erzeugen.

WLAN-Profilnamen und SSIDs sollen beim Import konsistent behandelt werden,
damit importierte Daten denselben Regeln folgen wie manuell gespeicherte
Profile.

## Problem

Beim manuellen Speichern werden Profilname und SSID getrimmt und der
Profilname wird gegen bestehende lokale Profile geprüft.

Beim Import werden WLAN-Profile direkt in die Datenbank geschrieben. Dadurch
kann ein Merge-Import einen Profilnamen speichern, der lokal bereits existiert,
solange der Name innerhalb der Importdatei eindeutig ist.

Außerdem können importierte Profilnamen oder SSIDs führende oder nachfolgende
Leerzeichen behalten.

Geräte-WLAN-Zuordnungen erfolgen technisch über die WLAN-Profil-ID, nicht über
den Profilnamen. Beim Merge-Import bedeutet eine gleiche WLAN-Profil-ID, dass
das vorhandene lokale Profil überschrieben beziehungsweise aktualisiert wird,
auch wenn der Profilname abweicht. Ein gleicher Profilname mit anderer
WLAN-Profil-ID wird abgelehnt.

## Scope

- Importierte WLAN-Profilnamen und SSIDs vor dem Speichern trimmen.
- Merge-Import gegen bestehende lokale WLAN-Profilnamen prüfen.
- Doppelte Profilnamen case-insensitive und trim-basiert erkennen.
- Verständliche Fehlermeldung anzeigen, wenn ein Import einen lokalen
  Profilnamenkonflikt erzeugen würde.
- Hilfe- und Info-Texte erklären, dass Geräte-Zuordnungen über Profil-IDs
  erfolgen und gleiche IDs beim Merge überschrieben werden.
- Replace-Import weiterhin nur gegen die importierten Daten selbst validieren.
- Bestehende Import-/Export-Struktur nur soweit nötig anpassen.
- Unit-Tests für Merge-Konflikte und Trimming ergänzen.

## Nicht im Scope

- Automatisches Umbenennen importierter Profile.
- Änderung der Passwortverschlüsselung.
- Änderung der WLAN-Verbindungslogik.
- Änderung des JSON-Dateiformats, falls nicht zwingend erforderlich.
- GitHub-Issue oder Pull Request ohne ausdrückliche Freigabe.

## Akzeptanzkriterien

- [x] Ein Merge-Import wird abgelehnt, wenn ein importierter WLAN-Profilname
      bereits lokal für ein anderes Profil existiert.
- [x] Der Vergleich der Profilnamen ignoriert Groß-/Kleinschreibung.
- [x] Der Vergleich der Profilnamen ignoriert führende und nachfolgende
      Leerzeichen.
- [x] Importierte Profilnamen werden vor dem Speichern getrimmt.
- [x] Importierte SSIDs werden vor dem Speichern getrimmt.
- [x] Ein Replace-Import mit intern eindeutigen Profilnamen bleibt möglich.
- [x] Die Fehlermeldung erklärt den Profilnamenkonflikt verständlich.
- [x] Hilfe- und Info-Texte erklären die Zuordnung über WLAN-Profil-IDs.
- [x] Unit-Tests decken Merge-Konflikte gegen lokale Profile ab.
- [x] Unit-Tests decken Trimming von Profilname und SSID ab.

## Abschluss

- Implementiert auf Branch `import-unique-wifi-profile-names`.
- Importfehler beim Laden werden im Importdialog angezeigt.
- Merge-Import prüft WLAN-Profilnamen gegen lokale Profile
  case-insensitive und trim-basiert.
- Importierte WLAN-Profilnamen und SSIDs werden vor dem Speichern getrimmt.
- Hilfe-, Info- und Projektdokumentation erklären die Zuordnung über
  WLAN-Profil-IDs.
- Container-Prüfungen erfolgreich:
  - `./gradlew testDebugUnitTest --tests 'de.piecha.switchwerk.viewmodel.SettingsViewModelTest' --tests 'de.piecha.switchwerk.data.transfer.ConfigurationImportValidatorTest' --tests 'de.piecha.switchwerk.data.repository.ConfigurationImportNormalizationTest'`
  - `./gradlew clean assembleDebug`
- Host-Prüfung: `./gradlew clean assembleDebug` wurde vom Benutzer als
  erfolgreich gemeldet. `installDebug` muss nach synchronem Host-Dateistand
  erneut ausgeführt werden.

## Testhinweise

- Lokales Profil "Garage" anlegen und Datei mit anderem Profil gleicher
  Namensschreibweise importieren.
- Datei mit Profilname " garage " gegen lokales Profil "Garage" per Merge
  importieren.
- Datei mit Profilname und SSID mit Leerzeichen importieren und gespeicherte
  Werte prüfen.
- Replace-Import mit eindeutigen Profilnamen prüfen.
