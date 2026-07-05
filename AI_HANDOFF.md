# AI Handoff

Stand: 5. Juli 2026

## Abgeschlossen

- Issue 074 „Schaltgruppen“
- GitHub-Issue: #173
- Pull Request: #175
- Squash-Merge auf `main`: `5bd4e1e`
- Schaltgruppen sind eigene lokale Modelle/Room-Entities und werden nicht als
  normale Geräte gespeichert.
- Dashboard zeigt Geräte und Schaltgruppen über ein gemeinsames UI-Modell.
- Schaltgruppen werden sequenziell über einen eigenen Service ausgeführt, der
  intern `DeviceActionService` nutzt.
- Dasselbe Gerät kann mehrfach in derselben Schaltgruppe vorkommen.
- Pausen pro Gruppenmitglied sind exportierbar/importierbar; eigene Pausen sind
  bis 1 Stunde erlaubt und werden als `HH:MM:SS.mmm` angezeigt.
- Fehlerbehandlung ist konfigurierbar: beim ersten Fehler abbrechen oder nach
  Fehlern weiter ausführen.
- Leere Gruppen bleiben sichtbar, sind aber nicht ausführbar.
- Folgeissue 079 „Schaltgruppen App Shortcuts“ wurde als P1 angelegt.
- Lokale Prüfungen, GitHub-Prüfungen und der vom Benutzer bestätigte Host-Test
  waren erfolgreich.

## Nächster Stand

- Nächstes offenes Issue nach `docs/issues/overview.txt`: Issue 078 „Release
  Notes mit GitHub Models vorbefüllen“.
- Weitere Reihenfolge und Status ausschließlich aus
  `docs/issues/overview.txt` entnehmen.

Für die nächste Session `AI_SESSION_PROMPT.md` als wiederverwendbare
Startvorlage verwenden.

Ohne ausdrückliche Anweisung nicht committen, pushen, PR erstellen, mergen,
GitHub-Issue schließen oder Branch löschen.
