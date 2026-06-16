# Issue #26: Settings UI Rework

## Ziel

Den Einstellungsbildschirm übersichtlicher, wartbarer und besser bedienbar machen.

## Scope

- Einstellungen in fachliche Bereiche gliedern
  - Anzeige
  - WLAN-Profile
  - Geräte
  - Import / Export
- Bereiche optional ein- und ausklappbar darstellen
- Lange Listen besser nutzbar machen
- Scrollaufwand reduzieren
- QR-Code-Import optisch in den Import-/Export-Bereich integrieren
- Vorbereitung für zukünftige Anzeigeeinstellungen
- Vorbereitung für zukünftige Dashboard-Layout-Einstellungen

## Nicht Bestandteil

- Neue Gerätefunktionen
- Änderung der WLAN-Verbindungslogik
- Änderung der Geräteaktionslogik
- Änderung der Importlogik
- Neue Import- oder Exportformate
- Dashboard-Funktionserweiterungen

## Akzeptanzkriterien

- [ ] Einstellungen sind in klar erkennbare Bereiche gegliedert
- [ ] Import/Export inklusive QR-Code-Import ist logisch gruppiert
- [ ] Der Screen wirkt übersichtlicher als die aktuelle Version
- [ ] Bestehende Funktionen bleiben unverändert nutzbar
- [ ] Keine Regressionen bei Geräte- oder WLAN-Verwaltung

## Testhinweise

- Alle bestehenden Einstellungsfunktionen prüfen
- WLAN-Profil anlegen, bearbeiten und löschen
- Gerät anlegen, bearbeiten und löschen
- Import und Export testen
- QR-Code-Import testen
- Bedienbarkeit auf dem Pixel-Gerät prüfen
