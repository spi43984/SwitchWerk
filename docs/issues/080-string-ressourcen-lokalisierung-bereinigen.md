# Issue 080: String-Ressourcen und Lokalisierung bereinigen

## Metadaten

- Status: Offen
- Priorität: P0
- Typ: Wartung / Lokalisierung
- Bereich: Android-Ressourcen / Deutsch / Englisch

## Ziel

Die String-Ressourcen von SwitchWerk sollen wieder einen vollständigen und
eindeutigen deutschen und englischen Schlüsselsatz besitzen. Die
Default-Ressourcen sollen konsequent als englischer Fallback dienen, damit eine
fehlende sprachspezifische Ressource nicht unbemerkt Text in der falschen
Sprache anzeigt.

## Hintergrund

Issue 029 dokumentiert einen identischen vollständigen Schlüsselsatz für
Default-, deutsche und englische Ressourcen. Dieser Zustand ist inzwischen
regressiert:

- `values-en/strings.xml` fehlen 18 vorhandene `setup_wizard_*`-Schlüssel.
  Android verwendet dafür derzeit die englischen Texte aus
  `values/strings.xml`.
- `values-de/strings.xml` fehlen die Schlüssel
  `dashboard_layout_diagnostics`, `dashboard_layout_selector_visible` und
  `dashboard_layout_selector_hidden`.
- Die drei fehlenden deutschen Dashboard-Texte stehen aktuell deutsch in
  `values/strings.xml`. Dadurch sind die Default-Ressourcen nicht konsequent
  englisch.
- `tools:ignore="MissingTranslation"` unterdrückt derzeit die entsprechende
  Lint-Prüfung global.

Die App zeigt dadurch aktuell nicht zwingend falsche Texte an, die Struktur ist
aber wartungsanfällig und kann weitere fehlende Übersetzungen verdecken.

## Scope

### Ressourcen-Parität

- Alle in `values/strings.xml` vorhandenen String-Schlüssel explizit in
  `values-en/strings.xml` und `values-de/strings.xml` pflegen.
- Die 18 fehlenden Setup-Wizard-Texte in `values-en/strings.xml` ergänzen.
- Die drei fehlenden Dashboard-Layout-Diagnosetexte in
  `values-de/strings.xml` ergänzen.
- Platzhalter, Formatangaben, Escape-Sequenzen und `translatable`-Attribute
  zwischen den Sprachvarianten konsistent halten.

### Default-Sprache

- `values/strings.xml` konsequent als englischen Fallback führen.
- Die drei deutschen Dashboard-Layout-Diagnosetexte im Default-Verzeichnis
  durch die passenden englischen Texte ersetzen.
- Keine Änderung der auswählbaren App-Sprachen oder der bestehenden
  Sprachumschaltung.

### Prüfung

- Einen kleinen automatisierbaren Test oder eine gleichwertige stabile Prüfung
  ergänzen, die fehlende Schlüssel in `values-en` und `values-de` erkennt.
- Prüfen, ob `tools:ignore="MissingTranslation"` vollständig entfernt werden
  kann. Falls es für bewusst nicht übersetzbare Ressourcen erforderlich bleibt,
  die Unterdrückung so eng wie möglich begrenzen und begründen.
- Sichtbare Setup-Wizard- und Dashboard-Diagnosetexte in Deutsch und Englisch
  auf sprachliche Konsistenz prüfen.

## Nicht im Scope

- Keine neue Sprache.
- Keine Änderung der Sprachwahl oder Persistenz.
- Keine funktionale Änderung am Setup-Wizard oder Dashboard.
- Keine allgemeine Umformulierung fachlich unabhängiger UI-Texte.
- Keine externe Übersetzungsbibliothek oder Cloud-Abhängigkeit.

## Architekturhinweise

- Android-Standardressourcen weiterverwenden.
- `values/strings.xml` bleibt der englische Default-Fallback.
- `values-en/strings.xml` und `values-de/strings.xml` enthalten jeweils den
  vollständigen Satz übersetzbarer String-Schlüssel.
- Der Paritätscheck soll XML strukturiert auswerten und nicht von der
  Dateireihenfolge abhängen.
- Bewusst nicht übersetzbare Ressourcen müssen explizit gekennzeichnet sein und
  dürfen den Paritätscheck nicht pauschal deaktivieren.

## Import/Export

Nicht betroffen. Es werden keine Einstellungen oder Konfigurationsdaten
hinzugefügt oder geändert.

## Hilfe-, Info- und Tooltip-Texte

Keine neuen Funktionen. Betroffene sichtbare Texte des Setup-Wizards und der
Dashboard-Diagnose werden in beiden Sprachen auf Konsistenz geprüft; andere
Hilfe-, Info- und Tooltip-Texte bleiben unverändert.

## Akzeptanzkriterien

- [ ] `values`, `values-en` und `values-de` besitzen denselben Satz
      übersetzbarer String-Schlüssel.
- [ ] Alle 18 Setup-Wizard-Schlüssel sind explizit in `values-en` vorhanden.
- [ ] Die drei Dashboard-Layout-Diagnoseschlüssel sind explizit in `values-de`
      vorhanden.
- [ ] `values/strings.xml` enthält für die betroffenen Ressourcen nur englische
      Fallback-Texte.
- [ ] Platzhalter und Formatangaben stimmen für jeden Schlüssel zwischen den
      Sprachvarianten überein.
- [ ] Eine automatisierbare Prüfung erkennt zukünftig fehlende deutsche oder
      englische String-Ressourcen.
- [ ] `MissingTranslation` wird nicht mehr pauschal unterdrückt oder eine
      verbleibende eng begrenzte Ausnahme ist dokumentiert.
- [ ] Setup-Wizard und Dashboard-Diagnosetexte wurden auf Deutsch und Englisch
      manuell geprüft.
- [ ] Es gibt keine funktionalen Änderungen an Setup-Wizard, Dashboard oder
      Sprachumschaltung.

## Testhinweise

- Automatisierten Ressourcen-Paritätscheck ausführen.
- App nacheinander auf Deutsch und Englisch stellen.
- Setup-Wizard in beiden Sprachen vollständig anzeigen.
- Dashboard-Layout-Diagnose in beiden Sprachen anzeigen.
- Platzhalterdarstellung der Dashboard-Diagnose prüfen.

Host-Prüfung für Debug:

```text
./gradlew lintDebug
./gradlew testDebugUnitTest
./gradlew clean assembleDebug
./gradlew installDebug
```

Release bei konfigurierter Release-Signierung:

```text
./gradlew lintRelease
./gradlew testDebugUnitTest
./gradlew clean assembleRelease
./gradlew installRelease
```

`testReleaseUnitTest` existiert in diesem Projekt nicht. Bei abweichenden
Signaturen erfordert der Variantenwechsel eine Deinstallation mit Verlust der
lokalen App-Daten.
