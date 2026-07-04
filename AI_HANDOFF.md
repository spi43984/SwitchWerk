# AI Handoff

Stand: 4. Juli 2026

## Startvorlage

Für die nächste Session zuerst `AI_SESSION_PROMPT.md` verwenden und danach
`AGENTS.md` sowie diesen Handoff beachten.

## Aktueller Stand

In Arbeit:

- Issue 070 „Dependabot Vulnerabilities prüfen“
- GitHub-Issue: #164
- Branch: `dependabot-vulnerabilities-check`
- Commit, Push und Pull Request wurden vom Benutzer freigegeben. Nicht mergen,
  kein GitHub-Issue schließen und keinen Branch löschen ohne weitere Freigabe.

Umgesetzt:

- Dependabot Security Alerts wurden per `gh api` lesend geprüft.
- Alle offenen Alerts beziehen sich auf transitive Maven-Abhängigkeiten aus
  `settings.gradle.kts` bzw. Gradle-/Android-Gradle-Plugin-Tooling, nicht auf
  ausgelieferte App-Runtime-Abhängigkeiten.
- `org.gradle.toolchains.foojay-resolver-convention` wurde aus
  `settings.gradle.kts` entfernt. Das Plugin war nicht für den aktuellen Build
  nötig und brachte shadowed Netty-Abhängigkeiten mit.
- In `build.gradle.kts` erzwingt der Buildscript-Classpath gepatchte Versionen
  der betroffenen AGP-Tooling-Abhängigkeiten:
  - `org.bouncycastle:bcpkix-jdk18on:1.84`
  - `org.bouncycastle:bcprov-jdk18on:1.84`
  - `org.bouncycastle:bcutil-jdk18on:1.84`
  - `org.bitbucket.b_c:jose4j:0.9.6`
  - `org.jdom:jdom2:2.0.6.1`
  - `org.apache.commons:commons-lang3:3.18.0`
- `./gradlew buildEnvironment` zeigt die gepatchten Versionen im
  Buildscript-Classpath.
- `./gradlew app:dependencies --configuration debugRuntimeClasspath` zeigte
  keine Treffer für die gemeldeten Alert-Pakete.

Container-Prüfungen:

- `./gradlew buildEnvironment` erfolgreich
- `./gradlew app:dependencies --configuration debugRuntimeClasspath`
  erfolgreich
- `./gradlew testDebugUnitTest` erfolgreich
- `./gradlew lintDebug` im ersten parallelen Lauf mit
  Ressourcen-Linking-Fehler fehlgeschlagen; direkt danach als Einzelaufruf
  erfolgreich.

Host-Prüfungen:

- `./gradlew lintRelease` erfolgreich
- `./gradlew testDebugUnitTest` erfolgreich
- `./gradlew clean assembleRelease` erfolgreich
- `./gradlew installRelease` erfolgreich; Installation auf einem Gerät bestätigt
- Manueller Smoke-Test der gestarteten App erfolgreich.

Hinweise:

- Die Host-Bestätigung für Build, Unit-Tests und Installation liegt vor.
- Die gemeldeten Deprecation-, SDK-Pfad- und Symbol-Stripping-Warnungen waren
  nicht blockierend und liegen außerhalb des Scopes von Issue 070.
- GitHub Dependabot Alerts beziehen sich auf den Default-Branch und werden erst
  nach Veröffentlichung der Änderung neu bewertet.
