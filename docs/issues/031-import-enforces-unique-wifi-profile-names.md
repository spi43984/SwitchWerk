# Issue #31: Import Enforces Unique WiFi Profile Names

## Metadaten

- Status: Offen
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

## Scope

- Importierte WLAN-Profilnamen und SSIDs vor dem Speichern trimmen.
- Merge-Import gegen bestehende lokale WLAN-Profilnamen prüfen.
- Doppelte Profilnamen case-insensitive und trim-basiert erkennen.
- Verständliche Fehlermeldung anzeigen, wenn ein Import einen lokalen
  Profilnamenkonflikt erzeugen würde.
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

- [ ] Ein Merge-Import wird abgelehnt, wenn ein importierter WLAN-Profilname
      bereits lokal für ein anderes Profil existiert.
- [ ] Der Vergleich der Profilnamen ignoriert Groß-/Kleinschreibung.
- [ ] Der Vergleich der Profilnamen ignoriert führende und nachfolgende
      Leerzeichen.
- [ ] Importierte Profilnamen werden vor dem Speichern getrimmt.
- [ ] Importierte SSIDs werden vor dem Speichern getrimmt.
- [ ] Ein Replace-Import mit intern eindeutigen Profilnamen bleibt möglich.
- [ ] Die Fehlermeldung erklärt den Profilnamenkonflikt verständlich.
- [ ] Unit-Tests decken Merge-Konflikte gegen lokale Profile ab.
- [ ] Unit-Tests decken Trimming von Profilname und SSID ab.

## Testhinweise

- Lokales Profil "Garage" anlegen und Datei mit anderem Profil gleicher
  Namensschreibweise importieren.
- Datei mit Profilname " garage " gegen lokales Profil "Garage" per Merge
  importieren.
- Datei mit Profilname und SSID mit Leerzeichen importieren und gespeicherte
  Werte prüfen.
- Replace-Import mit eindeutigen Profilnamen prüfen.
