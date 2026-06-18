# AI Handoff

Stand: 18. Juni 2026

## Aktuelle Arbeit

Keine aktive Implementierung.

Nächstes geplantes Issue laut `ai-context.md`:

```text
023 Settings Display And Action Details
```

## Zuletzt abgeschlossene Arbeit

Issue 018 "Adaptive WiFi Security Fallback" ist implementiert, geprüft,
veröffentlicht, nach `main` gemergt und lokal dokumentiert.

- GitHub-Issue: #48
- Pull Request: #50

## Umgesetzter Scope Issue 018

- WLAN-Sicherheitstypen werden als `WifiSecurityType` modelliert.
- Pro WLAN-Profil wird der zuletzt erfolgreiche Sicherheitstyp gespeichert.
- Ohne bekannten Typ wird WPA2 zuerst versucht.
- Mit bekanntem Typ wird der gespeicherte Typ zuerst versucht.
- Bei plausiblen WLAN-Verbindungsfehlern wird einmalig der andere Typ versucht.
- Beide Security-Versuche teilen sich ein begrenztes Gesamt-Timeout.
- Nach erfolgreicher Verbindung wird der funktionierende Typ gespeichert.
- Import/Export erhält die drei Zustände `null`, `WPA2_PSK` und `WPA3_SAE`.
- Bestehende WLAN-Profile werden per Room-Migration übernommen.
- HTTP-, DNS-, Geräte- und App-Fehler lösen keinen Security-Fallback aus.

## Bestätigte Prüfungen

```text
git diff --check
./gradlew testDebugUnitTest
./gradlew assembleDebug
./gradlew clean assembleDebug
./gradlew installDebug
```

Host-Build, Installation und manuelle Gerätetests wurden vom Benutzer als
erfolgreich gemeldet.

## Wichtige Hinweise für die nächste Session

- Wiederverwendbare Startvorlage: `AI_SESSION_PROMPT.md`
- Die lokalen Statusdateien wurden nach Issue 018 aktualisiert:
  - `docs/issues/018-adaptive-wifi-security-fallback.md`
  - `docs/issues/overview.txt`
  - `ai-context.md`
  - `AI_HANDOFF.md`
- GitHub-Issue #48 ist abgeschlossen bzw. kann geschlossen werden.
- Nächstes fachliches Issue ist 023 "Settings Display And Action Details".

## Nächste geplante Themen

```text
023 Settings Display And Action Details
026 Settings UI Rework
027 WiFi Timeout Analysis And Stabilization
020 Device Assigned WiFi Order
025 Dashboard Widget Layout
028 Theme Mode Setting
029 Language Setting
030 WiFi Profile Deletion Safety
033 Android-managed WiFi networks
019 Configurable WiFi List Sorting
021 HTTP/HTTPS Device Actions
022 Request Body And Content-Type Support
031 Import Enforces Unique WiFi Profile Names
032 Room Schema And Migration Test Coverage
```
