# AI Handoff

Stand: 4. Juli 2026

## Aktive Arbeit

- Issue 077 „Import/Export fehlender App-Einstellungen“
- GitHub-Issue: #171
- Branch: `import-export-app-settings`
- Implementierung abgeschlossen, aber noch nicht committed oder veröffentlicht.
- Konfigurationsschema auf Version 6 erhöht.
- Sprache, WLAN-Profil-Sortierung und globale Freigabe externer Intents werden
  exportiert und importiert.
- Ältere Konfigurationen ohne diese Felder behalten die lokalen Werte.
- Ungültige Enum-Werte und unvollständige WLAN-Sortierangaben werden vor dem
  Speichern abgelehnt.
- Änderungen der Intent-Freigabe erscheinen vor der Importbestätigung in der
  Zusammenfassung; die Übernahme erfolgt erst beim bestätigten Import.
- `showSetupWizardOnStart` bleibt lokal und wird nicht übertragen.
- Merge und Replace verwenden für die neuen App-Einstellungen dieselbe Logik.
- Deutsche und englische Hilfe-, Info- und Zusammenfassungstexte wurden auf
  Konsistenz geprüft und an den tatsächlichen Transferumfang angepasst.
- Die englische Fallback-Ressource enthält für die relevanten Import-/Export-
  Statusmeldungen keine deutschen Texte mehr.

## Prüfstand

- Gezielte Unit-Tests: erfolgreich.
- Gesamte Debug-Unit-Test-Suite im Container: erfolgreich.
- `lintDebug` im Container: erfolgreich.
- `git diff --check`: erfolgreich.
- Der Benutzer hat den lokalen Host-/Gerätetest als erfolgreich bestätigt.

## Nächster Schritt

- Auf ausdrückliche Freigabe: Änderungen committen, pushen und Pull Request
  erstellen.
- Issue-Datei und `docs/issues/overview.txt` erst nach Merge gemäß
  Abschluss-Workflow aktualisieren.
- Dauerhafte neue Projektregel: Bei jeder neuen Funktion muss ausdrücklich
  geprüft und der Benutzer gefragt werden, ob zugehörige Einstellungen
  exportiert und importiert werden sollen. Die Regel steht in `AGENTS.md`,
  `ai-context.md` und `AI_SESSION_PROMPT.md`.

Für die nächste Session `AI_SESSION_PROMPT.md` als wiederverwendbare
Startvorlage verwenden.

Ohne ausdrückliche Anweisung nicht committen, pushen, PR erstellen, mergen,
GitHub-Issue schließen oder Branch löschen.
