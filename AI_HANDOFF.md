# AI Handoff

Stand: 4. Juli 2026

## Aktive Arbeit

- Issue 072 „SwitchWerk empfängt Intents“ ist auf dem lokalen Branch
  `switchwerk-intents` implementiert, aber noch nicht auf dem Ubuntu-Host
  bestätigt oder veröffentlicht.
- Zugehöriges GitHub-Issue: #168.
- Externe Intents sind standardmäßig deaktiviert und global unter
  Einstellungen → System aktivierbar.
- Die Action `de.piecha.switchwerk.action.RUN_DEVICE_ACTION` akzeptiert
  ausschließlich das String-Extra `de.piecha.switchwerk.extra.DEVICE_ID`.
- Der Parser lehnt fehlende, ungültige und zusätzliche Parameter ab. Das
  MainViewModel löst nur bereits konfigurierte Geräte-IDs auf und verwendet die
  bestehende DeviceActionService-Logik.
- Start-Intents und `onNewIntent` werden verarbeitet; Fortschritt und Ergebnis
  erscheinen im vorhandenen Dashboard-/Aktionsprotokoll.
- Deutsche und englische UI-/Hilfetexte sowie die README-Nutzungsdokumentation
  sind ergänzt.

## Prüfstand

- Container: `./gradlew testDebugUnitTest` erfolgreich.
- Container: `./gradlew lintDebug` erfolgreich.
- Die Intent-Ausführung mit einer konfigurierten Geräte-ID wurde vom Benutzer
  per ADB erfolgreich getestet.
- Vollständige Host-Build-Prüfung und Installation sind noch nicht bestätigt.

Ohne ausdrückliche Anweisung nicht committen, pushen, PR erstellen, mergen,
GitHub-Issue schließen, lokale Issue-Dateien abhaken oder Branch löschen.
