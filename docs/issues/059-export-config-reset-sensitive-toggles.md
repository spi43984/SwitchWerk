# Issue 059: Export Config Reset Sensitive Toggles

## Metadaten

- Status: Abgeschlossen
- Priorität: P0
- Typ: Bugfix / Security / UX
- Bereich: Import/Export / Einstellungen / Projektdokumentation

## Ziel

Sicherheitskritische Schieberegler dürfen nur für die unmittelbar folgende Aktion gelten und müssen danach automatisch wieder auf den sicheren Standardwert zurückgesetzt werden.

Konkret soll beim Export der Konfiguration der Schieberegler **Passwörter einschließen** nach jeder Export-Aktion wieder deaktiviert sein.

## Hintergrund

Beim Export der Konfiguration bleibt der Schieberegler **Passwörter einschließen** nach Abschluss des Exports aktiviert.

Dadurch besteht die Gefahr, dass ein späterer Export versehentlich erneut Passwörter enthält.

Dieses Verhalten betrifft grundsätzlich alle sicherheitskritischen Schieberegler, deren Aktivierung nur für eine einzelne Aktion gedacht ist.

## Scope

### Export-Konfiguration

- Verhalten des Exportdialogs prüfen.
- Schieberegler **Passwörter einschließen** nach jeder Beendigung des Exportvorgangs automatisch auf `false` zurücksetzen.
- Das Zurücksetzen erfolgt unabhängig davon, wie die Aktion endet:
  - erfolgreicher Export
  - Benutzerabbruch
  - Fehler während des Exports
- Ein erneuter Export beginnt immer mit deaktiviertem Schalter.
- UI-Zustand und gespeicherte Einstellungen dürfen nach der Aktion keinen aktivierten sicherheitskritischen Export-Schalter mehr enthalten.

### Allgemeines Verhalten für kritische Schieberegler

- Prüfen, ob es weitere sicherheitskritische Schieberegler gibt, die ebenfalls nur für eine einzelne Aktion gelten dürfen.
- Falls vorhanden, diese Schalter nach Abschluss oder Abbruch der jeweiligen Aktion ebenfalls zurücksetzen.
- Die Lösung soll möglichst wiederverwendbar bzw. als klares Muster für weitere kritische Schalter umgesetzt werden.

### Projektdokumentation

Im Rahmen dieses Issues sind die relevanten Projektdokumente zu ergänzen.

Dabei ist als dauerhafte Projektregel festzuhalten:

- Sicherheitskritische Schieberegler dürfen nur für die unmittelbar folgende Aktion gelten.
- Nach Abschluss, Abbruch oder Fehler der Aktion sind sie automatisch auf ihren sicheren Standardwert zurückzusetzen.
- Diese Regel ist insbesondere in `CODE_STYLE.md` und `SECURITY.md` zu dokumentieren.
- Die Regel ist künftig bei allen neuen Funktionen mit sicherheitskritischen UI-Schaltern zu berücksichtigen.

## Nicht im Scope

- Änderung des Import-/Export-Dateiformats
- Änderung der Verschlüsselungslogik
- Änderung bestehender Backup-Dateien
- Neues Berechtigungssystem
- GitHub-Issue, Branch, Pull Request oder Merge

## Architekturhinweise

- Bestehende Compose-, State- und Settings-Struktur beibehalten.
- Keine neue externe Abhängigkeit einführen.
- Sichere Standardwerte sollen zentral nachvollziehbar sein.
- Sicherheitskritische One-Shot-Optionen sollen nicht dauerhaft aktiv bleiben.
- Dokumentationsänderungen sind Bestandteil dieses Issues und sollen zusammen mit dem Bugfix umgesetzt werden.

## Akzeptanzkriterien

- [x] Nach erfolgreichem Export ist **Passwörter einschließen** deaktiviert.
- [x] Nach Abbruch des Exportvorgangs ist **Passwörter einschließen** deaktiviert.
- [x] Nach einem Fehler während des Exports ist **Passwörter einschließen** deaktiviert.
- [x] Ein erneuter Export beginnt immer mit deaktiviertem Schalter.
- [x] Weitere vorhandene sicherheitskritische One-Shot-Schieberegler wurden geprüft.
- [x] Falls weitere betroffene Schieberegler vorhanden sind, werden sie ebenfalls nach Abschluss oder Abbruch ihrer Aktion zurückgesetzt.
- [x] Das Verhalten ist als wiederverwendbares Muster nachvollziehbar umgesetzt.
- [x] `CODE_STYLE.md` wurde um die Regel für sicherheitskritische Schieberegler ergänzt.
- [x] `SECURITY.md` wurde um das One-Shot-Prinzip für sicherheitskritische Optionen ergänzt.

## Abschlussnotizen

- GitHub-Issue: #134
- Der Export-Schalter **Passwörter einschließen** wird nur noch als One-Shot-Option verwendet und nach Start, Abbruch oder Fehlerpfad auf `false` zurückgesetzt.
- Der ebenfalls sicherheitskritische Import-Schalter **Passwörter importieren** wurde geprüft und analog nach Import oder Abbruch zurückgesetzt.
- Hilfe- und Info-Texte wurden aktualisiert.
- Container-Prüfungen erfolgreich:
  - `./gradlew lintDebug`
  - `./gradlew testDebugUnitTest`
- Host-Prüfungen vom Benutzer bestätigt:
  - `./gradlew clean assembleDebug`
  - `./gradlew installDebug`
