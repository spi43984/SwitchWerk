# AI Handoff

Stand: 4. Juli 2026

## Abgeschlossen

- Issue 071 „App Shortcuts“
- GitHub-Issue: #166
- Pull Request: #167
- Squash-Merge auf `main`: `65bc40d feat: add dynamic app shortcuts (#167)`
- Dynamische App-Shortcuts sind pro Gerät konfigurierbar, werden zentral aus
  dem Geräte-Repository aktualisiert und führen Aktionen über die bestehende
  MVVM-/DeviceActionService-Logik aus.
- Room-Migration 8 → 9 und Konfigurationsschema 5 speichern
  `shortcutEnabled`; ältere Importe verwenden sicher `false`.
- Deutsche und englische Hilfe-, Info- und UI-Texte sind aktualisiert.
- Debug- und Release-Prüfbefehle sind in den zentralen Entwicklungsdokumenten
  getrennt dokumentiert; beide verwenden `testDebugUnitTest`.
- Container-Prüfungen, GitHub Android Quality Checks und Dependency Submission
  waren erfolgreich. Release-Build, Installation und Gerätetest wurden vom
  Benutzer auf dem Ubuntu-Host bestätigt.

## Nächster Stand

- Nächstes offenes Issue nach `docs/issues/overview.txt`: Issue 072
  „SwitchWerk empfängt Intents“.
- Weitere Reihenfolge und Status ausschließlich aus `docs/issues/overview.txt`
  entnehmen.

Ohne ausdrückliche Anweisung nicht committen, pushen, PR erstellen, mergen,
GitHub-Issue schließen oder Branch löschen.
