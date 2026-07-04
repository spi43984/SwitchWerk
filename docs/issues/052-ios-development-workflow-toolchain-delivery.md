# Issue 052 iOS: Development Workflow, Toolchain & Delivery

## Metadaten

- Status: Backlog
- Priorität: P4
- Plattform: iOS
- iOS-Phase: 1 von 6
- Typ: Entwicklungsworkflow / Tooling / Qualität

## Ziel

Den reproduzierbaren Entwicklungs-, Test- und Auslieferungsworkflow für eine spätere native iOS-App festlegen. Das Issue schafft die Voraussetzungen für die Machbarkeitsprüfung und verhindert, dass iOS-spezifische Einschränkungen nur im Simulator oder zu spät vor der Veröffentlichung entdeckt werden.

## Scope

### Lokale Entwicklungsumgebung

- Verfügbaren Mac, unterstützte macOS- und Xcode-Version sowie eine begründete iOS-Mindestversion festlegen.
- Unterstützte iPhone-Modelle, die konkrete iOS-Version der Testgeräte und eine geeignete iOS-Mindestversion dokumentieren.
- Xcode-Projekt, Shared Scheme und lokale Kommandozeilen-Prüfungen mit `xcodebuild` planen.
- Physische iPhones und simulierte Geräte klar abgrenzen: Simulatoren sind für UI- und viele Unit-Tests geeignet, WLAN-, Local-Network-, Geräte-AP- und System-Prompt-Flows müssen auf realen Geräten geprüft werden.
- Mindestens ein reales iPhone mit aktueller iOS-Version und EU-Region als Pflicht-Testgerät für die spätere Machbarkeitsprüfung festlegen.
- Reale Shelly-Geräte-APs, Heim-WLAN und ein AP-ohne-Internet-Szenario als erforderliche Testumgebung festlegen.

### Minimaler Prototyp-Pfad ohne Apple Developer Program

- Prüfen und dokumentieren, ob Issue 047 zunächst mit kostenlosem Apple-Account, Xcode und einem eigenen iPhone durchgeführt werden kann.
- Die Grenzen kostenloser Signierung klar festhalten: nur Entwicklung und Machbarkeitsprüfung, keine Team-Verteilung, kein TestFlight, keine App-Store-Veröffentlichung und keine dauerhaft belastbare Nutzerinstallation.
- Erneuerungs- oder Ablaufgrenzen kostenlos signierter Builds dokumentieren, ohne diese als späteren Verteilungsweg einzuplanen.
- Festlegen, ab welchem Punkt ein kostenpflichtiges Apple Developer Program zwingend erforderlich wird.

### Signierung und Geheimnisse

- Bundle-ID, Apple-Developer-Program-Zugang, Team-Rollen, Signing und Provisioning als spätere Voraussetzungen dokumentieren.
- Die Varianten kostenloser Apple-Account, Apple Developer Program, TestFlight, App Store und EU-Alternative Distribution in einer Entscheidungsmatrix gegenüberstellen.
- Zugangsdaten, Zertifikate, Provisioning-Profile und sonstige Secrets außerhalb des Repositorys halten.
- Keychain als Ziel für sensible lokale App-Daten einplanen; keine Passwörter in Beispielkonfigurationen oder Logs.

### Qualität und CI/CD

- Prüfen und begründen, ob für iOS GitHub Actions mit macOS-Runnern oder Xcode Cloud eingesetzt wird; eine Entscheidung für CI ist keine Cloud-Abhängigkeit der App.
- Mindestprüfungen für Pull Requests definieren: Build, Unit-Tests und geeignete UI-Tests.
- Eine kleine realistische iPhone-Testmatrix für WLAN/AP/HTTP festlegen.
- Konkrete lokale Prüf- und Diagnosebefehle für die spätere Host-Arbeit dokumentieren, insbesondere `xcodebuild`-Aufrufe und Geräteprüfung, ohne Secrets oder lokale reale Netzwerkinformationen in Befehlen zu speichern.
- TestFlight als späteren Beta-Verteilungsweg und App-Store-Connect-Voraussetzungen planen, ohne bereits einen App-Store-Eintrag oder eine Veröffentlichung anzulegen.

### Sicherheits- und Release-Prüfungen

- Local-Network-Erklärung, benötigte `Info.plist`-Angaben und Berechtigungsdialoge als Review-Punkte aufnehmen.
- App Transport Security (ATS) und gegebenenfalls eng begrenzte, begründete Ausnahmen für lokale HTTP-Geräte prüfen.
- Datenschutzangaben, Logging-Regeln und Abbruch-/Fehlerpfade vor einer externen Beta prüfen.
- EU-Sideloading, Web Distribution und alternative App-Marktplätze ausdrücklich nicht als einfachen GitHub-APK-Ersatz behandeln, sondern als später separat zu bewertende Distributionswege dokumentieren.

### Erwartete Ergebnisartefakte

- Kurze Entscheidungsmatrix zu Installations- und Verteilwegen: kostenloser Apple-Account, Apple Developer Program, TestFlight, App Store und EU-Alternative Distribution.
- Kostenübersicht je Phase: kostenloser Prototyp, Machbarkeitsprüfung, TestFlight/Beta, App-Store- oder alternative Distribution.
- Dokumentierte Host-Voraussetzungen: Mac, macOS, Xcode, reales iPhone, iOS-Version, Apple-Account-Variante und Testumgebung.
- Konkrete lokale Prüfkommandos für Xcode- und `xcodebuild`-basierte Builds.
- Klare Abschlussentscheidung: `Issue 047 kann starten: ja/nein`; bei `ja` mit allen Voraussetzungen, bei `nein` mit blockierenden Punkten.

## Abhängigkeiten

- Dieses Issue muss vor Issue 047 abgeschlossen oder mindestens so weit entschieden sein, dass ein echter iOS-Prototyp auf Mac und Testgerät ausgeführt werden kann.
- Die Ergebnisse aus Issue 047 konkretisieren anschließend die reale Testmatrix und die Release-Risiken.

## Nicht Bestandteil

- Implementierung der iOS-App oder eines produktiven CI-Workflows.
- Implementierung eines SwiftUI-Prototyps oder einer iOS-App-Foundation.
- Anlegen eines Apple-Developer-Kontos, App-Store-Connect-Eintrags, TestFlight-Builds oder einer Veröffentlichung.
- Einrichtung von EU-Web-Distribution, alternativen App-Marktplätzen oder sonstigen externen Distributionskanälen.
- Cloud-, Tracking-, Account- oder Analytics-Funktionen in SwitchWerk.
- Änderung des bestehenden Android-Workflows.

## Akzeptanzkriterien

- [ ] Entwicklungsrechner, Xcode/macOS-Kompatibilität, unterstützte Testgeräte und iOS-Mindestversion sind festgelegt.
- [ ] Mindestens ein reales iPhone und die WLAN/AP-Testumgebung für Issue 047 sind verfügbar und dokumentiert.
- [ ] Der Minimalpfad mit kostenlosem Apple-Account, Xcode und eigenem iPhone ist bewertet und seine Grenzen sind dokumentiert.
- [ ] Der Zeitpunkt, ab dem ein kostenpflichtiges Apple Developer Program erforderlich wird, ist klar benannt.
- [ ] Lokale Build- und Testbefehle sowie Mindestprüfungen für Pull Requests sind definiert.
- [ ] CI-Option, Signierung, Secret-Handhabung und TestFlight-Voraussetzungen sind bewertet, ohne externe Ressourcen anzulegen.
- [ ] ATS, Local-Network-Review und datenschutzgerechtes Logging sind als Release-Prüfpunkte festgehalten.
- [ ] Eine Entscheidungsmatrix und Kostenübersicht für die Installations- und Verteilwege liegen vor.
- [ ] EU-Sideloading, Web Distribution und alternative App-Marktplätze sind als spätere Distributionsfragen abgegrenzt und nicht als Voraussetzung für Issue 047 eingeplant.
- [ ] Am Ende ist eindeutig dokumentiert, ob Issue 047 starten kann und welche Voraussetzungen dafür erfüllt sein müssen.
- [ ] Der iOS-Workflow ergänzt den Android-Workflow, ohne ihn zu ändern.
