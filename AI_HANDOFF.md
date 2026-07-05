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

- Das lokale Planungs-Issue 082 "Gemeinsame Aktionsdetails für alle
  Einstiegspfade" wurde unter `docs/issues` angelegt und in
  `docs/issues/overview.txt` nach Issue 073 eingeordnet.
- Issue 082 plant einen gemeinsamen flüchtigen, sicheren Detail-Store für
  Dashboard, Widgets, App Shortcuts und externe Intents. Es wurde noch nicht
  implementiert und nicht als GitHub-Issue veröffentlicht.
- Nächstes offenes Implementierungs-Issue bleibt gemäß Priorisierung Issue 073
  "Farbige Geräte".
- Der Benutzer testet standardmäßig Release. Nach Änderungen die passenden
  schnellen Release-Befehle ohne `clean` ausgeben.
