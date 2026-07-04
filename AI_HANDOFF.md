# AI Handoff

Stand: 4. Juli 2026

## Startvorlage

Für die nächste Session zuerst `AI_SESSION_PROMPT.md` verwenden und danach
`AGENTS.md` sowie diesen Handoff beachten.

## Aktueller Stand

Abgeschlossen:

- Issue 069 „Stable Latest APK Release Asset“
- GitHub-Issue: #163
- Branch: `stable-latest-apk-release-asset`
- Commit auf `main`: `14fbfb7 fix: add stable latest apk release asset`
- `scripts/release-github.sh` erzeugt weiterhin das bestehende versionierte
  Release-Asset `SwitchWerk-${VERSION}.apk`.
- Zusätzlich erzeugt das Script `SwitchWerk.apk` aus derselben geprüften
  Release-APK.
- `gh release create` erhält beide APK-Dateien als Assets.
- Der lokale Feature-Branch wurde gelöscht.
- Es existierte kein Remote-Branch `stable-latest-apk-release-asset`.
- Container-Prüfung: `bash -n scripts/release-github.sh`

Noch durch den Benutzer beim nächsten echten Release zu prüfen:

- Release-Script auf dem Host ausführen.
- Beide GitHub-Release-Assets herunterladen.
- Stable-Link `/releases/latest/download/SwitchWerk.apk` prüfen.
- APK-Signatur der heruntergeladenen APK prüfen.

Nächstes priorisiertes Thema:

- Issue 070 „Dependabot Vulnerabilities prüfen“

Ohne ausdrückliche Anweisung nicht committen, pushen, PR erstellen, mergen,
GitHub-Issue schließen oder Branch löschen.
