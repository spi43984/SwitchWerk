# AI Handoff

Stand: 26. Juni 2026

## Aktueller Stand

Zuletzt lokal abgeschlossen:

- Issue 060 „Scrollbares Hamburger-Menü bei Platzmangel“
- Branch: `scrollbares-hamburger-menue`
- GitHub-Issue: #136
- Der Inhalt des Hamburger-/Overflow-Menüs ist bei Platzmangel vertikal
  scrollbar, ohne Menüeinträge, Texte, Icons, Navigation oder Zielseiten
  funktional zu ändern.
- Close-Button, Einstellungen, Hilfe, Über SwitchWerk, Version, Release-Datum
  und Icon bleiben auch in Landscape, Portrait, auf kleinen Displays und bei
  großer Android-Schriftgröße erreichbar.
- Das Icon im unteren Menü-Info-Bereich und das Icon auf der Über-SwitchWerk-
  Ansicht werden im Landscape-Modus nicht breiter als die jeweilige
  Portrait-Basis dargestellt.
- Container-Prüfungen erfolgreich:
  - `./gradlew lintDebug`
  - `./gradlew testDebugUnitTest`
- Host-Build, Installation und manuelle UI-Prüfung wurden vom Benutzer
  bestätigt:
  - `./gradlew clean assembleDebug`
  - `./gradlew installDebug`
- `docs/issues/060-scrollbares-hamburger-menue.md` und
  `docs/issues/overview.txt` sind lokal auf `abgeschlossen` gesetzt.
- Nächstes offenes Issue nach `docs/issues/overview.txt`: Issue 061
  „Import Replace Preserves Wizard State“.

Zuletzt abgeschlossen:

- Issue 022 „Request Body And Content-Type Support“
- GitHub-Issue: #127
- Pull Request: #128
- Merge-Commit: `173bfba`

## Start für nächste Codex-Session

1. `AGENTS.md` lesen.
2. `AI_HANDOFF.md` lesen.
3. Für Issue-Arbeit die konkrete Datei unter `docs/issues` lesen.
4. Bei Reihenfolge/Status `docs/issues/overview.txt` lesen.

## Workflow-Erinnerung

- `docs/issues/overview.txt` ist führend für Issue-Status, Priorität und Reihenfolge.
- Ohne ausdrückliche Anweisung nicht committen, pushen, PR erstellen, mergen, GitHub-Issue schließen oder Branch löschen.
- Bei neuen oder geänderten Funktionen Hilfe-, Info- und Tooltip-Texte prüfen.
- Host-Build und Installation werden durch den Benutzer bestätigt:
  - `./gradlew clean assembleDebug`
  - `./gradlew installDebug`
