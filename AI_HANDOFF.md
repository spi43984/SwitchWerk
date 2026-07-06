# AI Handoff

Stand: 6. Juli 2026

## Aktueller Stand

- Issue 084 / GitHub-Issue #192 `Gruppenmitglied-Swipe dialogweit schließen`
  ist über Pull Request #193 nach `main` gemergt.
- Der offene Swipe-Zustand eines Gruppenmitglieds wird im gesamten
  Gruppenformular verwaltet. Ein kurzer Tap oberhalb oder innerhalb der
  Mitgliederliste schließt den Swipe.
- Die bewegungstolerante Tap-Erkennung aus dem Geräteformular liegt jetzt bei
  der gemeinsamen Swipe-Komponente und wird von Geräte- und Gruppenformular
  identisch verwendet.
- Konfigurationsimport und -export sowie gespeicherte Gruppendaten sind nicht
  betroffen. Hilfe-, Info- und Tooltip-Texte mussten nicht geändert werden, da
  bestehendes Bedienverhalten nur vereinheitlicht wurde.
- GitHub-Issue #192 ist geschlossen; der Feature-Branch ist nach dem Merge
  entfernt.

## Prüfungen

- `./gradlew testDebugUnitTest lintDebug` war im Container erfolgreich.
- Die Kotlin-Kompilierung war erfolgreich; es erschienen nur bestehende
  Deprecation-Warnungen außerhalb des Fixes.
- Der manuelle Release-Test auf dem Ubuntu-Host wurde vom Benutzer bestätigt.

## Nächste Schritte

- Neue Sessions sollen mit `AI_SESSION_PROMPT.md` starten und danach diesen
  aktuellen Handoff berücksichtigen.
- Es gibt aktuell kein offenes Implementierungs-Issue. Die nächste Empfehlung
  in `docs/issues/overview.txt` ist Issue 075 `Schnellkacheln`, derzeit
  Backlog.
