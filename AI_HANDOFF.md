# AI Handoff

Stand: 18. Juni 2026

## Kontext für neue Codex-Sessions

Vor einer Implementierung zuerst lesen:

* AGENTS.md
* AI_HANDOFF.md
* die aktuelle Issue-Datei unter `docs/issues`

Danach nur gezielt die für das Issue benötigten Dateien suchen und öffnen.

Keine vollständige Repository-Analyse durchführen, sofern dies nicht ausdrücklich erforderlich ist.

## Aktuelle Arbeit

Keine aktive Implementierung.

Nächstes geplantes Issue laut `ai-context.md`:

```text
018 Adaptive WiFi Security Fallback
```

## Zuletzt abgeschlossene Arbeit

Issue 017 "Unique WiFi Profile Name" ist implementiert, geprüft,
veröffentlicht, nach `main` gemergt und abgeschlossen.

- GitHub-Issue: #42

## Umgesetzter Scope Issue 017

- WLAN-Profile besitzen einen frei wählbaren Namen.
- Profilnamen sind eindeutig.
- Mehrere Profile dürfen dieselbe SSID verwenden.
- Name wird als primäre Bezeichnung angezeigt.
- SSID bleibt zusätzlich sichtbar.
- Bestehende Daten wurden per Room-Migration übernommen.
- Gerätezuordnungen können Profilnamen verwenden.

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

- Die lokalen Statusdateien wurden nach Issue 017 aktualisiert:
  - `docs/issues/017-unique-wifi-profile-name.md`
  - `docs/issues/overview.txt`
  - `ai-context.md`
  - `AI_HANDOFF.md`
- GitHub-Issue #42 ist abgeschlossen bzw. kann geschlossen werden.
- Nächstes fachliches Issue ist 018 "Adaptive WiFi Security Fallback".

## Nächste geplante Themen

```text
018 Adaptive WiFi Security Fallback
019 Configurable WiFi List Sorting
020 Device Assigned WiFi Order
021 HTTP/HTTPS Device Actions
022 Request Body And Content-Type Support
023 Settings Display And Action Details
025 Dashboard Widget Layout
026 Settings UI Rework
027 WiFi Timeout Analysis And Stabilization
028 Theme Mode Setting
029 Language Setting
030 WiFi Profile Deletion Safety
031 Import Enforces Unique WiFi Profile Names
032 Room Schema And Migration Test Coverage
```
