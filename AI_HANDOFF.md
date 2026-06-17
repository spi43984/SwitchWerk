# AI Handoff

Stand: 17. Juni 2026

## Aktuelle Arbeit

Keine aktive Implementierung.

Nächstes geplantes Issue laut `ai-context.md`:

```text
017 Unique WiFi Profile Name
```

## Zuletzt abgeschlossene Arbeit

Issue 016 "Edit Items by Name Click and Swipe Actions" ist implementiert, geprüft,
veröffentlicht, nach `main` gemergt und abgeschlossen.

- GitHub-Issue: #37
- Pull Request: #38
- Branch: `edit-items-by-name-click`
- Implementierungs-Commit: `5f73093d19c36d17d7bce0530b420f1a66738015`
- Merge-Commit: `4468498f7d783bbad2b329778613198fd03ce26c`

## Umgesetzter Scope Issue 016

- Geräte und WLAN-Profile werden in den Einstellungen durch kurzen Klick auf den
  Namen bzw. Eintrag bearbeitet, solange kein Swipe-Eintrag offen ist.
- Geräte- und WLAN-Profilzeilen haben keine dauerhaft sichtbaren Bearbeiten- oder
  Löschen-Icons mehr.
- Swipe nach links oder rechts legt Aktionssymbole frei.
- Der Swipe selbst führt keine Aktion aus.
- Bearbeiten erfolgt nur über Tipp auf das freigelegte Bleistift-Symbol.
- Löschen erfolgt nur über Tipp auf das freigelegte Mülleimer-Symbol.
- Tippen außerhalb der freigelegten Symbole schließt einen offenen Swipe ohne
  weitere Aktion.
- Ein neuer Swipe schließt andere offene Swipes.
- Die Geräteliste in den Einstellungen zeigt nur noch den Gerätenamen.
- Listenflächen behalten auch bei wenigen oder keinen Einträgen ihre Höhe und
  Hintergrundfläche.

## Technische Umsetzung Issue 016

- Keine externe Swipe-Library.
- Keine neue Dependency.
- Keine Verwendung von `SwipeToDismissBox`.
- Kleine lokale Compose-Helper-Composable `SwipeRevealItem`.
- Umsetzung mit `AnchoredDraggableState`, `DraggableAnchors` und
  `Modifier.anchoredDraggable`.
- Bestehende ViewModel-Callbacks wurden wiederverwendet.
- Keine Datenmodell-, Repository-, Passwort-, WLAN-Verbindungs- oder
  Geräteaktionsänderungen.

## Bestätigte Prüfungen

```text
git diff --check
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew clean assembleDebug
./gradlew installDebug
```

Host-Build und Installation wurden vom Benutzer als erfolgreich gemeldet.

## Wichtige Hinweise für die nächste Session

- Die lokalen Statusdateien wurden nach Issue 016 aktualisiert:
  - `docs/issues/016-edit-items-by-name-click.md`
  - `docs/issues/overview.txt`
  - `ai-context.md`
  - `AI_HANDOFF.md`
- GitHub-Issue #37 ist geschlossen.
- PR #38 ist gemergt.
- Nächstes fachliches Issue ist 017 "Unique WiFi Profile Name".

## Nächste geplante Themen

```text
017 Unique WiFi Profile Name
018 Adaptive WiFi Security Fallback
019 Configurable WiFi List Sorting
020 Device Assigned WiFi Order
021 HTTP/HTTPS Device Actions
022 Request Body And Content-Type Support
023 Settings Display And Action Details
025 Dashboard Widget Layout
026 Settings UI Rework
```
