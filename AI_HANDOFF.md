# AI Handoff

Stand: 26. Juni 2026

## Aktueller Stand

Zuletzt lokal abgeschlossen:

- Issue 031 „Import Enforces Unique WiFi Profile Names“
- Branch: `import-unique-wifi-profile-names`
- Import-Fehler beim Laden einer Konfiguration werden im Importdialog angezeigt
  statt global unter dem Dialog.
- Hilfe-, Info- und Projektdokumentation erklären jetzt, dass
  Geräte-WLAN-Zuordnungen über WLAN-Profil-IDs erfolgen, gleiche IDs beim
  Merge-Import überschrieben werden und gleiche Profilnamen mit anderer ID
  abgelehnt werden.
- Container-Prüfung `./gradlew clean assembleDebug` erfolgreich. Benutzer meldete
  auf dem Host: `assembleDebug` erfolgreich, `installDebug` scheiterte danach
  mit fehlenden `R.string.help_import_title`/`help_import_text`, vermutlich
  wegen nicht synchroner String-Ressourcen oder inkrementellem Gradle-Zustand.
- `docs/issues/031-import-enforces-unique-wifi-profile-names.md` und
  `docs/issues/overview.txt` sind lokal auf `abgeschlossen` gesetzt.
- Nächstes offenes Issue nach `docs/issues/overview.txt`: Issue 032
  „Room Schema And Migration Test Coverage“.

Zuletzt abgeschlossen:

- Issue 022 „Request Body And Content-Type Support“
- GitHub-Issue: #127
- Pull Request: #128
- Merge-Commit: `173bfba`

## Wichtige offene Hinweise

- Issue 057 „Encrypted Storage Restore Start Crash“ war laut älterem Handoff lokal abgeschlossen und vom Benutzer bestätigt, aber möglicherweise noch nicht veröffentlicht. Status bei Bedarf in GitHub und `docs/issues/overview.txt` prüfen.
- Issue 019 „Configurable WiFi List Sorting“ war laut älterem Handoff lokal implementiert und bestätigt, aber möglicherweise noch nicht veröffentlicht. Status bei Bedarf in GitHub und `docs/issues/overview.txt` prüfen.

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
