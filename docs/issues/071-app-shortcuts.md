# Issue 071: App Shortcuts

## Metadaten

- Status: Offen
- Priorität: P1
- Typ: Feature / UX
- Bereich: Android App Shortcuts / Dashboard / Geräteaktionen

## Ziel

SwitchWerk soll ausgewählte Geräteaktionen als Android App Shortcuts anbieten, damit häufig genutzte Aktionen direkt über langes Drücken auf das App-Icon gestartet werden können.

## Hintergrund

Viele SwitchWerk-Aktionen sind kurze, wiederkehrende Schaltvorgänge. Anwender sollen dafür nicht jedes Mal erst die App öffnen, das Gerät suchen und die Aktion auswählen müssen.

Android App Shortcuts bieten dafür einen einfachen Einstieg, ohne Cloud, Account oder externe Dienste.

## Scope

### Shortcut-Funktion

- Häufig oder explizit ausgewählte Geräteaktionen als App Shortcut bereitstellen.
- Ein Shortcut startet genau eine Geräteaktion.
- Der Shortcut öffnet SwitchWerk und führt die Aktion über die bestehende Geräteaktionslogik aus.
- Fehler, Fortschritt und Ergebnis werden wie bei normalen Geräteaktionen angezeigt.

### Auswahl

- Pro Gerät soll konfigurierbar sein, ob eine Aktion als Shortcut angeboten wird.
- Die Anzahl der dynamischen Shortcuts soll bewusst klein bleiben.
- Wenn Android die Anzahl begrenzt, werden nur die wichtigsten Shortcuts veröffentlicht.

### Verhalten

- Shortcuts müssen bei Umbenennung oder Löschung von Geräten aktualisiert beziehungsweise entfernt werden.
- Shortcuts dürfen keine Passwörter, Tokens oder vollständige technische URLs enthalten.
- Shortcuts dürfen keine neue Netzwerklogik implementieren.

### Texte

- Deutsche und englische Texte für Shortcut-Beschriftungen pflegen.
- Hilfe- oder Info-Texte ergänzen, falls die Funktion in der UI erklärungsbedürftig ist.

## Nicht im Scope

- Keine Homescreen-Widgets.
- Keine Quick Settings Tiles.
- Keine Deep Links.
- Keine externen Automatisierungs-Integrationen.
- Keine Cloud- oder Account-Funktion.

## Architekturhinweise

- Bestehende MVVM- und Repository-Struktur beibehalten.
- Geräteaktionen weiterhin ausschließlich über bestehende Use-Cases, ViewModels oder Repositories ausführen.
- Keine HTTP-, RPC- oder WLAN-Logik in Activity-, Intent- oder Shortcut-Code duplizieren.
- Shortcut-Erzeugung und Shortcut-Aktualisierung klar kapseln.
- Keine sensiblen Daten in Shortcut-Labels, Intent-Extras, Logs oder Dokumentation schreiben.

## Akzeptanzkriterien

- [ ] Für ausgewählte Geräteaktionen werden Android App Shortcuts angelegt.
- [ ] Ein Shortcut startet die passende bestehende Geräteaktion.
- [ ] Fortschritt, Erfolg und Fehler werden für Shortcut-Aktionen verständlich angezeigt.
- [ ] Umbenannte Geräte aktualisieren ihre Shortcut-Beschriftung.
- [ ] Gelöschte Geräte entfernen zugehörige Shortcuts.
- [ ] Die Anzahl veröffentlichter Shortcuts bleibt Android-kompatibel begrenzt.
- [ ] Keine sensiblen Daten werden in Shortcuts, Logs oder sichtbaren technischen Daten gespeichert.
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

- Gerät als Shortcut markieren.
- App-Icon lange drücken und Shortcut prüfen.
- Shortcut ausführen und Erfolg prüfen.
- Shortcut ausführen, wenn Zielgerät nicht erreichbar ist.
- Gerät umbenennen und Shortcut-Beschriftung prüfen.
- Gerät löschen und prüfen, dass der Shortcut entfernt wird.
- App neu starten und Shortcut-Funktion erneut prüfen.
