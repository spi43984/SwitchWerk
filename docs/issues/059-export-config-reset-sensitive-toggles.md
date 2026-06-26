# Issue 059: Export Config Reset Sensitive Toggles

## Metadaten

- Status: Offen
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

- [ ] Nach erfolgreichem Export ist **Passwörter einschließen** deaktiviert.
- [ ] Nach Abbruch des Exportvorgangs ist **Passwörter einschließen** deaktiviert.
- [ ] Nach einem Fehler während des Exports ist **Passwörter einschließen** deaktiviert.
- [ ] Ein erneuter Export beginnt immer mit deaktiviertem Schalter.
- [ ] Weitere vorhandene sicherheitskritische One-Shot-Schieberegler wurden geprüft.
- [ ] Falls weitere betroffene Schieberegler vorhanden sind, werden sie ebenfalls nach Abschluss oder Abbruch ihrer Aktion zurückgesetzt.
- [ ] Das Verhalten ist als wiederverwendbares Muster nachvollziehbar umgesetzt.
- [ ] `CODE_STYLE.md` wurde um die Regel für sicherheitskritische Schieberegler ergänzt.
- [ ] `SECURITY.md` wurde um das One-Shot-Prinzip für sicherheitskritische Optionen ergänzt.
