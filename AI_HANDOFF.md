# AI Handoff

Stand: 2. Juli 2026

## Startvorlage

Für die nächste Session zuerst `AI_SESSION_PROMPT.md` verwenden und danach
`AGENTS.md` sowie diesen Handoff beachten.

## Aktueller Stand

Abgeschlossen:

- Issue 066 „QR Code Import HTTP URL“
- GitHub-Issue: #158
- Branch: `qr-code-import-http-url`
- QR-Code-Import akzeptiert `http://` und `https://`.
- Manueller URL-Import akzeptiert `http://` und `https://`.
- Andere URL-Schemes bleiben ungültig.
- Direkte HTTP-Importe mit Portnummer werden akzeptiert.
- HTTPS-Weiterleitungen auf HTTP bleiben blockiert.
- Deutsche, englische und Default-Strings nennen HTTP/HTTPS statt nur HTTPS.
- Hilfe- und Info-Texte wurden geprüft; sie waren bereits allgemein formuliert.
- Container-Prüfung: `./gradlew testDebugUnitTest`
- Host-Prüfungen laut Benutzerrückmeldung erfolgreich:
  `./gradlew clean assembleDebug`, `./gradlew installDebug`
- Nächstes priorisiertes Thema: Issue 024 „Authenticated Import Sources
  Backlog“; aktuell Status `Backlog`, daher keine aktive Implementierung.

Ohne ausdrückliche Anweisung nicht committen, pushen, PR erstellen, mergen,
GitHub-Issue schließen oder Branch löschen.
