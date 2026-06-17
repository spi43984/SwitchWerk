# Issue #16: Edit Items by Name Click and Swipe Actions

Status: abgeschlossen

GitHub-Issue: #37

## Ziel

Geräte und WLAN-Profile ohne dauerhaft sichtbare Aktionsbuttons bearbeiten und
löschen.

Kurzer Klick auf den Namen bzw. Listeneintrag in den Einstellungen öffnet die
Bearbeitung, solange kein Swipe-Eintrag offen ist. Zusätzlich legt seitliches
Wischen Aktionen frei. Der Swipe selbst führt keine Aktion aus. Bearbeiten oder
Löschen erfolgt erst nach zusätzlichem Tipp auf das freigelegte Symbol.

## Umgesetzter Scope

- Kurzer Klick auf Gerätenamen in den Einstellungen öffnet Gerätebearbeitung
- Kurzer Klick auf WLAN-Eintrag öffnet WLAN-Bearbeitung
- Geräteliste zeigt nur den Gerätenamen
- Seitliches Wischen eines Geräts in beide Richtungen legt die Aktionen
  `Bearbeiten` und `Löschen` frei
- Seitliches Wischen eines WLAN-Profils in beide Richtungen legt die Aktionen
  `Bearbeiten` und `Löschen` frei
- Swipe nach rechts zeigt die Aktionssymbole links
- Swipe nach links zeigt die Aktionssymbole rechts
- Der Swipe selbst bearbeitet und löscht nicht sofort
- Bearbeiten erfolgt nur nach Tipp auf das freigelegte Bleistift-Symbol
- Löschen erfolgt nur nach Tipp auf das freigelegte Mülleimer-Symbol
- Tippen irgendwo außerhalb der beiden freigelegten Symbole schließt einen
  offenen Swipe und führt keine weitere Aktion aus
- Ein neuer Swipe schließt alle anderen offenen Swipes
- Offene Zeilen bleiben lesbar und werden im Hell- und Dunkelmodus farblich
  hervorgehoben
- Der Zeilentext wird nicht aus dem Rahmen geschoben
- Listenflächen bleiben auch bei wenigen oder keinen Einträgen gleich hoch und
  werden mit der Listen-Hintergrundfarbe gefüllt
- Bleistift-Icons sind nicht dauerhaft sichtbar
- Mülleimer-Icons sind nicht dauerhaft sichtbar
- Keine dauerhaft sichtbaren Aktionsbuttons am rechten Listenrand

## Technische Umsetzung

- Keine externe Swipe-Library
- Keine neue Dependency
- Keine Verwendung von `SwipeToDismissBox`
- Kleine lokale Compose-Helper-Composable `SwipeRevealItem`
- Umsetzung mit `AnchoredDraggableState`, `DraggableAnchors` und
  `Modifier.anchoredDraggable`
- Bestehende ViewModel-Callbacks wiederverwendet
- Keine Datenmodell- oder Repository-Änderungen

## Nicht im Scope

- Neue Dialog-Layouts
- Drag & Drop Sortierung
- Snackbar-Undo-Löschung
- Sofortiges Bearbeiten oder Löschen durch Swipe
- Bearbeitung durch Klick auf Gerätenamen im Dashboard
- Mehrfachauswahl
- Änderung der Datenmodelle
- Änderung der Passwortspeicherung
- Änderung der Geräteaktion- oder WLAN-Verbindungslogik

## Akzeptanzkriterien

- [x] Klick auf Gerätenamen in den Einstellungen öffnet Gerätebearbeitung
- [x] Klick auf WLAN-Eintrag öffnet WLAN-Bearbeitung
- [x] Geräteliste zeigt nur den Gerätenamen
- [x] Bleistift-Icons sind entfernt
- [x] Mülleimer-Icons sind entfernt
- [x] Es sind keine dauerhaft sichtbaren Aktionsbuttons am rechten Listenrand vorhanden
- [x] Swipe auf Gerät in beide Richtungen legt `Bearbeiten` und `Löschen` frei
- [x] Swipe auf WLAN-Profil in beide Richtungen legt `Bearbeiten` und `Löschen` frei
- [x] Swipe selbst führt keine Aktion sofort aus
- [x] Swipe nach rechts zeigt die Aktionen links
- [x] Swipe nach links zeigt die Aktionen rechts
- [x] Bearbeiten erfolgt nur nach Tipp auf das freigelegte Bleistift-Symbol
- [x] Löschen erfolgt nur nach Tipp auf das freigelegte Mülleimer-Symbol
- [x] Tippen außerhalb der beiden Symbole schließt den offenen Swipe ohne weitere Aktion
- [x] Ein neuer Swipe schließt alle anderen offenen Swipes
- [x] Offene Zeilen bleiben lesbar und werden farblich hervorgehoben
- [x] Listenflächen behalten ihre Höhe auch bei wenigen oder keinen Einträgen
- [x] Geräte löschen bleibt möglich
- [x] WLAN-Profile löschen bleibt möglich

## Prüfungen

Erfolgreich im Container ausgeführt:

```bash
git diff --check
./gradlew testDebugUnitTest
./gradlew assembleDebug
```

Auf dem Host zusätzlich empfohlen:

```bash
./gradlew clean assembleDebug
./gradlew installDebug
```

## Testhinweise

- Gerät in den Einstellungen per kurzem Klick auf den Namen bearbeiten
- WLAN-Profil per kurzem Klick auf den Eintrag bearbeiten
- Gerät nach links und rechts swipen
- WLAN-Profil nach links und rechts swipen
- Prüfen, dass Swipe selbst noch nicht bearbeitet oder löscht
- Prüfen, dass die Aktionssymbole je nach Swipe-Richtung nur auf einer Seite
  erscheinen
- Prüfen, dass nur Bleistift und Mülleimer Aktionen auslösen
- Prüfen, dass Tippen außerhalb der Symbole nur schließt
- Prüfen, dass ein neuer Swipe andere offene Swipes schließt
- Prüfen, dass leere oder kurze Listen optisch gleich hoch bleiben
- Prüfen, dass keine Bearbeiten- oder Löschen-Icons dauerhaft sichtbar sind
