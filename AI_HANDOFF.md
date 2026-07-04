# AI Handoff

Stand: 4. Juli 2026

## Abgeschlossen

- Issue 077 „Import/Export fehlender App-Einstellungen“
- GitHub-Issue: #171
- Pull Request: #172
- Squash-Merge auf `main`: `21512a5`
- Sprache, WLAN-Profil-Sortierung und globale Freigabe externer Intents sind
  Bestandteil des Konfigurationsexports und -imports.
- Ältere Konfigurationen ohne diese Felder behalten die lokalen Werte;
  `showSetupWizardOnStart` bleibt ausschließlich lokal.
- Ungültige Enum-Werte und unvollständige WLAN-Sortierangaben werden vor dem
  Speichern abgelehnt.
- Änderungen der Intent-Freigabe werden vor der Importbestätigung angezeigt.
- Deutsche und englische Hilfe-, Info- und Zusammenfassungstexte sind
  konsistent aktualisiert.
- Unit-Tests, `lintDebug`, GitHub-Prüfungen und der vom Benutzer bestätigte
  Host-/Gerätetest waren erfolgreich.
- Dauerhafte Projektregel ergänzt: Bei jeder neuen Funktion muss der Benutzer
  ausdrücklich gefragt werden, ob zugehörige Einstellungen exportiert und
  importiert werden sollen. Die Entscheidung ist im Issue-Scope zu
  dokumentieren.

## Nächster Stand

- Nächstes offenes Issue nach `docs/issues/overview.txt`: Issue 074 „Gruppen“.
- Weitere Reihenfolge und Status ausschließlich aus
  `docs/issues/overview.txt` entnehmen.

Für die nächste Session `AI_SESSION_PROMPT.md` als wiederverwendbare
Startvorlage verwenden.

Ohne ausdrückliche Anweisung nicht committen, pushen, PR erstellen, mergen,
GitHub-Issue schließen oder Branch löschen.
