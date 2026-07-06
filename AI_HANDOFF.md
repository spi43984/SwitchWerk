# AI Handoff

Stand: 6. Juli 2026

## Aktueller Stand

- Issue 084 / GitHub-Issue #192 `Gruppenmitglied-Swipe dialogweit schließen`
  ist auf dem Branch `fix-group-member-swipe-dismiss` implementiert, aber noch
  nicht committet oder veröffentlicht.
- Der offene Swipe-Zustand eines Gruppenmitglieds wird im gesamten
  Gruppenformular verwaltet. Ein kurzer Tap oberhalb oder innerhalb der
  Mitgliederliste schließt den Swipe.
- Die bewegungstolerante Tap-Erkennung aus dem Geräteformular liegt jetzt bei
  der gemeinsamen Swipe-Komponente und wird von Geräte- und Gruppenformular
  identisch verwendet.
- Konfigurationsimport und -export sowie gespeicherte Gruppendaten sind nicht
  betroffen. Hilfe-, Info- und Tooltip-Texte mussten nicht geändert werden, da
  bestehendes Bedienverhalten nur vereinheitlicht wurde.

## Prüfungen

- `./gradlew testDebugUnitTest lintDebug` war im Container erfolgreich.
- Die Kotlin-Kompilierung war erfolgreich; es erschienen nur bestehende
  Deprecation-Warnungen außerhalb des Fixes.
- Der manuelle Release-Test auf dem Ubuntu-Host steht noch aus.

## Nächste Schritte

- Neue Sessions sollen mit `AI_SESSION_PROMPT.md` starten und danach diesen
  aktuellen Handoff berücksichtigen.
- Auf dem Ubuntu-Host Release bauen, installieren und Issue 084 anhand der
  Testhinweise manuell prüfen.
- Nach erfolgreicher Benutzerbestätigung und ausdrücklicher Freigabe Commit,
  Push und Pull Request für GitHub-Issue #192 erstellen.
