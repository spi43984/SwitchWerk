# Issue 052 iOS: Development Workflow, Toolchain & Delivery

## Metadaten

- Status: Aktiv
- Priorität: P4
- Plattform: iOS
- iOS-Phase: 1 von 6
- Typ: Entwicklungsworkflow / Tooling / Qualität
- Bearbeitungsstand: Workflow entschieden, keine Implementierung begonnen

## Ziel

Den reproduzierbaren Entwicklungs-, Test- und Auslieferungsworkflow für eine
spätere native iOS-App festlegen. Das Issue schafft die Voraussetzungen für die
Machbarkeitsprüfung und verhindert, dass iOS-spezifische Einschränkungen nur im
Simulator oder zu spät vor der Veröffentlichung entdeckt werden.

Der Maintainer hat derzeit keine Apple-Geräte. Für iOS-Entwicklung müssen daher
mindestens eine externe entwickelnde oder testende Person mit geeignetem Mac und
realem iPhone eingebunden werden.

## Ergebnis

Issue 047 kann starten: bedingt ja.

Start ist nur sinnvoll, wenn eine eingeladene iOS-Testperson vorab bestätigt:

- Mac mit unterstützter macOS-Version und aktuellem Xcode ist verfügbar.
- Mindestens ein reales iPhone mit aktueller iOS-Version ist verfügbar.
- Das iPhone kann in EU-Region, Heim-WLAN, Geräte-AP und AP-ohne-Internet
  getestet werden.
- Ein Apple Account kann in Xcode für lokale Entwicklung verwendet werden.
- Die Testperson kann lokale `xcodebuild`-Prüfungen ausführen und Ergebnisse
  zurückmelden.

Ohne diese Person und ohne reales iPhone ist Issue 047 blockiert, weil WLAN-,
Local-Network-, Geräte-AP- und System-Prompt-Verhalten nicht belastbar im
Simulator geprüft werden können.

## Quellenstand

Geprüft am 23. Juli 2026 anhand offizieller Apple-Dokumentation:

- Xcode-Systemanforderungen:
  https://developer.apple.com/xcode/system-requirements/
- Apple Developer Program:
  https://developer.apple.com/programs/whats-included/
- Mitgliedschaftsvergleich:
  https://developer.apple.com/support/compare-memberships/
- TestFlight:
  https://developer.apple.com/testflight/
- App-Store-Connect-SDK-Anforderungen:
  https://developer.apple.com/news/upcoming-requirements/
- EU-Distribution:
  https://developer.apple.com/support/dma-and-apps-in-the-eu/
- EU-Web-Distribution:
  https://developer.apple.com/support/web-distribution-eu/
- Local Network Privacy:
  https://developer.apple.com/documentation/technotes/tn3179-understanding-local-network-privacy
- ATS:
  https://developer.apple.com/documentation/bundleresources/information-property-list/nsapptransportsecurity

Apple-Vorgaben zu Xcode, SDKs, Distribution und EU-Regeln können sich ändern.
Vor App-Store-, TestFlight- oder EU-Distributionsarbeit müssen diese Quellen
erneut geprüft werden.

## Scope

### Lokale Entwicklungsumgebung

- Verfügbaren Mac, unterstützte macOS- und Xcode-Version sowie eine begründete
  iOS-Mindestversion festlegen.
- Unterstützte iPhone-Modelle, die konkrete iOS-Version der Testgeräte und eine
  geeignete iOS-Mindestversion dokumentieren.
- Xcode-Projekt, Shared Scheme und lokale Kommandozeilen-Prüfungen mit
  `xcodebuild` planen.
- Physische iPhones und simulierte Geräte klar abgrenzen: Simulatoren sind für
  UI- und viele Unit-Tests geeignet, WLAN-, Local-Network-, Geräte-AP- und
  System-Prompt-Flows müssen auf realen Geräten geprüft werden.
- Mindestens ein reales iPhone mit aktueller iOS-Version und EU-Region als
  Pflicht-Testgerät für die spätere Machbarkeitsprüfung festlegen.
- Reale Shelly-Geräte-APs, Heim-WLAN und ein AP-ohne-Internet-Szenario als
  erforderliche Testumgebung festlegen.

### Minimaler Prototyp-Pfad ohne Apple Developer Program

- Issue 047 kann für eine einzelne entwickelnde Person zunächst mit kostenlosem
  Apple Account, Xcode und eigenem iPhone geprüft werden.
- Dieser Pfad gilt nur für lokale Entwicklung und Machbarkeitsprüfung.
- Kostenlose Signierung ist kein Team-Verteilweg, kein TestFlight-Ersatz, keine
  App-Store-Veröffentlichung und keine dauerhaft belastbare Nutzerinstallation.
- Ablauf- oder Erneuerungsgrenzen kostenlos signierter Builds werden vor dem
  ersten Geräte-Test in Xcode geprüft und nicht als Release-Mechanismus
  eingeplant.
- Ein kostenpflichtiges Apple Developer Program wird zwingend, sobald Builds an
  weitere Personen verteilt, TestFlight genutzt, App Store Connect verwendet,
  Geräte systematisch registriert oder externe Beta-/Release-Abläufe begonnen
  werden.

### Signierung und Geheimnisse

- Vor iOS-Codearbeit wird eine neutrale Bundle-ID festgelegt, zum Beispiel
  `com.example.switchwerk`.
- Apple-Developer-Program-Zugang, Team-Rollen, Signing und Provisioning sind
  spätere Voraussetzungen für Beta und Release.
- Zugangsdaten, Zertifikate, Provisioning-Profile, App-Store-Connect-API-Keys
  und sonstige Secrets bleiben außerhalb des Repositorys.
- Lokale sensible App-Daten werden für iOS mit Keychain geplant.
- Beispiele, Logs und Testnotizen verwenden ausschließlich neutrale Platzhalter
  wie `device.local`, `192.0.2.10` und `Example WiFi`.

### Qualität und CI/CD

- Für Issue 047 wird kein CI-System eingerichtet.
- Lokale Prüfungen auf dem Mac der iOS-Testperson reichen für die
  Machbarkeitsphase.
- Für spätere Pull Requests werden mindestens Build, Unit-Tests und geeignete
  UI-Tests definiert.
- GitHub Actions mit macOS-Runnern ist später naheliegend, wenn der Code im
  bestehenden GitHub-Workflow geprüft werden soll.
- Xcode Cloud ist später möglich, bindet den iOS-Workflow aber stärker an
  Apple-Dienste und App Store Connect.
- Eine CI-Entscheidung ist keine Cloud-Abhängigkeit der App selbst, solange die
  App-Funktion lokal bleibt und keine Cloud-Laufzeitdienste nutzt.
- TestFlight wird als späterer Beta-Verteilweg geplant, aber in diesem Issue
  nicht eingerichtet.

### Sicherheits- und Release-Prüfungen

- `NSLocalNetworkUsageDescription` ist als Review-Punkt aufzunehmen, sobald die
  App lokale Geräte anspricht.
- `NSBonjourServices` ist zu prüfen, wenn Bonjour/mDNS zur Gerätesuche genutzt
  wird.
- App Transport Security (ATS) bleibt grundsätzlich aktiv. Ausnahmen für lokale
  HTTP-Geräte dürfen nur eng begrenzt, begründet und dokumentiert werden.
- Datenschutzangaben, Logging-Regeln, Abbruchpfade, Netzwerkfehler und
  Berechtigungsdialoge werden vor externer Beta erneut geprüft.
- EU-Sideloading, Web Distribution und alternative App-Marktplätze werden nicht
  als einfacher GitHub-APK-Ersatz behandelt. Diese Wege erfordern separate
  Bewertung, Apple-Voraussetzungen und voraussichtlich Developer-Program-Status.

## Host-Voraussetzungen für Issue 047

### Pflicht

- Mac mit Xcode 26 oder neuer.
- macOS passend zur verwendeten Xcode-Version. Für Xcode 26 nennt Apple macOS
  Sequoia 15.6 oder neuer.
- Ein reales iPhone mit aktueller iOS-Version.
- Regionale Testeinstellung: EU-Region, soweit für Distribution oder
  Systemdialoge relevant.
- Apple Account für lokale Xcode-Entwicklung.
- USB- oder vertrauenswürdige lokale Verbindung zwischen Mac und iPhone.
- Testumgebung mit:
  - Heim-WLAN mit Internetzugang.
  - Geräte-AP eines lokalen Testgeräts.
  - AP-ohne-Internet-Szenario.
  - Lokales HTTP-Ziel mit neutralem Platzhalter, zum Beispiel `192.0.2.10`.

### Empfohlen

- Zweites reales iPhone oder zweite iOS-Version für Gegenprobe.
- Ein aktuelles iPhone als Hauptgerät und ein älteres noch unterstütztes iPhone
  als Kompatibilitätsgerät.
- Separater Test-Apple-Account ohne private Produktivdaten.
- Schriftliche Rückmeldung der Testperson zu Gerät, iOS-Version, Xcode-Version,
  ausgeführten Befehlen und beobachtetem Verhalten.

## iOS-Mindestversion und Testmatrix

### Entscheidung

- Vorläufige iOS-Mindestversion: iOS 17.
- Begründung: iOS 17 ist für eine neue App konservativ genug, vermeidet sehr
  alte Plattformzustände und erlaubt trotzdem Tests auf nicht ausschließlich
  neuesten Geräten.
- App-Store-Uploads müssen nach aktuellem Apple-Stand mit Xcode 26 oder neuer
  und iOS-26-SDK oder neuer gebaut werden. Das ist unabhängig von der
  Deployment-Target-Entscheidung.

### Mindest-Testmatrix für Issue 047

| Testfall | Simulator | Reales iPhone |
|---|---:|---:|
| UI-Start und Navigation | ja | ja |
| Unit-Tests ohne Netzwerk | ja | ja |
| HTTP-Aufruf gegen lokales Gerät | eingeschränkt | Pflicht |
| Local-Network-Prompt | nein | Pflicht |
| Verbindung mit Geräte-AP | nein | Pflicht |
| AP ohne Internet | nein | Pflicht |
| Fehlerpfad bei nicht erreichbarem Gerät | eingeschränkt | Pflicht |
| EU-regionale Distributionsannahmen | nein | Pflicht |

## Entscheidungsmatrix Installation und Distribution

| Weg | Geeignet für Issue 047 | Geeignet für eingeladene Personen | Kosten | Grenzen |
|---|---:|---:|---:|---|
| Kostenloser Apple Account mit Xcode | ja, für eine Person mit eigenem iPhone | nur wenn jede Person selbst mit Xcode baut | 0 USD | keine Team-Verteilung, kein TestFlight, keine Veröffentlichung, nicht als dauerhafte Installation planen |
| Apple Developer Program | ja | ja, über registrierte Geräte oder TestFlight | 99 USD pro Jahr | Konto, Rollen, Signing und Provisioning erforderlich |
| TestFlight | nein, nicht für ersten lokalen Prototyp nötig | ja, für Beta-Tests | Developer Program erforderlich | App Store Connect, Review-/Beta-Prozess und Signing erforderlich |
| App Store | nein | ja, für öffentliche Veröffentlichung | Developer Program erforderlich | Review, Datenschutzangaben, Release-Prozess und laufende Pflege erforderlich |
| EU-Alternative Distribution / Web Distribution | nein | später separat zu bewerten | Developer Program und weitere Apple-Voraussetzungen | kein einfacher APK-Ersatz, regionale und organisatorische Anforderungen, separate Risiko- und Kostenprüfung nötig |

## Kostenübersicht je Phase

| Phase | Minimaler Kostenansatz | Bemerkung |
|---|---:|---|
| Lokale Vorplanung | 0 USD | Dokumentation und technische Vorbereitung ohne Apple-Geräte |
| Issue 047 mit externer Testperson | 0 USD für das Projekt, wenn die Testperson Mac und iPhone stellt | Nur lokaler Build über Xcode; keine verteilte Beta |
| Eigene belastbare iOS-Entwicklung | Kosten für geeigneten Mac und iPhone | Erforderlich, wenn das Projekt selbst unabhängig testen soll |
| Team-Beta / TestFlight | 99 USD pro Jahr | Apple Developer Program erforderlich |
| App Store | 99 USD pro Jahr plus mögliche weitere geschäftliche Aufwände | Veröffentlichung, Review und Datenschutzangaben erforderlich |
| EU-Alternative Distribution | separat zu bewerten | Keine Voraussetzung für Issue 047 |

## Lokale Prüf- und Diagnosebefehle für spätere Mac-Arbeit

Die folgenden Befehle sind Planungsbefehle für die spätere iOS-Codebasis. Sie
werden erst verwendet, wenn ein Xcode-Projekt existiert.

```bash
# Xcode-Version prüfen
xcodebuild -version
```

```bash
# Verfügbare SDKs prüfen
xcodebuild -showsdks
```

```bash
# Verfügbare Simulatoren und Geräte prüfen
xcrun simctl list devices
```

```bash
# Projekt-Schemes prüfen
xcodebuild \
  -list \
  -project SwitchWerk.xcodeproj
```

```bash
# Simulator-Build für schnelle UI-/Unit-Test-Rückmeldung
xcodebuild \
  -project SwitchWerk.xcodeproj \
  -scheme SwitchWerk \
  -destination "platform=iOS Simulator,name=iPhone 16" \
  clean build
```

```bash
# Unit-Tests im Simulator
xcodebuild \
  -project SwitchWerk.xcodeproj \
  -scheme SwitchWerk \
  -destination "platform=iOS Simulator,name=iPhone 16" \
  test
```

```bash
# Reales Gerät anzeigen
xcrun devicectl list devices
```

```bash
# Build für ein angeschlossenes reales iPhone
xcodebuild \
  -project SwitchWerk.xcodeproj \
  -scheme SwitchWerk \
  -destination "platform=iOS,name=Example iPhone" \
  build
```

Keine Befehle enthalten reale Team-IDs, Bundle-IDs, Gerätenamen,
Netzwerknamen, IP-Adressen, Zertifikatspfade oder Secrets.

## Team- und Einladungsmodell

Für die nächste Phase gibt es zwei praktikable Wege:

1. Externe Person baut lokal aus dem Quellcode.
   - Geeignet für Issue 047.
   - Kein Apple Developer Program zwingend nötig.
   - Jede Person nutzt eigenen Mac, eigenes iPhone und eigenen Apple Account.
   - Keine Binärverteilung durch das Projekt.

2. Projekt verteilt Builds an weitere Personen.
   - Nicht nötig für Issue 047.
   - Apple Developer Program wird erforderlich.
   - TestFlight ist der bevorzugte Beta-Weg.
   - Signing, Provisioning, Rollen und App Store Connect müssen vorbereitet
     werden.

Für den aktuellen Stand wird Weg 1 gewählt. Der Wechsel zu Weg 2 wird erst nach
der Machbarkeitsprüfung aus Issue 047 entschieden.

## Abhängigkeiten

- Dieses Issue muss vor Issue 047 abgeschlossen oder mindestens so weit
  entschieden sein, dass ein echter iOS-Prototyp auf Mac und Testgerät
  ausgeführt werden kann.
- Die Ergebnisse aus Issue 047 konkretisieren anschließend die reale Testmatrix
  und die Release-Risiken.

## Nicht Bestandteil

- Implementierung der iOS-App oder eines produktiven CI-Workflows.
- Implementierung eines SwiftUI-Prototyps oder einer iOS-App-Foundation.
- Anlegen eines Apple-Developer-Kontos, App-Store-Connect-Eintrags,
  TestFlight-Builds oder einer Veröffentlichung.
- Einrichtung von EU-Web-Distribution, alternativen App-Marktplätzen oder
  sonstigen externen Distributionskanälen.
- Cloud-, Tracking-, Account- oder Analytics-Funktionen in SwitchWerk.
- Änderung des bestehenden Android-Workflows.

## Akzeptanzkriterien

- [x] Entwicklungsrechner, Xcode/macOS-Kompatibilität, unterstützte Testgeräte
  und iOS-Mindestversion sind festgelegt.
- [ ] Mindestens ein reales iPhone und die WLAN/AP-Testumgebung für Issue 047
  sind durch eine eingeladene Testperson bestätigt.
  Die Pflichtanforderung ist dokumentiert; die tatsächliche Verfügbarkeit ist
  noch offen.
- [x] Der Minimalpfad mit kostenlosem Apple-Account, Xcode und eigenem iPhone
  ist bewertet und seine Grenzen sind dokumentiert.
- [x] Der Zeitpunkt, ab dem ein kostenpflichtiges Apple Developer Program
  erforderlich wird, ist klar benannt.
- [x] Lokale Build- und Testbefehle sowie Mindestprüfungen für Pull Requests
  sind definiert.
- [x] CI-Option, Signierung, Secret-Handhabung und TestFlight-Voraussetzungen
  sind bewertet, ohne externe Ressourcen anzulegen.
- [x] ATS, Local-Network-Review und datenschutzgerechtes Logging sind als
  Release-Prüfpunkte festgehalten.
- [x] Eine Entscheidungsmatrix und Kostenübersicht für die Installations- und
  Verteilwege liegen vor.
- [x] EU-Sideloading, Web Distribution und alternative App-Marktplätze sind als
  spätere Distributionsfragen abgegrenzt und nicht als Voraussetzung für Issue
  047 eingeplant.
- [x] Am Ende ist eindeutig dokumentiert, ob Issue 047 starten kann und welche
  Voraussetzungen dafür erfüllt sein müssen.
- [x] Der iOS-Workflow ergänzt den Android-Workflow, ohne ihn zu ändern.
