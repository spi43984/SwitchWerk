# AI Handoff

Stand: 23. Juli 2026

## Aktueller Stand

- Issue 052 `iOS: Development Workflow, Toolchain & Delivery` wurde als reine
  Dokumentationsarbeit bearbeitet und von `Backlog` auf `Aktiv` gesetzt.
- `docs/issues/052-ios-development-workflow-toolchain-delivery.md` enthält jetzt
  die iOS-Workflow-Entscheidung, Host-Voraussetzungen, Testmatrix,
  Entscheidungsmatrix für Installations- und Verteilwege, Kostenübersicht,
  lokale Xcode-/`xcodebuild`-Prüfbefehle und die Entscheidung zu Issue 047.
- `docs/issues/overview.txt` ist konsistent aktualisiert: Issue 052 steht auf
  `Aktiv` und bleibt als Voraussetzung vor Issue 047 eingeordnet.
- Es wurde kein Swift-Code, kein Xcode-Projekt, keine GitHub Action, kein
  Buildskript und keine App-Store-Konfiguration angelegt.

## Entscheidung

- Issue 047 kann bedingt starten.
- Voraussetzung ist eine eingeladene iOS-Testperson mit unterstütztem Mac,
  aktuellem Xcode, realem iPhone, Apple Account und Zugriff auf Heim-WLAN,
  Geräte-AP und AP-ohne-Internet-Testumgebung.
- Ohne reales iPhone und ohne externe Testperson ist Issue 047 blockiert.
- Für Issue 047 ist zunächst kein Apple Developer Program erforderlich, wenn die
  Testperson lokal mit Xcode auf dem eigenen iPhone baut.
- Für Team-Verteilung, TestFlight, App Store Connect oder externe Beta wird ein
  kostenpflichtiges Apple Developer Program erforderlich.

## Prüfungen

- Keine Gradle-Checks ausgeführt, da Issue 052 ausschließlich Markdown-
  Dokumentation betrifft.
- Keine iOS-Builds ausgeführt, da noch kein Xcode-Projekt existiert.

## Nächste Schritte

- Benutzer prüft die Markdown-Änderungen lokal.
- Wenn die Dokumentation passt, können die Änderungen committed und gepusht
  werden.
- Danach kann Issue 047 vorbereitet werden, sobald eine geeignete iOS-Testperson
  bestätigt ist.
