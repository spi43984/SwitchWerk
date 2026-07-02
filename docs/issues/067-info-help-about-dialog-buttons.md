# Issue 067: Info, Hilfe und Über-Dialog Buttons

## Metadaten

- Status: Abgeschlossen
- Priorität: P0
- Typ: UX / UI
- Bereich: Info-Texte / Hilfe / Über SwitchWerk / Compose UI

## Ziel

Die Dialog-Buttons in Info-Texten, Hilfe und Über SwitchWerk sollen konsistenter und weniger redundant dargestellt werden.

Info-Texte sollen unten rechts nur noch einen Button `Schließen` anzeigen. Die bisherige Kombination aus `OK` und `Abbrechen` ist dort redundant.

Die Dialoge `Über SwitchWerk` und `Hilfe` sollen jeweils zwei klar angeordnete Aktionen erhalten: links eine fachliche Aktion und rechts `Schließen`.

## Hintergrund

In den Info-Texten erscheinen unten aktuell die Buttons `OK` und `Abbrechen`. Bei reinen Informationstexten ist das unnötig, weil beide Aktionen denselben fachlichen Effekt haben: den Dialog schließen.

Zusätzlich sollen die Aktionsbereiche in `Über SwitchWerk` und `Hilfe` optisch vereinheitlicht werden. Der linke Button enthält die primäre Zusatzaktion, der rechte Button schließt den Dialog.

## Scope

### Info-Texte

- Alle Info-Texte beziehungsweise i-Texte prüfen.
- Unten rechts nur noch einen Button `Schließen` anzeigen.
- Die Buttons `OK` und `Abbrechen` bei reinen Info-Texten entfernen.
- Deutsche und englische Texte prüfen und konsistent pflegen.
- Falls gemeinsame Dialog-Komponenten existieren, die Änderung dort zentral umsetzen.

### Über SwitchWerk

- Unten links einen Button `GitHub-Projekt öffnen` anzeigen.
- Unten rechts daneben einen Button `Schließen` anzeigen.
- Den linken Button so breit machen, dass rechts `Schließen` gerade noch als Button daneben passt.
- Beide Buttons gleich hoch darstellen.
- Die Button-Anordnung muss auch bei größerer Android-Schriftgröße stabil bleiben.
- Die englische Version prüfen und konsistent pflegen.

### Hilfe

- Unten links einen Button `Einrichtungs-Assistent erneut zeigen` anzeigen.
- Unten rechts daneben einen Button `Schließen` anzeigen.
- Den linken Button so breit machen, dass rechts `Schließen` gerade noch als Button daneben passt.
- Beide Buttons gleich hoch darstellen.
- Die Button-Anordnung muss auch bei größerer Android-Schriftgröße stabil bleiben.
- Die englische Version prüfen und konsistent pflegen.

### Betroffene Bereiche

- Info-Dialoge / i-Texte.
- Hilfe-Dialog.
- Über-SwitchWerk-Dialog.
- Gemeinsame Dialog- oder Button-Komponenten.
- Deutsche und englische String-Ressourcen.

## Nicht im Scope

- Keine inhaltliche Überarbeitung der Hilfe- oder Info-Texte.
- Keine Änderung an Navigation, Menüstruktur oder Dashboard-Funktion.
- Keine Änderung am Setup-Wizard selbst außer dem vorhandenen Aufruf aus der Hilfe.
- Keine neue externe Abhängigkeit.
- Keine Änderung an Import, Export, Geräten oder WLAN-Profilen.

## Architekturhinweise

- Bestehende Compose- und Material-3-Struktur beibehalten.
- Gemeinsame Dialog-, Button- und Hilfekomponenten bevorzugt zentral anpassen.
- Keine redundanten Speziallösungen in einzelnen Screens, wenn eine gemeinsame Komponente existiert.
- Button-Anordnung responsiv gestalten, insbesondere für kleine Displays, Landscape und größere Android-Schrift.
- Bei Sicherheitsabfragen bleibt die sichere Abbruchaktion rechts. Dieses Issue betrifft reine Info-, Hilfe- und Über-Dialoge.
- Sichtbare Texte in den String-Ressourcen pflegen und Deutsch/Englisch konsistent halten.

## Akzeptanzkriterien

- [x] Alle i-Texte zeigen unten rechts nur noch `Schließen`.
- [x] Reine Info-Texte zeigen nicht mehr gleichzeitig `OK` und `Abbrechen`.
- [x] `Über SwitchWerk` zeigt unten links `GitHub-Projekt öffnen`.
- [x] `Über SwitchWerk` zeigt rechts daneben `Schließen`.
- [x] Im `Über SwitchWerk`-Dialog sind beide Buttons gleich hoch.
- [x] Im `Über SwitchWerk`-Dialog ist der linke Button breiter und der rechte Button gerade ausreichend breit für `Schließen`.
- [x] `Hilfe` zeigt unten links `Einrichtungs-Assistent erneut zeigen`.
- [x] `Hilfe` zeigt rechts daneben `Schließen`.
- [x] Im Hilfe-Dialog sind beide Buttons gleich hoch.
- [x] Im Hilfe-Dialog ist der linke Button breiter und der rechte Button gerade ausreichend breit für `Schließen`.
- [x] Die Darstellung funktioniert im Portrait-Modus.
- [x] Die Darstellung funktioniert im Landscape-Modus.
- [x] Die Darstellung bleibt bei größerer Android-Schriftgröße bedienbar.
- [x] Deutsche und englische Texte sind konsistent gepflegt.
- [x] Bestehende Sicherheitsabfragen behalten die sichere Abbruchaktion rechts.

## Testhinweise

- Alle i-Texte öffnen und prüfen, dass nur `Schließen` angezeigt wird.
- Hilfe öffnen und Button-Anordnung prüfen.
- `Einrichtungs-Assistent erneut zeigen` aus der Hilfe auslösen.
- Über SwitchWerk öffnen und Button-Anordnung prüfen.
- `GitHub-Projekt öffnen` aus Über SwitchWerk auslösen.
- Dialoge im Portrait-Modus prüfen.
- Dialoge im Landscape-Modus prüfen.
- Dialoge mit größerer Android-Schriftgröße prüfen.
- Englische App-Sprache aktivieren und alle betroffenen Dialoge prüfen.
