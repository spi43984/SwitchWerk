# Issue 070: Dependabot Vulnerabilities prüfen

## Metadaten

- Status: Abgeschlossen
- Priorität: P0
- Typ: Security / Dependencies
- Bereich: Abhängigkeiten / Dependabot / Gradle / Build

## Ziel

Die von GitHub Dependabot gemeldeten Sicherheitswarnungen sollen geprüft,
bewertet und soweit sinnvoll durch Dependency-Updates oder andere
risikoreduzierende Maßnahmen behoben werden.

## Hintergrund

GitHub meldet beim Push Sicherheitswarnungen für den Default-Branch. Diese
Warnungen stammen aus Dependabot Security Alerts und betreffen bekannte
Schwachstellen in direkten oder transitiven Projektabhängigkeiten.

Die Warnungen sind unabhängig von fachlichen UI-Änderungen, sollten aber
separat bewertet werden, damit Sicherheitsrisiken und mögliche Update-Folgen
kontrolliert behandelt werden.

## Scope

### Abhängigkeiten ansehen

- GitHub Dependabot Security Alerts prüfen.
- Betroffene direkte und transitive Abhängigkeiten identifizieren.
- Kritikalität und Ausnutzbarkeit für diese Android-App bewerten.
- Prüfen, ob die betroffenen Abhängigkeiten im App-Code, Build-System oder nur
  in nicht ausgelieferten Tooling-Pfaden verwendet werden.

### Updates bewerten

- Verfügbare sichere Versionen prüfen.
- Kompatibilität mit Android Gradle Plugin, Kotlin, Compose und bestehenden
  Bibliotheken bewerten.
- Breaking Changes und notwendige Codeanpassungen dokumentieren.
- Updates priorisieren: zuerst kritische und hohe Risiken, danach mittlere und
  niedrige Risiken.

### Umsetzung vorbereiten

- Einen konkreten Update-Plan erstellen.
- Falls Updates umgesetzt werden, nur notwendige Dependency- und
  Build-Konfigurationsänderungen vornehmen.
- Keine neuen externen Dienste, Analytics-, Tracking-, Werbe- oder
  Cloud-Abhängigkeiten einführen.
- Keine sensiblen Daten, Tokens, privaten Domains, realen Hostnamen oder
  lokalen IP-Adressen in Dokumentation, Tests oder Logs aufnehmen.

### Build und Tests ausführen

- Nach Änderungen sinnvolle Container-Prüfungen ausführen.
- Mindestens relevante Gradle-Prüfungen für Build, Lint und Tests bewerten.
- Host-Build und Installation bleiben durch den Benutzer auf dem Ubuntu-Host zu
  bestätigen.

## Nicht im Scope

- Keine funktionalen App-Features.
- Keine UI-Änderungen, außer sie werden durch notwendige Dependency-Updates
  zwingend erforderlich.
- Keine Änderung am Import-/Export-Dateiformat.
- Keine Änderung an WLAN- oder Geräteaktionslogik.
- Keine pauschale Aktualisierung aller Dependencies ohne Bezug zu den
  gemeldeten Sicherheitswarnungen.
- Kein Schließen oder Ignorieren von Dependabot-Warnungen ohne dokumentierte
  Begründung.

## Architekturhinweise

- Bestehende Gradle- und Android-Projektstruktur beibehalten.
- Dependency-Updates klein und nachvollziehbar halten.
- Sicherheitsrelevante Entscheidungen dokumentieren.
- Bei inkompatiblen Updates lieber ein Folge-Issue zuschneiden als große
  Nebenumbauten in dieses Issue aufzunehmen.

## Akzeptanzkriterien

- [x] GitHub Dependabot Security Alerts wurden geprüft.
- [x] Betroffene direkte und transitive Abhängigkeiten sind dokumentiert.
- [x] Kritikalität und Relevanz für SwitchWerk wurden bewertet.
- [x] Sichere Update-Pfade wurden geprüft.
- [x] Notwendige Dependency-Updates wurden umgesetzt oder begründet
      zurückgestellt.
- [x] Keine unnötigen neuen Abhängigkeiten wurden eingeführt.
- [x] Keine Cloud-, Tracking-, Analytics-, Werbe- oder Account-Funktion wurde
      eingeführt.
- [x] Build-, Lint- und Testauswirkungen wurden geprüft.
- [x] Verbleibende Risiken oder offene Dependabot-Warnungen sind dokumentiert.
- [x] Host-Build und Installation wurden durch den Benutzer bestätigt, falls
      Code- oder Build-Dateien geändert wurden.

## Ergebnis

- PR #165 wurde als Commit `20caac2` nach `main` übernommen.
- Der nicht benötigte Foojay-Toolchain-Resolver wurde entfernt.
- Gepatchte Versionen von Bouncy Castle, jose4j, JDOM und Commons Lang werden
  im Buildscript-Classpath erzwungen.
- Die ausgelieferten App-Runtime-Abhängigkeiten enthalten keine der von
  Dependabot gemeldeten Pakete. Die Warnungen betreffen transitive
  Gradle-/Android-Build- und Dependency-Submission-Pfade.
- Nach der Dependency-Submission auf `main` wurden die Alerts für `jose4j`
  und `jdom2` automatisch geschlossen.

## Verbleibende Warnungen

Nach der Neubewertung auf `main` bleiben 33 Alerts offen: 1 kritisch, 12 hoch,
18 mittel und 2 niedrig. Betroffen sind ältere, parallel im GitHub
Dependency-Graph erfasste Tooling-Versionen von Netty, Bouncy Castle, Commons
Lang und Apache HttpClient. Sichere Versionen derselben Bibliotheken sind zum
Teil bereits zusätzlich im Graph enthalten; die älteren Versionen stammen aus
weiteren Tooling-Konfigurationen und nicht aus dem App-Runtime-Classpath.

Ein globales Erzwingen aktueller Netty-, Bouncy-Castle- und HttpClient-Versionen
über sämtliche Gradle- und Plugin-Konfigurationen wurde zurückgestellt. Dies
könnte interne Gradle-, Android-Gradle-Plugin-, gRPC- oder
Dependency-Submission-Kompatibilität verändern und wäre ohne gezielte
Toolchain-Aktualisierung und separate Prüfung zu riskant. Der sichere Folgepfad
ist ein kompatibles Upgrade der verursachenden Build-Plugins bzw. Toolchains;
bis dahin besteht kein bekannter Auslieferungs- oder Laufzeitpfad in der App.

## Durchgeführte Prüfungen

- Container: `./gradlew buildEnvironment`
- Container: `./gradlew app:dependencies --configuration debugRuntimeClasspath`
- Container: `./gradlew lintDebug`
- Container: `./gradlew testDebugUnitTest`
- Host: `./gradlew lintRelease`
- Host: `./gradlew testDebugUnitTest`
- Host: `./gradlew clean assembleRelease`
- Host: `./gradlew installRelease`
- Host: manueller App-Smoke-Test
- GitHub: Android Quality Checks und Automatic Dependency Submission

## Testhinweise

- Dependabot Security Alerts in GitHub prüfen.
- Relevante Gradle-Abhängigkeitsbäume für betroffene Libraries prüfen.
- Nach Dependency-Änderungen ausführen:

```bash
./gradlew clean assembleDebug
./gradlew lintDebug
./gradlew testDebugUnitTest
./gradlew installDebug
```

- App nach Dependency-Updates kurz auf Start, Einstellungen, Import/Export,
  WLAN-Profile und Geräteaktionen prüfen.
