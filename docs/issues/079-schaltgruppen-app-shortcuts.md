# Issue 079: Schaltgruppen App Shortcuts

## Metadaten

- Status: Offen
- Priorität: P1
- Typ: Feature / UX
- Bereich: Android App Shortcuts / Schaltgruppen / Dashboard

## Ziel

SwitchWerk soll ausgewählte Schaltgruppen als Android App Shortcuts anbieten,
damit häufig genutzte Gruppenaktionen direkt über langes Drücken auf das
App-Icon gestartet werden können.

## Hintergrund

Issue 071 unterstützt App Shortcuts für einzelne Geräteaktionen. Schaltgruppen
werden mit Issue 074 als eigene ausführbare Dashboard-Einträge eingeführt und
nicht als normale Geräte gespeichert. Deshalb müssen Gruppen-Shortcuts bewusst
in die bestehende Shortcut-Logik integriert werden.

## Scope

### Shortcut-Funktion

- Ausgewählte Schaltgruppen als Android App Shortcuts bereitstellen.
- Ein Shortcut startet genau eine Schaltgruppe.
- Der Shortcut öffnet SwitchWerk und führt die Gruppe über die bestehende
  Schaltgruppen-Ausführung aus.
- Fortschritt, Erfolg, Abbruch und Fehler werden wie bei normalen
  Gruppenaktionen im Dashboard angezeigt.

### Auswahl und Reihenfolge

- Pro Schaltgruppe soll konfigurierbar sein, ob sie als Shortcut angeboten wird.
- Geräte-Shortcuts und Gruppen-Shortcuts teilen sich die Android-Begrenzung für
  dynamische Shortcuts.
- Die Auswahl soll sich an der gemeinsamen Dashboard-Reihenfolge orientieren.

### Verhalten

- Shortcuts müssen bei Umbenennung oder Löschung von Schaltgruppen aktualisiert
  beziehungsweise entfernt werden.
- Leere Schaltgruppen dürfen nicht als ausführbarer Shortcut angeboten werden.
- Shortcuts dürfen keine technischen Geräte-URLs, Zugangsdaten, privaten
  Hostnamen oder sonstigen sensiblen Daten enthalten.
- Shortcuts dürfen keine neue Netzwerk-, HTTP-, RPC- oder WLAN-Logik
  implementieren.

### Texte

- Deutsche und englische Texte für Gruppen-Shortcuts pflegen.
- Hilfe-, Info- und Tooltip-Texte prüfen und bei Bedarf aktualisieren.

## Nicht im Scope

- Keine Quick Settings Tiles.
- Keine Homescreen-Widgets.
- Keine externen Automatisierungs-Integrationen.
- Keine verschachtelten Gruppen.
- Keine neuen Intent-Funktionen für externe Apps außerhalb der internen
  App-Shortcut-Ausführung.
- Keine Cloud- oder Account-Funktion.

## Architekturhinweise

- Bestehende MVVM-, Repository- und Koin-Struktur beibehalten.
- Bestehende Shortcut-Erzeugung erweitern, nicht duplizieren.
- Für Shortcuts ein gemeinsames Modell für Geräte- und Gruppenaktionen
  verwenden oder die bestehende Auswahl sauber erweitern.
- Gruppen-Ausführung ausschließlich über den Schaltgruppen-Service starten, der
  intern die bestehende Geräteaktionslogik nutzt.
- Keine Netzwerklogik in Activity-, Intent-, Shortcut- oder Compose-Code
  einführen.
- Shortcut-IDs müssen Geräte und Gruppen eindeutig unterscheiden, z. B. durch
  unterschiedliche Präfixe.
- Import/Export prüfen, wenn eine neue Shortcut-Auswahl an Schaltgruppen
  gespeichert wird.

## Akzeptanzkriterien

- [ ] Für ausgewählte Schaltgruppen werden Android App Shortcuts angelegt.
- [ ] Ein Gruppen-Shortcut startet die passende bestehende Schaltgruppe.
- [ ] Fortschritt, Erfolg, Abbruch und Fehler werden verständlich im Dashboard
      angezeigt.
- [ ] Umbenannte Schaltgruppen aktualisieren ihre Shortcut-Beschriftung.
- [ ] Gelöschte Schaltgruppen entfernen zugehörige Shortcuts.
- [ ] Leere Schaltgruppen werden nicht als ausführbare Shortcuts veröffentlicht.
- [ ] Geräte- und Gruppen-Shortcuts teilen sich die Android-kompatible
      Shortcut-Begrenzung.
- [ ] Keine sensiblen Daten werden in Shortcut-Labels, Intent-Extras, Logs oder
      Dokumentation gespeichert.
- [ ] Deutsche und englische Texte sind konsistent gepflegt.
- [ ] Hilfe-, Info- und Tooltip-Texte wurden geprüft und bei Bedarf aktualisiert.

## Testhinweise

Die Host-Prüfung passend zur aktuell installierten App-Variante ausführen.

Debug:

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

- Schaltgruppe als Shortcut markieren.
- App-Icon lange drücken und Gruppen-Shortcut prüfen.
- Gruppen-Shortcut ausführen und Erfolg prüfen.
- Gruppen-Shortcut ausführen, wenn ein enthaltenes Gerät nicht erreichbar ist.
- Schaltgruppe umbenennen und Shortcut-Beschriftung prüfen.
- Schaltgruppe leeren und prüfen, dass kein ausführbarer Shortcut veröffentlicht
  wird.
- Schaltgruppe löschen und prüfen, dass der Shortcut entfernt wird.
