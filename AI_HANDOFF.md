# AI Handoff

Stand: 6. Juli 2026

## Aktueller Stand

- Issue 081 "Homescreen Widgets" ist abgeschlossen.
- Feature-Commit auf `homescreen-widgets`: `654378b`.
- Pull Request #184 wurde als `a8dc926` nach `main` gemergt.
- GitHub-Issue #182 ist geschlossen.
- Lokale Issue-Datei und `docs/issues/overview.txt` zeigen 081 als
  abgeschlossen; nächstes offenes Implementierungs-Issue ist 073 "Farbige
  Geräte".
- Architektur: klassische Android AppWidget/RemoteViews mit Compose-
  Konfigurations-Activity; Zuordnungen liegen launcher-lokal in
  SharedPreferences und sind bewusst nicht Teil von Import/Export.
- Widget-Aktionen delegieren ausschließlich an `DeviceActionService` und
  `SwitchGroupActionService`. PendingIntents enthalten nur Widget-ID und
  Eintragsindex.
- Deutsche, englische und Default-Texte sowie Hilfe-/Info-Texte sind konsistent.
- Bekannte Plattformgrenze: Ein nach der Neukonfiguration sichtbar bleibendes
  Launcher-Kontextmenü kann SwitchWerk nicht über eine öffentliche Android-API
  schließen.

## Prüfungen

- `./gradlew testDebugUnitTest`
- `./gradlew lintDebug`
- GitHub Actions PR #184: `build` und `submit-gradle` erfolgreich.
- Iterative Release-Installationen und manuelle Gerätetests für Widget-Größen,
  Skalierung, Konfiguration, Layout, Statusfarben und Ausführung.

## Nächster Auftrag

- Der Benutzer möchte als Nächstes ein separates lokales Planungs-Issue für
  gemeinsame Aktionsdetails dokumentieren.
- Hintergrund: Widget-Aktionen schreiben aktuell nicht in die vom Dashboard
  gehaltenen Aktionsdetails. Zu prüfen ist ein gemeinsamer sicherer Detail-Store
  für Dashboard, Widgets, App Shortcuts und externe Intents.
- Vor dem Anlegen gemäß `AGENTS.md` GitHub lesend prüfen,
  `docs/issues/overview.txt` lesen und die nächste freie lokale Issue-ID
  bestimmen. Noch kein Issue wurde dafür angelegt.
- Der Benutzer testet standardmäßig Release. Nach Änderungen die passenden
  schnellen Release-Befehle ohne `clean` ausgeben.
