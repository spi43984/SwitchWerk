# AI Handoff

Stand: 6. Juli 2026

## Aktueller Stand

- Issue 082 / GitHub-Issue #190 "Gemeinsame Aktionsdetails für alle
  Einstiegspfade" ist über Pull Request #191 nach `main` gemergt.
- Dashboard, Homescreen-Widgets, App Shortcuts und externe Intents verwenden
  denselben flüchtigen `InMemoryActionDetailStore` aus Koin.
- Aktionsblöcke bleiben auch bei parallelen Aktionen getrennt und kennzeichnen
  ihre Herkunft. Ungültige Ziele werden ohne IDs oder veraltete
  Konfigurationswerte beschrieben.
- Geräteadressen werden in sichtbaren Geräte-, HTTP- und DNS-Diagnosen nicht
  ausgegeben. Action-Services, Netzwerklogik, Berechtigungen sowie
  Konfigurationsimport und -export blieben unverändert.
- Default-, deutsche und englische Texte sowie die Hilfe-/Info-Erklärung wurden
  konsistent aktualisiert. Separate Tooltips waren nicht erforderlich.
- Es gibt aktuell kein offenes Implementierungs-Issue. Die nächste Empfehlung
  in `docs/issues/overview.txt` ist Issue 075 "Schnellkacheln", derzeit Backlog.

## Prüfungen

- `testDebugUnitTest` wurde vom Benutzer auf dem Ubuntu-Host erfolgreich
  bestätigt.
- Zwei Release-Lint-Befunde zu API-35-`removeFirst()` wurden durch
  API-26-kompatibles `removeAt(0)` behoben.
- Pull Request #191 ist gemergt; Merge-/Squash-Commit auf `main`: `2a5d16b`.
- Hilfe-, Info- und Übersetzungstexte wurden für Default, Deutsch und Englisch
  auf Konsistenz geprüft.

## Nächste Schritte

- Kein offenes Implementierungs-Issue ist aktiviert.
- Vor einer weiteren Implementierung ein Backlog-Issue ausdrücklich aktivieren
  oder ein neues lokales Planungs-Issue nach dem dokumentierten Workflow
  anlegen.
