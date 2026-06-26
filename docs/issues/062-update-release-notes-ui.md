# Issue 062: Update Release Notes UI

## Metadaten

* Status: Offen
* Priorität: P1
* Typ: UX / Feature
* Bereich: Release / Updates

## Ziel

Der Update-Bereich in den Einstellungen soll Release Notes platzsparender und bedienbarer darstellen.

Anwender sollen die GitHub-Release-Seite über einen klaren Button öffnen können, statt einen Link aus den Release Notes kopieren oder erkennen zu müssen. Lange Release Notes sollen einklappbar sein, damit der Update-Bereich nach Download oder Installation nicht dauerhaft viel Platz belegt.

## Hintergrund

Issue 037 hat GitHub Release Update Support eingeführt. Die App zeigt Release Notes derzeit als normalen Text im Update-Bereich. Links im Text sind nicht interaktiv, und lange automatisch oder manuell gepflegte Release Notes können den Systembereich der Einstellungen stark verlängern.

Ein dedizierter Button und einklappbare Release Notes verbessern die Bedienbarkeit auf kleinen Displays und halten die Einstellungen übersichtlich.

## Scope

### GitHub-Release-Button

* Im Update-Bereich wird ein Button `GitHub Release öffnen` angezeigt, wenn eine verfügbare Release-URL vorhanden ist.
* Der Button öffnet die GitHub-Release-Seite der angezeigten verfügbaren Version.
* Der Button verwendet die bereits vorhandene Release-URL aus dem Update-Status.
* Es wird keine Markdown-Link-Erkennung und kein Parsing von Release-Notes-Text eingeführt.

### Einklappbare Release Notes

* Release Notes können ein- und ausgeklappt werden.
* Lange Release Notes verschwenden im eingeklappten Zustand keinen Platz.
* Der eingeklappte Zustand muss klar erkennbar und wieder umkehrbar sein.
* Die Bedienung bleibt auch mit langen Release Notes flüssig und scrollbar.

### Zustand nach Download / Installation

* Nach erfolgreichem Download oder nach Start des Android-Installationsdialogs werden Release Notes automatisch eingeklappt.
* Wenn die installierte Version aktuell ist, sollen Release Notes standardmäßig platzsparend eingeklappt bleiben.
* Eine manuelle erneute Update-Prüfung darf die Release Notes wieder sinnvoll anzeigen, wenn ein Update verfügbar ist.

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

* [ ] Der Update-Bereich zeigt bei vorhandener Release-URL einen Button `GitHub Release öffnen`.
* [ ] Der Button öffnet die GitHub-Release-Seite im Browser.
* [ ] Release Notes sind ein- und ausklappbar.
* [ ] Eingeklappte Release Notes belegen nur wenig Platz.
* [ ] Nach erfolgreichem Download werden Release Notes eingeklappt.
* [ ] Nach Start des Installationsdialogs werden Release Notes eingeklappt.
* [ ] Bei aktueller installierter Version bleiben Release Notes standardmäßig eingeklappt.
* [ ] Lange Release Notes blockieren die Bedienung nicht.
* [ ] Es wird kein Markdown-Link-Parser eingeführt.
* [ ] Hilfe-, Info- und Tooltip-Texte wurden geprüft und bei Bedarf aktualisiert.
* [ ] Deutsch und Englisch sind konsistent gepflegt.
* [ ] Bestehende Update-Funktionen aus Issue 037 bleiben unverändert nutzbar.

## Testhinweise

* Update verfügbar mit kurzen Release Notes.
* Update verfügbar mit langen Release Notes.
* Release Notes ein- und ausklappen.
* GitHub-Release-Button öffnet die Release-Seite.
* Download erfolgreich.
* Nach Download sind Release Notes eingeklappt.
* Installation öffnen.
* Nach Start des Installationsdialogs sind Release Notes eingeklappt.
* App-Version ist aktuell.
* Manuelle Update-Prüfung nach eingeklapptem Zustand.
* Deutsch und Englisch prüfen.
