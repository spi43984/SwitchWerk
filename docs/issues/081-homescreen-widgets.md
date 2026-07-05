# Issue 081: Homescreen Widgets

## Metadaten

- Status: Offen
- Priorität: P1
- Typ: Feature / Android Integration / UX
- Bereich: Android Homescreen Widgets / Geräteaktionen / Schaltgruppen

## Ziel

SwitchWerk soll Android-Homescreen-Widgets bereitstellen, damit ausgewählte Geräte- oder Gruppenaktionen direkt vom Android-Startbildschirm ausgelöst werden können.

## Hintergrund

App Shortcuts und externe Intents decken bereits direkte Startwege ab, erfordern aber andere Android-Oberflächen. Homescreen-Widgets sollen häufig genutzte Einzel- oder Gruppenaktionen dauerhaft sichtbar und direkt antippbar auf dem Startbildschirm anbieten.

Wichtig ist zu prüfen, ob eine Aktion zuverlässig ausgelöst werden kann, ohne SwitchWerk sichtbar im Vordergrund zu öffnen.

## Scope

### Widget-Größen und Layout

- Android-Homescreen-Widgets in unterschiedlichen Größen bereitstellen.
- Größe `1x1` für genau eine Aktion eines Geräts oder einer Gruppe unterstützen.
- Größen `2x1` und `1x2` für eine kombinierte Darstellung von zwei Aktionen unterstützen.
- Vergrößerte Varianten von `2x1` beziehungsweise `1x2` sollen mehrere Geräte- oder Gruppenaktionen anzeigen können.
- Bei `1x...` werden Aktionen untereinander dargestellt.
- Bei `2x...` werden Aktionen in zwei Spalten dargestellt; innerhalb jeder Spalte stehen die Einträge untereinander.
- Größere Widget-Flächen sollen die verfügbare Fläche sinnvoll nutzen, ohne freie Drag-and-Drop-Positionierung einzuführen.

### Verknüpfung mit Aktionen

- Widgets können mit lokal konfigurierten Geräten verknüpft werden.
- Widgets können mit lokal konfigurierten Schaltgruppen verknüpft werden.
- Ein Widget-Eintrag startet genau eine lokale SwitchWerk-Aktion.
- Leere oder nicht ausführbare Schaltgruppen dürfen nicht als ausführbare Widget-Aktion angeboten werden.
- Gelöschte oder umbenannte Geräte und Gruppen müssen in Widgets sicher behandelt beziehungsweise aktualisiert werden.

### Ausführung

- Prüfen und umsetzen, ob ein Widget-Klick eine Aktion auslösen kann, ohne SwitchWerk im Vordergrund zu öffnen.
- Falls Android oder die bestehende App-Architektur ein vollständiges unsichtbares Ausführen nicht zuverlässig zulässt, muss das Verhalten klar dokumentiert und für Anwender verständlich bleiben.
- Die Ausführung muss dieselbe bestehende Geräte- beziehungsweise Gruppenaktionslogik verwenden wie Dashboard, App Shortcuts und externe Intents.
- Fortschritt, Erfolg und Fehler müssen nachvollziehbar behandelt werden, auch wenn die App nicht sichtbar geöffnet wird.

### Konfiguration

- Eine einfache Konfiguration für Widget-Inhalte bereitstellen.
- Geräte- und Gruppenaktionen auswählbar machen.
- Mehrere Einträge je Widget ermöglichen, soweit die Widget-Größe dies sinnvoll zulässt.
- Änderungen an Namen, Reihenfolge oder Löschung lokal konfigurierter Geräte und Gruppen berücksichtigen.
- Prüfen und im Issue-Scope dokumentieren, ob neue Widget-Zuordnungen Teil von Konfigurationsexport und -import werden sollen.

### Texte

- Deutsche und englische Texte für Widget-Konfiguration, leere Zustände, Fehler und Hilfe pflegen.
- Hilfe-, Info- und Tooltip-Texte prüfen und bei Bedarf aktualisieren.

## Nicht im Scope

- Keine Quick Settings Tiles.
- Keine neuen App-Shortcut-Funktionen.
- Keine neue externe Automatisierungs-API.
- Keine Cloud-, Account-, Tracking- oder Synchronisationsfunktion.
- Keine freie Positionierung einzelner Widget-Einträge innerhalb eines Widgets.
- Keine neue WLAN-, HTTP- oder RPC-Sonderlogik.
- Keine zeitgesteuerten Automationen.
- Keine verschachtelten Gruppen.

## Architekturhinweise

- Bestehende MVVM-, Repository-, Room- und Koin-Struktur beibehalten.
- Android-AppWidget-Code nur als Einstieg und Darstellungsintegration verwenden.
- Keine Netzwerk-, HTTP-, RPC- oder WLAN-Logik direkt im Widget-Code implementieren.
- Widget-Aktionen an die bestehende Geräte- beziehungsweise Gruppenaktionslogik delegieren.
- Widget-IDs und referenzierte Aktionen müssen Geräte und Gruppen eindeutig unterscheiden.
- Widget-RemoteViews beziehungsweise Glance-Ansatz bewusst prüfen und die im Projekt einfachste stabile Variante wählen.
- Keine sensiblen Daten in Widget-Labels, PendingIntents, Extras, Logs oder Dokumentation speichern.
- Bei Hintergrundausführung Android-Versionen, PendingIntent-Sicherheit und mögliche Foreground-Service-Anforderungen prüfen.
- Import/Export ausdrücklich prüfen, wenn Widget-Zuordnungen lokal gespeichert werden.
- Bestehende App-Shortcuts und externe Intents nicht duplizieren, sondern gemeinsame Aktionsmodelle oder klare Delegation nutzen.

## Implementierungsnotiz

- Architekturentscheidung: klassische Android AppWidget/RemoteViews mit einer
  Compose-Konfigurations-Activity.
- Widget-Klicks starten einen nicht exportierten lokalen Service, der nur
  Widget-ID und Eintragsindex entgegennimmt und die Zielaktion anschließend aus
  der lokalen Widget-Zuordnung auflöst.
- Ausführung erfolgt ausschließlich über bestehende `DeviceActionService`- und
  `SwitchGroupActionService`-Logik.
- Widget-Zuordnungen werden in SharedPreferences gespeichert, nicht in Room.
  Eine Room-Migration ist deshalb nicht nötig.
- Widget-Zuordnungen werden nicht in Konfigurationsimport/-export aufgenommen,
  weil Android-AppWidget-IDs launcher- und gerätelokal sind.
- Leere Schaltgruppen werden in der Widget-Konfiguration nicht als ausführbare
  Aktion angeboten und bei späterer Leerung im Widget als nicht verfügbar
  dargestellt.
- Die Widget-Auswahl stellt eigene Launcher-Varianten für `1x1`, `1x2` und
  `2x1` mit passenden Android-Zellmaßen bereit. Kompatible Launcher öffnen die
  Zuordnung später über ihre native Bearbeiten-Aktion nach langem Drücken.
- Die Render-Kapazität nutzt die aktuelle AppWidget-Größe, damit nach einer
  Vergrößerung zusätzlich ausgewählte Aktionen angezeigt werden.
- Widgets können einen optionalen freien Titel speichern. Ein leeres Feld
  blendet den Titel aus und gibt die Höhe den Aktionsbuttons; eine kompakte
  Zurücksetzen-Schaltfläche stellt den Standardtitel `SwitchWerk` wieder her.
  Bei der erstmaligen Einrichtung ist `SwitchWerk` vorausgefüllt.
- Die Konfiguration bietet eine Layoutwahl zwischen automatisch, einer Spalte
  und zwei Spalten. Damit können Aktionen in ausreichend hohen Widgets bewusst
  als breite Buttons untereinander dargestellt werden.
- Ausgewählte Aktionen zeigen statt eines Hakens ihre laufende Auswahlnummer;
  diese Nummer entspricht der späteren Reihenfolge im Widget.
- Das Widget ist als nativ neu konfigurierbar markiert. Kompatible Launcher
  bieten die Bearbeitung nach langem Drücken im Widget-Kontextmenü an; ein
  dauerhaftes Zahnrad im Widget ist nicht nötig. Speichern und Abbrechen
  schließen immer den Android-Konfigurationsvertrag per Activity-Ergebnis ab.
  Ein noch sichtbares Launcher-Kontextmenü kann SwitchWerk mangels Android-API
  nicht zuverlässig selbst schließen. Die Konfigurations-Activity verwendet
  keine eigene Task-Zuordnung oder abweichende Task-Flags, damit der Launcher
  den Standard-Lebenszyklus vollständig kontrolliert.
- In der physischen Größe `1x1` wird der nicht sinnvoll lesbare Widget-Titel
  ausgeblendet, damit die einzelne Aktionsfläche den verfügbaren Platz nutzt.
- Ab Android 12 verwenden Widget-Hintergrund und Aktionsbuttons die systemseitig
  vorgegebenen äußeren und inneren Widget-Radien. Ältere Android-Versionen
  behalten die kompatiblen festen Radien.
- Die vertikalen Abstände der Aktionsbuttons sind oben und unten symmetrisch;
  dadurch sitzt der Aktionsbereich insbesondere ohne Titel mittig im Rahmen.
- Widget-Aktionsflächen übernehmen die Dashboard-nahe WLAN-Statusfarbe
  grün/grau/rot. Erfolg oder Fehler einer Widget-Aktion wird für vier Sekunden
  in einem helleren Grün beziehungsweise Rot hervorgehoben und danach wieder
  auf den aktuellen Status gesetzt.
- Widget-Aktionen werden als Foreground-Service gestartet, damit Android den
  Start aus dem Homescreen-Widget zuverlässig als Nutzeraktion behandelt.

## Akzeptanzkriterien

- [x] SwitchWerk stellt Android-Homescreen-Widgets bereit.
- [x] Ein `1x1`-Widget kann eine Geräteaktion starten.
- [x] Ein `1x1`-Widget kann eine Gruppenaktion starten.
- [x] `2x1` oder `1x2` kann zwei Aktionen kombiniert darstellen.
- [x] Vergrößerte Widgets können mehrere Geräte- oder Gruppenaktionen anzeigen.
- [x] `1x...`-Widgets ordnen Einträge untereinander an.
- [x] `2x...`-Widgets ordnen Einträge zweispaltig mit Einträgen untereinander an.
- [x] Widget-Einträge können mit lokal konfigurierten Geräten und Gruppen verknüpft werden.
- [x] Widget-Klicks starten ausschließlich lokal konfigurierte Aktionen.
- [x] Es ist geprüft und dokumentiert, ob die Aktion ohne sichtbares Öffnen der App ausgeführt werden kann.
- [x] Fortschritt, Erfolg und Fehler werden nachvollziehbar behandelt.
- [x] Umbenannte oder gelöschte Geräte und Gruppen werden in Widgets sicher aktualisiert oder entfernt.
- [x] Leere oder nicht ausführbare Gruppen werden nicht als ausführbare Widget-Aktion angeboten.
- [x] Keine sensiblen Daten werden in Widget-Labels, PendingIntents, Extras, Logs oder Dokumentation gespeichert.
- [x] Deutsche und englische Texte sind konsistent gepflegt.
- [x] Hilfe-, Info- und Tooltip-Texte wurden geprüft und bei Bedarf aktualisiert.

## Abschlussprüfung

- Container: `./gradlew testDebugUnitTest` erfolgreich.
- Container: `./gradlew lintDebug` erfolgreich.
- Iterative Release-Installationen und manuelle Gerätetests für Größen,
  Skalierung, Konfiguration, Ausführung, Statusfarben und Layout durchgeführt.
- Bekannte Plattformgrenze: Ein nach der Neukonfiguration sichtbar bleibendes
  Launcher-Kontextmenü kann SwitchWerk nicht über eine öffentliche Android-API
  schließen.
- Folge-Thema vorgemerkt: Widget-Aktionen schreiben derzeit nicht in die
  Dashboard-Aktionsdetails. Eine gemeinsame Aktionshistorie für Dashboard,
  Widgets, Shortcuts und Intents soll als separates Issue geplant werden.

## Testhinweise

- Widget in Größe `1x1` zum Android-Startbildschirm hinzufügen.
- Einzelne Geräteaktion mit dem Widget verknüpfen und ausführen.
- Einzelne Gruppenaktion mit dem Widget verknüpfen und ausführen.
- Widget in Größe `2x1` testen.
- Widget in Größe `1x2` testen.
- Vergrößerte Widget-Varianten mit mehreren Einträgen testen.
- Layout für `1x...` mit vertikaler Anordnung prüfen.
- Layout für `2x...` mit zweispaltiger Anordnung prüfen.
- Verhalten bei umbenannten Geräten und Gruppen prüfen.
- Verhalten bei gelöschten Geräten und Gruppen prüfen.
- Verhalten bei leerer Schaltgruppe prüfen.
- Verhalten bei nicht erreichbarem Zielgerät prüfen.
- Prüfen, ob Widget-Aktion ohne sichtbares Öffnen von SwitchWerk ausgeführt wird.
- App-Neustart und Geräte-Neustart mit vorhandenen Widgets prüfen.
- Export-/Import-Entscheidung für Widget-Zuordnungen prüfen und dokumentieren.
