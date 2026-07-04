# AI Handoff

Stand: 4. Juli 2026

## Startvorlage

Für die nächste Session zuerst `AI_SESSION_PROMPT.md` verwenden und danach
`AGENTS.md` sowie diesen Handoff beachten.

## Aktueller Stand

Abgeschlossen:

- Issue 070 „Dependabot Vulnerabilities prüfen“
- GitHub-Issue: #164
- Pull Request: #165
- Commit auf `main`: `20caac2 fix: address Dependabot build vulnerabilities`
- Der Foojay-Toolchain-Resolver wurde entfernt; gepatchte Versionen der direkt
  adressierbaren Buildscript-Abhängigkeiten werden erzwungen.
- Build, Lint, Unit-Tests, Release-Installation und manueller Smoke-Test wurden
  im Container bzw. auf dem Ubuntu-Host erfolgreich bestätigt.
- Die GitHub Dependency Submission auf `main` war erfolgreich.
- Zwei Alerts (`jose4j`, `jdom2`) wurden geschlossen. 33 Alerts bleiben für
  ältere transitive Build-Tooling-Versionen offen; sie sind in
  `docs/issues/070-dependabot-vulnerabilities-pruefen.md` begründet
  zurückgestellt und betreffen nicht den App-Runtime-Classpath.

Nächster Stand:

- Es gibt kein Issue mit Status `offen`.
- Die verbleibenden Einträge 024 und 047 bis 052 haben Status `Backlog` und
  werden nur nach ausdrücklicher Aktivierung umgesetzt.

Ohne ausdrückliche Anweisung nicht committen, pushen, PR erstellen, mergen,
GitHub-Issue schließen oder Branch löschen.
