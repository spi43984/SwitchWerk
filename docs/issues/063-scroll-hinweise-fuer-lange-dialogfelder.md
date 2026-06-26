# Issue 063: Scroll-Hinweise für lange Dialogfelder

## Metadaten

- Status: Abgeschlossen
- Priorität: P1
- Typ: UX / UI
- Bereich: Dialoge / Einstellungen / Compose UI

## Ziel

Lange Dialogfelder sollen klar anzeigen, wenn weiterer Inhalt nach oben oder unten scrollbar ist.

Anwender sollen sofort erkennen können, dass ein Dialog nicht vollständig sichtbar ist und durch Scrollen weitere Inhalte erreichbar sind.

## Hintergrund

Lange Dialogfelder, zum Beispiel im Bereich `Einstellungen -> System`, können über eine Bildschirmseite hinausgehen.

Die Scrollfunktion ist grundsätzlich vorhanden, aber der Anwender sieht nicht immer sofort, dass gescrollt werden kann. In Detailansichten bei WLAN-Profilen oder Geräten ist die Scrollbarkeit bereits verständlicher erkennbar.

Dieses Issue soll eine konsistente Anzeige vorschlagen und einführen, die bei langen Dialoginhalten signalisiert, ob nach oben oder unten gescrollt werden kann.

## Scope

### Scroll-Hinweise für lange Dialoge

- Bestehende Dialog-Basis und lange Dialoginhalte prüfen.
- Eine visuelle Anzeige einführen, wenn oberhalb oder unterhalb des sichtbaren Bereichs weiterer Inhalt vorhanden ist.
- Die Anzeige soll unterscheiden können:
  - weiter unten ist Inhalt vorhanden
  - weiter oben ist Inhalt vorhanden
  - oben und unten ist weiterer Inhalt vorhanden
- Die Anzeige soll nur erscheinen, wenn tatsächlich gescrollt werden kann.
- Bei ausreichend Platz ohne Scrollbedarf soll keine zusätzliche Anzeige sichtbar sein.
- Die Lösung soll möglichst zentral und wiederverwendbar umgesetzt werden.
- Scrollbare Hilfs-, Info-, Setup- und Menüflächen sollen denselben Scroll-Hinweis verwenden, sofern sie keinen bewusst abweichenden inneren Scroll-Hinweis haben.
- Das Hamburger-Menü soll zusätzlich zum Schließen über `X` auch durch einen Klick außerhalb des Menüs geschlossen werden können.

### Betroffene Bereiche

- Lange Dialogfelder, insbesondere `Einstellungen -> System`.
- Weitere lange Dialoge, sofern sie dieselbe Dialog-Basis oder denselben Mechanismus nutzen.
- Hilfe, Über SwitchWerk, Setup-Wizard und Hamburger-Menü, sofern deren Inhalt scrollbar ist.

### UX-Vorgaben

- Die Anzeige soll unaufdringlich, aber eindeutig sein.
- Mögliche Umsetzungen sind zum Beispiel Fade-Verläufe, kleine Pfeil-/Chevron-Hinweise oder eine andere zur bestehenden Material-3-Optik passende Lösung.
- Die Anzeige darf keine wichtigen Inhalte, Schalter oder Aktionen verdecken.
- Die Bedienbarkeit mit großer Schriftgröße und im Landscape-Modus muss erhalten bleiben.

## Nicht im Scope

- Keine Änderung der Scrollfunktion bei WLAN-Profilen.
- Keine Änderung der Scrollfunktion bei Geräten.
- Keine fachlichen Änderungen an Einstellungen, WLAN-Profilen oder Geräten.
- Keine neue Navigation.
- Keine Umstrukturierung der Einstellungsbereiche.
- Keine neue externe Abhängigkeit.
- Kein GitHub-Issue, Branch, Pull Request oder Merge im Rahmen des lokalen Planungs-Issues.

## Architekturhinweise

- Bestehende Compose- und Material-3-Struktur beibehalten.
- Eine zentrale UI-Komponente unter `ui/components/` bevorzugen, falls dies zur bestehenden Struktur passt.
- UI-Zustand und fachliche Logik bleiben in den bestehenden ViewModels beziehungsweise State-Haltern.
- Die Lösung soll rein visuell anzeigen, ob Scrollen möglich ist, und keine bestehende Scrolllogik in WLAN-Profilen oder Geräten verändern.
- Hilfe-, Info- und Tooltip-Texte prüfen und bei Bedarf auf Deutsch und Englisch aktualisieren.

## Akzeptanzkriterien

- [x] Lange Dialogfelder zeigen sichtbar an, wenn nach unten gescrollt werden kann.
- [x] Lange Dialogfelder zeigen sichtbar an, wenn nach oben gescrollt werden kann.
- [x] Die Anzeige verschwindet, wenn in die jeweilige Richtung kein weiterer Inhalt vorhanden ist.
- [x] Bei Dialogen ohne Scrollbedarf wird keine Scroll-Anzeige eingeblendet.
- [x] `Einstellungen -> System` ist bei langen Inhalten klar als scrollbar erkennbar.
- [x] Die Scrollfunktion bei WLAN-Profilen bleibt unverändert.
- [x] Die Scrollfunktion bei Geräten bleibt unverändert.
- [x] Dialogaktionen bleiben erreichbar und werden durch die Anzeige nicht verdeckt.
- [x] Die Lösung funktioniert im Portrait-Modus.
- [x] Die Lösung funktioniert im Landscape-Modus.
- [x] Die Lösung funktioniert bei großer Android-Schriftgröße.
- [x] Hilfe, Über SwitchWerk, Setup-Wizard und Hamburger-Menü zeigen bei Scrollbedarf denselben Scroll-Hinweis.
- [x] Das Hamburger-Menü lässt sich per `X` und per Klick außerhalb des Menüs schließen.
- [x] Hilfe-, Info- und Tooltip-Texte wurden geprüft und bei Bedarf aktualisiert.
- [x] Deutsch und Englisch sind bei neuen Texten konsistent gepflegt.

## Testhinweise

- `Einstellungen -> System` mit langem Inhalt prüfen.
- Dialog am oberen Scrollrand öffnen.
- Dialog nach unten scrollen.
- Dialog bis zum unteren Scrollrand scrollen.
- Dialog mit Inhalt prüfen, der vollständig auf eine Bildschirmseite passt.
- Portrait-Modus prüfen.
- Landscape-Modus prüfen.
- Große Android-Schriftgröße prüfen.
- WLAN-Profil-Details prüfen und sicherstellen, dass deren Scrollfunktion unverändert bleibt.
- Geräte-Details prüfen und sicherstellen, dass deren Scrollfunktion unverändert bleibt.
