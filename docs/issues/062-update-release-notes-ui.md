# Issue 062: Update Release Notes UI

## Metadaten

* Status: Abgeschlossen
* Priorität: P1
* Typ: UX / Feature
* Bereich: Release / Updates

## Ziel

Der Update-Bereich in den Einstellungen soll Release Notes platzsparender und bedienbarer darstellen.

Anwender sollen die GitHub-Release-Seite über einen klaren Button öffnen können, statt einen Link aus den Release Notes kopieren oder erkennen zu müssen. Lange Release Notes sollen einklappbar sein, damit der Update-Bereich nach Download oder Installation nicht dauerhaft viel Platz belegt.

Abschlussentscheidung: Release Notes werden nicht mehr direkt im Update-Bereich angezeigt. Stattdessen öffnet ein eigener Button die GitHub-Release-Seite im Browser. Dadurch entfällt der einklappbare In-App-Release-Notes-Abschnitt.

## Hintergrund

Issue 037 hat GitHub Release Update Support eingeführt. Die App zeigt Release Notes derzeit als normalen Text im Update-Bereich. Links im Text sind nicht interaktiv, und lange automatisch oder manuell gepflegte Release Notes können den Systembereich der Einstellungen stark verlängern.

Ein dedizierter Button und einklappbare Release Notes verbessern die Bedienbarkeit auf kleinen Displays und halten die Einstellungen übersichtlich.

## Scope

### GitHub-Release-Button

* Im Update-Bereich wird ein Button `GitHub Release Notes öffnen` angezeigt, wenn eine verfügbare Release-URL vorhanden ist.
* Der Button öffnet die GitHub-Release-Seite der angezeigten verfügbaren Version.
* Der Button verwendet die bereits vorhandene Release-URL aus dem Update-Status.
* Es wird keine Markdown-Link-Erkennung und kein Parsing von Release-Notes-Text eingeführt.

### Release Notes

* Release Notes werden nicht mehr direkt im Update-Bereich angezeigt.
* Lange Release Notes belegen in den Einstellungen keinen zusätzlichen Platz.
* Vollständige Release Notes sind über den GitHub-Release-Button erreichbar.

### Zustand nach Download / Installation

* Download- und Installationsbuttons bleiben sichtbar und werden je nach Zustand deaktiviert.
* Wenn die installierte Version aktuell ist, bleibt der Installationsbutton sichtbar, aber deaktiviert.

### UI-Texte

* Hilfe-, Info- und Tooltip-Texte sind zu prüfen.
* Deutsch und Englisch sind bei neuen Texten konsistent zu pflegen.
* Bestehende Update-Hinweise sollen nicht doppelt oder widersprüchlich werden.

## Nicht im Scope

* Markdown-Renderer für Release Notes.
* Automatische Link-Erkennung im Release-Notes-Text.
* Anzeige eines vollständigen Changelog-Vergleichs direkt in der App.
* Änderungen am GitHub Releases API Abruf.
* Änderungen am APK-Download oder Package-Installer-Flow.
* Erzwungene Updates.
* Automatische Installation.

## Akzeptanzkriterien

* [x] Der Update-Bereich zeigt bei vorhandener Release-URL einen Button `GitHub Release Notes öffnen`.
* [x] Der Button öffnet die GitHub-Release-Seite im Browser.
* [x] Release Notes werden nicht mehr redundant direkt im Update-Bereich angezeigt.
* [x] Lange Release Notes belegen in den Einstellungen keinen zusätzlichen Platz.
* [x] Download- und Installationsbuttons bleiben sichtbar und werden je nach Zustand deaktiviert.
* [x] Bei aktueller installierter Version ist `Installation öffnen` deaktiviert.
* [x] Es wird kein Markdown-Link-Parser eingeführt.
* [x] Hilfe-, Info- und Tooltip-Texte wurden geprüft und bei Bedarf aktualisiert.
* [x] Deutsch und Englisch sind konsistent gepflegt.
* [x] Bestehende Update-Funktionen aus Issue 037 bleiben unverändert nutzbar.

## Testhinweise

* Update verfügbar mit kurzen Release Notes.
* Update verfügbar mit langen Release Notes.
* GitHub-Release-Notes-Button öffnet die Release-Seite.
* Download erfolgreich.
* Installation öffnen.
* Download- und Installationsbuttons sind sichtbar und je nach Zustand deaktiviert.
* App-Version ist aktuell.
* Deutsch und Englisch prüfen.
