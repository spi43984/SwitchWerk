# Issue 077: Import/Export fehlender App-Einstellungen

## Metadaten

- Status: Offen
- Priorität: P0
- Typ: Bugfix / Konfiguration / Sicherheit
- Bereich: Import/Export / AppSettings / Übersetzungen

## Ziel

Der Konfigurationsexport und -import soll alle übertragbaren App-Einstellungen
vollständig abbilden. Aktuell fehlen Sprache, Sortierung der WLAN-Profile und
die globale Freigabe externer Intents.

## Hintergrund

Das Exportmodell `ConfigurationAppSettings` enthält derzeit nur Theme,
Aktionsdetails, Detailbereichshöhe, Sortierung der Aktionsdetails und
Dashboard-Layout. Folgende persistierte Einstellungen aus `AppSettings` werden
weder exportiert noch importiert:

- Sprache
- Sortierkriterium der WLAN-Profile
- Sortierrichtung der WLAN-Profile
- Freigabe externer Intents

Dadurch stellt eine exportierte Konfiguration den sichtbaren und funktionalen
App-Zustand nicht vollständig wieder her. Der Zustand des
Einrichtungs-Assistenten bleibt weiterhin bewusst lokal und ist nicht Teil
dieses Issues.

## Scope

### Austauschformat

- `ConfigurationAppSettings` um folgende Werte erweitern:
  - Sprache
  - WLAN-Profil-Sortierkriterium
  - WLAN-Profil-Sortierrichtung
  - Freigabe externer Intents
- Konfigurationsschema kontrolliert erhöhen.
- JSON-Codec für Export und Import ergänzen.
- Ausschließlich bekannte Enum-Werte akzeptieren.
- Ungültige Werte verständlich und ohne Teilimport ablehnen.

### Export

- Aktuelle Sprache exportieren.
- Aktuelles Sortierkriterium und aktuelle Sortierrichtung der WLAN-Profile
  exportieren.
- Aktuellen Zustand der globalen Intent-Freigabe exportieren.
- Keine Zugangsdaten oder weiteren sensiblen Werte ergänzen.

### Import

- Sprache über das bestehende `AppSettingsRepository` übernehmen.
- WLAN-Profil-Sortierkriterium und -richtung gemeinsam übernehmen.
- Freigabe externer Intents über das bestehende `AppSettingsRepository`
  übernehmen.
- In der Import-Zusammenfassung klar anzeigen, wenn externe Intents durch den
  Import aktiviert oder deaktiviert werden.
- Eine Aktivierung externer Intents darf nicht unbemerkt erfolgen; sie wird erst
  mit der bestehenden Importbestätigung übernommen.
- Merge- und Replace-Import verwenden für diese App-Einstellungen dasselbe
  nachvollziehbare Verhalten.

### Rückwärtskompatibilität

- Bestehende Konfigurationen älterer Schema-Versionen bleiben importierbar.
- Fehlen die neuen Felder in einer älteren Konfiguration, bleiben die aktuellen
  lokalen Werte unverändert.
- Der Zustand `showSetupWizardOnStart` bleibt bewusst lokal und wird weiterhin
  weder exportiert noch importiert.

### UI und Dokumentation

- Import-Zusammenfassung und Sicherheitsinformation zur Intent-Freigabe auf
  Deutsch und Englisch konsistent ergänzen.
- Hilfe- und Info-Texte prüfen und nur dort ändern, wo sie den Umfang des
  Konfigurationstransfers beschreiben.
- Beispiele verwenden ausschließlich neutrale Platzhalter.

## Nicht im Scope

- Export oder Import des Einrichtungs-Assistenten-Zustands
- Änderung der Passwort-Import-/Exportregeln
- Änderung der Geräte-, WLAN- oder Aktionslogik
- Cloud-Synchronisation oder Account-System
- Freie URL-, RPC- oder Befehlsausführung über Intents

## Architekturhinweise

- Bestehende `ConfigurationDocument`-, JSON-Codec- und
  `AppSettingsRepository`-Struktur weiterverwenden.
- Keine parallele Einstellungsablage einführen.
- Validierung vor dem Speichern vollständig abschließen, damit ein ungültiger
  Einstellungswert keinen Teilimport verursacht.
- Sicherheitskritische Defaults und ältere Konfigurationen explizit testen.

## Akzeptanzkriterien

- [ ] Sprache wird exportiert und importiert.
- [ ] WLAN-Profil-Sortierkriterium wird exportiert und importiert.
- [ ] WLAN-Profil-Sortierrichtung wird exportiert und importiert.
- [ ] Freigabe externer Intents wird exportiert und importiert.
- [ ] Die Import-Zusammenfassung weist auf eine Änderung der Intent-Freigabe
      hin.
- [ ] Externe Intents werden erst nach bestätigtem Import aktiviert.
- [ ] Ungültige Einstellungswerte werden vor dem Speichern sicher abgelehnt.
- [ ] Ältere Konfigurationen ohne die neuen Felder bleiben importierbar und
      verändern die zugehörigen lokalen Einstellungen nicht.
- [ ] Der Zustand des Einrichtungs-Assistenten bleibt unverändert lokal.
- [ ] Merge- und Replace-Import verhalten sich für die neuen Einstellungen
      konsistent.
- [ ] Deutsche und englische Hilfe-, Info- und Zusammenfassungstexte sind
      konsistent.
- [ ] Unit-Tests decken Export, Import, Rückwärtskompatibilität, ungültige Werte
      und die Intent-Sicherheitsanzeige ab.

## Testhinweise

- Sprache auf Englisch, WLAN-Sortierung auf SSID absteigend und externe Intents
  aktiviert exportieren; auf einer Installation mit abweichenden lokalen
  Werten importieren und alle drei Einstellungen prüfen.
- Dieselbe Prüfung mit deaktivierten externen Intents durchführen.
- Import-Zusammenfassung vor der Übernahme einer aktivierten Intent-Freigabe
  prüfen und Import abbrechen.
- Ältere Konfiguration ohne neue Felder per Merge und Replace importieren.
- Konfigurationen mit unbekannter Sprache, unbekanntem Sortierkriterium oder
  unbekannter Sortierrichtung sicher ablehnen.
- Prüfen, dass keine Passwörter, Tokens, vollständigen URLs oder Befehle durch
  die neuen Felder ergänzt oder geloggt werden.

## Überschneidungsprüfung

- Issue 012 definiert die allgemeine Import-/Export-Grundlage, enthält diese
  später ergänzten App-Einstellungen aber nicht.
- Issue 040 behandelt ausschließlich Passwortdaten und bleibt unverändert.
- Issue 059 behandelt sicherheitskritische One-Shot-Schalter; die persistente
  globale Intent-Freigabe ist kein One-Shot-Schalter.
- Issue 061 legt fest, dass der Wizard-State lokal bleibt. Diese Entscheidung
  wird ausdrücklich beibehalten.
