# AI Handoff

Stand: 5. Juli 2026

## Abgeschlossen

- Issue 079 "Schaltgruppen App Shortcuts"
- GitHub-Issue: #180
- Branch: `schaltgruppen-app-shortcuts`
- Schaltgruppen können als Android App Shortcuts veröffentlicht werden.
- Geräte- und Gruppen-Shortcuts teilen sich das Android-Limit und folgen der
  gemeinsamen Dashboard-Reihenfolge.
- Schaltgruppen können bei aktivierter externer Intent-Freigabe per lokaler
  Gruppen-ID gestartet werden.
- Leere Schaltgruppen werden weder als Shortcut veröffentlicht noch per
  externem Gruppen-Intent ausgeführt.
- Import/Export überträgt die Gruppenshortcut-Auswahl; ältere Importe bleiben
  mit Default `false` kompatibel.
- Deutsche, englische und Default-String-Ressourcen wurden konsistent gepflegt.
- `GITHUB_WORKFLOW.md` dokumentiert, dass Release-Befehle zum Kompilieren und
  Installieren künftig immer ausgegeben werden.

## Prüfungen

- `./gradlew testDebugUnitTest`
- `./gradlew lintDebug`
- Host-Test wurde vom Benutzer als erfolgreich gemeldet.

## Nächster Stand

- Nächstes offenes Issue nach `docs/issues/overview.txt`: Issue 073
  "Farbige Geräte".
- Weitere Reihenfolge und Status ausschließlich aus
  `docs/issues/overview.txt` entnehmen.

Ohne ausdrückliche Anweisung nicht committen, pushen, PR erstellen, mergen,
GitHub-Issue schließen oder Branch löschen.
