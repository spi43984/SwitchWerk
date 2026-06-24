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
- Xcode-Projekt, Shared Scheme und lokale Kommandozeilen-Prüfungen mit `xcodebuild` planen.
- Physische iPhones und simulierte Geräte klar abgrenzen: Simulatoren sind für UI- und viele Unit-Tests geeignet, WLAN-, Local-Network-, Geräte-AP- und System-Prompt-Flows müssen auf realen Geräten geprüft werden.
- Reale Shelly-Geräte-APs, Heim-WLAN und ein AP-ohne-Internet-Szenario als erforderliche Testumgebung festlegen.

### Signierung und Geheimnisse

- Bundle-ID, Apple-Developer-Program-Zugang, Team-Rollen, Signing und Provisioning als Voraussetzungen dokumentieren.
- Zugangsdaten, Zertifikate, Provisioning-Profile und sonstige Secrets außerhalb des Repositorys halten.
- Keychain als Ziel für sensible lokale App-Daten einplanen; keine Passwörter in Beispielkonfigurationen oder Logs.

### Qualität und CI/CD

- Prüfen und begründen, ob für iOS GitHub Actions mit macOS-Runnern oder Xcode Cloud eingesetzt wird; eine Entscheidung für CI ist keine Cloud-Abhängigkeit der App.
- Mindestprüfungen für Pull Requests definieren: Build, Unit-Tests und geeignete UI-Tests.
- Eine kleine realistische iPhone-Testmatrix für WLAN/AP/HTTP festlegen.
- TestFlight als späteren Beta-Verteilungsweg und App-Store-Connect-Voraussetzungen planen, ohne bereits einen App-Store-Eintrag oder eine Veröffentlichung anzulegen.

### Sicherheits- und Release-Prüfungen

- Local-Network-Erklärung, benötigte `Info.plist`-Angaben und Berechtigungsdialoge als Review-Punkte aufnehmen.
- App Transport Security (ATS) und gegebenenfalls eng begrenzte, begründete Ausnahmen für lokale HTTP-Geräte prüfen.
- Datenschutzangaben, Logging-Regeln und Abbruch-/Fehlerpfade vor einer externen Beta prüfen.

## Abhängigkeiten

- Dieses Issue muss vor Issue 047 abgeschlossen oder mindestens so weit entschieden sein, dass ein echter iOS-Prototyp auf Mac und Testgerät ausgeführt werden kann.
- Die Ergebnisse aus Issue 047 konkretisieren anschließend die reale Testmatrix und die Release-Risiken.

## Nicht Bestandteil

- Implementierung der iOS-App oder eines produktiven CI-Workflows.
- Anlegen eines Apple-Developer-Kontos, App-Store-Connect-Eintrags, TestFlight-Builds oder einer Veröffentlichung.
- Cloud-, Tracking-, Account- oder Analytics-Funktionen in SwitchWerk.
- Änderung des bestehenden Android-Workflows.

## Akzeptanzkriterien

- [ ] Entwicklungsrechner, Xcode/macOS-Kompatibilität und iOS-Mindestversion sind festgelegt.
- [ ] Mindestens ein reales iPhone und die WLAN/AP-Testumgebung für Issue 047 sind verfügbar und dokumentiert.
- [ ] Lokale Build- und Testbefehle sowie Mindestprüfungen für Pull Requests sind definiert.
- [ ] CI-Option, Signierung, Secret-Handhabung und TestFlight-Voraussetzungen sind bewertet, ohne externe Ressourcen anzulegen.
- [ ] ATS, Local-Network-Review und datenschutzgerechtes Logging sind als Release-Prüfpunkte festgehalten.
- [ ] Der iOS-Workflow ergänzt den Android-Workflow, ohne ihn zu ändern.
