# AI Handoff

Stand: 2. Juli 2026

## Startvorlage

Für die nächste Session zuerst `AI_SESSION_PROMPT.md` verwenden und danach
`AGENTS.md` sowie diesen Handoff beachten.

## Aktueller Stand

Abgeschlossen:

- Issue 067 „Info, Hilfe und Über-Dialog Buttons“
- GitHub-Issue: #160
- Branch: `info-help-about-dialog-buttons`
- Info-Dialoge zeigen unten rechts nur noch `Schließen` beziehungsweise
  `Close`.
- `StandardDialogButtons` unterstützt optional eine fehlende linke Aktion und
  einen kompakten rechten Button.
- Hilfe zeigt unten links `Einrichtungs-Assistent erneut zeigen` und rechts
  `Schließen`.
- Über SwitchWerk zeigt unten links `GitHub-Projekt öffnen` und rechts
  `Schließen`.
- Hilfe-/Über-Fachaktionen wurden aus dem Inhaltsbereich in die untere
  Aktionsleiste verschoben.
- Deutsche und englische Strings für die betroffenen Buttons sind konsistent.
- Container-Prüfungen: `./gradlew lintDebug`, `./gradlew testDebugUnitTest`
- Host-Prüfungen laut Benutzerrückmeldung erfolgreich:
  `./gradlew clean assembleDebug`, `./gradlew installDebug`
- Nächstes priorisiertes Thema: Issue 024 „Authenticated Import Sources
  Backlog“; aktuell Status `Backlog`, daher keine aktive Implementierung.

Ohne ausdrückliche Anweisung nicht committen, pushen, PR erstellen, mergen,
GitHub-Issue schließen oder Branch löschen.
