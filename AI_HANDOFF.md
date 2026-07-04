# AI Handoff

Stand: 4. Juli 2026

## Aktive Arbeit

- Issue 071 „App Shortcuts“, GitHub-Issue #166
- Lokaler Branch: `app-shortcuts`
- Implementierung abgeschlossen, noch nicht auf einem Android-Gerät geprüft
- Kein Commit, Push, Pull Request oder Merge erstellt

## Implementierungsstand

- Pro Gerät ist `shortcutEnabled` lokal in Room gespeichert; Migration 8 → 9
  verwendet den sicheren Default `false`.
- Konfigurationsschema 5 exportiert und importiert `shortcutEnabled`. Ältere
  Konfigurationen ohne Feld verwenden `false`; Merge und Replace verwenden die
  bestehenden Importpfade unverändert.
- `AppShortcutCoordinator` beobachtet Geräteänderungen und aktualisiert gekapselt
  die dynamischen Android-Shortcuts. Aktivierte Geräte werden in
  Dashboard-Reihenfolge gewählt und auf das kleinere Limit aus Androids
  Aktivitätslimit und vier Empfehlungen begrenzt.
- Shortcut-Intents enthalten nur die opake Geräte-ID. `MainActivity` öffnet das
  Dashboard; `MainViewModel` löst das aktuelle Gerät auf und nutzt den bestehenden
  `DeviceActionService` für Fortschritt, Erfolg und Fehler.
- Umbenennen, Löschen sowie Merge-/Replace-Import aktualisieren die Shortcuts über
  denselben Repository-Flow.
- Deutsche und englische UI- und Hilfetexte wurden ergänzt.
- Fokussierte Tests decken Auswahl, Reihenfolge, Begrenzung, Intent-Auswertung und
  den sicheren Konfigurationsdefault ab.
- `AGENTS.md`, `AI_SESSION_PROMPT.md`, `TESTING.md`, `README.md`,
  `GITHUB_WORKFLOW.md` und `docs/release-build.md` dokumentieren jetzt getrennte
  Debug- und Release-Prüfblöcke. Für Release gilt ebenfalls
  `testDebugUnitTest`, da kein `testReleaseUnitTest`-Task existiert.

## Ausgeführte Prüfungen im Container

- `./gradlew compileDebugKotlin` erfolgreich
- `./gradlew testDebugUnitTest` erfolgreich: 121 Tests
- `./gradlew lintDebug` erfolgreich
- `git diff --check` erfolgreich

## Noch auf dem Ubuntu-Host zu prüfen

- Lint und Unit-Tests erneut ausführen.
- Clean-Debug-Build und Installation ausführen.
- Shortcuts manuell prüfen: Aktivieren, App-Symbol lange drücken, Aktion bei
  Erfolg und nicht erreichbarem Gerät, Umbenennen, Löschen, App-Neustart,
  Begrenzung bei mehr als vier aktivierten Geräten sowie Import per Merge und
  Replace einschließlich älterer Konfiguration ohne Shortcut-Feld.

Ohne ausdrückliche Anweisung nicht committen, pushen, PR erstellen, mergen,
GitHub-Issue schließen oder Branch löschen.
