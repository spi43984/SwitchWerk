# Issue 079: Schaltgruppen App Shortcuts

## Metadaten

- Status: Abgeschlossen
- Priorität: P1
- Typ: Feature / UX
- Bereich: Android App Shortcuts / Schaltgruppen / Dashboard

## Ziel

SwitchWerk soll ausgewählte Schaltgruppen als Android App Shortcuts anbieten,
damit häufig genutzte Gruppenaktionen direkt über langes Drücken auf das
App-Icon gestartet werden können. Zusätzlich sollen Schaltgruppen über die
bestehende externe Intent-Freigabe per lokaler Gruppen-ID gestartet werden
können.

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
- Leere Schaltgruppen dürfen auch per externem Gruppen-Intent nicht gestartet
  werden.
- Shortcuts dürfen keine technischen Geräte-URLs, Zugangsdaten, privaten
  Hostnamen oder sonstigen sensiblen Daten enthalten.
- Shortcuts und Gruppen-Intents dürfen keine neue Netzwerk-, HTTP-, RPC- oder
  WLAN-Logik implementieren.

### Externe Intents

- Bei aktivierter externer Intent-Freigabe sollen Schaltgruppen per eigener
  Action und lokaler Gruppen-ID gestartet werden können.
- Der Gruppen-Intent akzeptiert ausschließlich die lokale Gruppen-ID.
- Fehlende, ungültige, unbekannte oder leere Gruppen werden sauber abgelehnt.
- Zusätzliche Extras, URLs, Befehle, Request-Bodies, Hostnamen und sonstige
  Steuerdaten werden abgelehnt.
- Die Ausführung nutzt ausschließlich die bestehende Schaltgruppen-Ausführung.

### Texte

- Deutsche und englische Texte für Gruppen-Shortcuts pflegen.
- Hilfe-, Info- und Tooltip-Texte prüfen und bei Bedarf aktualisieren.

## Nicht im Scope

- Keine Quick Settings Tiles.
- Keine Homescreen-Widgets.
- Keine verschachtelten Gruppen.
- Keine externen Automatisierungs-Integrationen über die lokale Gruppen-ID
  hinaus.
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
- Externe Intent-Actions und Extras müssen Geräte und Gruppen eindeutig trennen.
- Import/Export prüfen, wenn eine neue Shortcut-Auswahl an Schaltgruppen
  gespeichert wird.

## Akzeptanzkriterien

- [x] Für ausgewählte Schaltgruppen werden Android App Shortcuts angelegt.
- [x] Ein Gruppen-Shortcut startet die passende bestehende Schaltgruppe.
- [x] Fortschritt, Erfolg, Abbruch und Fehler werden verständlich im Dashboard
      angezeigt.
- [x] Umbenannte Schaltgruppen aktualisieren ihre Shortcut-Beschriftung.
- [x] Gelöschte Schaltgruppen entfernen zugehörige Shortcuts.
- [x] Leere Schaltgruppen werden nicht als ausführbare Shortcuts veröffentlicht.
- [x] Bei aktivierter externer Intent-Freigabe startet ein gültiger
      Gruppen-Intent die passende bestehende Schaltgruppe.
- [x] Externe Gruppen-Intents mit fehlender, ungültiger, unbekannter oder leerer
      Gruppe werden sauber abgelehnt.
- [x] Geräte- und Gruppen-Shortcuts teilen sich die Android-kompatible
      Shortcut-Begrenzung.
- [x] Keine sensiblen Daten werden in Shortcut-Labels, Intent-Extras, Logs oder
      Dokumentation gespeichert.
- [x] Deutsche und englische Texte sind konsistent gepflegt.
- [x] Hilfe-, Info- und Tooltip-Texte wurden geprüft und bei Bedarf aktualisiert.

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
- Externen Gruppen-Intent mit gültiger Gruppen-ID ausführen.
- Externen Gruppen-Intent ohne Gruppen-ID ausführen und Fehlermeldung prüfen.
- Externen Gruppen-Intent mit unbekannter Gruppen-ID ausführen und Fehlermeldung
  prüfen.
- Externen Gruppen-Intent mit leerer Schaltgruppe ausführen und Fehlermeldung
  prüfen.
