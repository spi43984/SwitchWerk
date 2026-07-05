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

## Akzeptanzkriterien

- [ ] SwitchWerk stellt Android-Homescreen-Widgets bereit.
- [ ] Ein `1x1`-Widget kann eine Geräteaktion starten.
- [ ] Ein `1x1`-Widget kann eine Gruppenaktion starten.
- [ ] `2x1` oder `1x2` kann zwei Aktionen kombiniert darstellen.
- [ ] Vergrößerte Widgets können mehrere Geräte- oder Gruppenaktionen anzeigen.
- [ ] `1x...`-Widgets ordnen Einträge untereinander an.
- [ ] `2x...`-Widgets ordnen Einträge zweispaltig mit Einträgen untereinander an.
- [ ] Widget-Einträge können mit lokal konfigurierten Geräten und Gruppen verknüpft werden.
- [ ] Widget-Klicks starten ausschließlich lokal konfigurierte Aktionen.
- [ ] Es ist geprüft und dokumentiert, ob die Aktion ohne sichtbares Öffnen der App ausgeführt werden kann.
- [ ] Fortschritt, Erfolg und Fehler werden nachvollziehbar behandelt.
- [ ] Umbenannte oder gelöschte Geräte und Gruppen werden in Widgets sicher aktualisiert oder entfernt.
- [ ] Leere oder nicht ausführbare Gruppen werden nicht als ausführbare Widget-Aktion angeboten.
- [ ] Keine sensiblen Daten werden in Widget-Labels, PendingIntents, Extras, Logs oder Dokumentation gespeichert.
- [ ] Deutsche und englische Texte sind konsistent gepflegt.
- [ ] Hilfe-, Info- und Tooltip-Texte wurden geprüft und bei Bedarf aktualisiert.

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
