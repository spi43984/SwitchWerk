# AI Handoff

Stand: 4. Juli 2026

## Startvorlage

Für die nächste Session zuerst `AI_SESSION_PROMPT.md` verwenden und danach
`AGENTS.md` sowie diesen Handoff beachten.

## Aktueller Stand

Abgeschlossen:

- Issue 068 „GUI-Navigation und Konfiguration vereinfachen“
- GitHub-Issue: #161
- Pull Request: #162
- Branch: `docs/issue-068-gui-navigation-konfiguration`
- Einrichtungs-Assistent zeigt nur noch den kurzen Einstieg und direkte
  Einstiegsaktionen für Hilfe, Konfigurationen, WLAN-Profile, Geräte und
  Dashboard.
- Sichtbare Bezeichnung `Backup` wurde zu `Konfigurationen` geändert.
- Hilfe, Info-/i-Texte und Über SwitchWerk wurden auf die neue Navigation und
  Terminologie angepasst.
- Import und Export im Bereich `Konfigurationen` sind optisch getrennt.
- Passwortauswahl für Export und Import ist eine Dreifachauswahl mit sicherer
  Mittelposition; Aktionsbuttons bleiben dort deaktiviert.
- Sicherheitskritische Passwortauswahlen werden nach Abschluss, Abbruch oder
  Fehler zurückgesetzt.
- Importdialog hebt die nächsten Schritte ruhig pulsierend hervor und scrollt
  nach Passwortentscheidung zur Zusammenfassung, soweit die Bildschirmhöhe das
  zulässt.
- Deutsche, englische und Fallback-Strings wurden konsistent geprüft.
- Über SwitchWerk und Menü-Footer zeigen nur noch `Version x.y.z`, kein hart
  codiertes Veröffentlichungsdatum.
- Container-Prüfungen: `./gradlew :app:compileDebugKotlin`,
  `./gradlew :app:lintDebug`
- GitHub Actions in PR #162 erfolgreich: `build`, `submit-gradle`
- Host-Prüfungen laut Benutzerrückmeldung erfolgreich: Build und Tests.

Nächstes priorisiertes Thema:

- Issue 024 „Authenticated Import Sources Backlog“; aktuell Status `Backlog`,
  daher keine aktive Implementierung ohne ausdrückliche Reaktivierung.

Ohne ausdrückliche Anweisung nicht committen, pushen, PR erstellen, mergen,
GitHub-Issue schließen oder Branch löschen.
