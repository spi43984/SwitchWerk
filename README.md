# SwitchWerk

SwitchWerk is an Android app to control Shelly and other local API devices.

## Zweck

Die App verbindet sich mit dem WLAN eines Zielgeräts, zum Beispiel einem Shelly,
und führt lokale HTTP- oder RPC-Aufrufe aus. Ziel ist eine einfache, robuste,
sichere und cloudfreie Lösung für lokale Geräteaktionen.

## Technologie

- Kotlin
- Jetpack Compose
- MVVM
- Koin
- Coroutines und Flow
- Room für notwendige lokale Speicherung
- GitHub Actions

## Externe Geräteaktionen

Externe Intents sind standardmäßig deaktiviert und müssen unter
`Einstellungen → System → Externe Intents` freigegeben werden. Eine andere App
kann danach eine bereits lokal konfigurierte Geräteaktion über deren stabile ID
starten. URLs, RPC-Befehle, HTTP-Bodies und zusätzliche Extras werden nicht
akzeptiert.

Die Geräte-ID steht beim jeweiligen Gerät im Feld `id` einer exportierten
SwitchWerk-Konfiguration. Der Intent startet die `MainActivity`; SwitchWerk wird
daher im Vordergrund geöffnet und zeigt dort Fortschritt und Ergebnis an.

```bash
adb shell am start -n de.piecha.switchwerk/.MainActivity \
  -a de.piecha.switchwerk.action.RUN_DEVICE_ACTION \
  --es de.piecha.switchwerk.extra.DEVICE_ID device-123
```

Action: `de.piecha.switchwerk.action.RUN_DEVICE_ACTION`

Pflicht-Extra: `de.piecha.switchwerk.extra.DEVICE_ID` (String)

## Entwicklung

Die Entwicklung erfolgt auf Ubuntu Linux mit Android Studio.

Debug-Build und -Installation auf dem Host:

```bash
./gradlew lintDebug
./gradlew testDebugUnitTest
./gradlew clean assembleDebug
./gradlew installDebug
```

Release-Build und -Installation bei konfigurierter Release-Signierung:

```bash
./gradlew lintRelease
./gradlew testDebugUnitTest
./gradlew clean assembleRelease
./gradlew installRelease
```

Einen Task `testReleaseUnitTest` gibt es in diesem Projekt nicht. Die gewählte
Installationsvariante muss zur Signatur der bereits installierten App passen.

ADB, Gerätetests und Android Studio laufen auf dem Ubuntu-Host, nicht im
Codex-Container.

## KI-Workflow

- ChatGPT Browser: Planung, Architekturfragen, Issue-Zuschnitt und Dokumentations-Review.
- Codex CLI im Docker-Container: konkrete Codeänderungen mit minimalem Kontext.
- GitHub: Issues, Branches, Pull Requests und Review.
- Ubuntu-Host: Gradle-Builds, ADB, Installation und Gerätetests.

Codex soll nicht pauschal das gesamte Repository analysieren. Für neue Aufgaben
zuerst `AGENTS.md` und `AI_HANDOFF.md` lesen. Bei Issue-Arbeiten zusätzlich die
konkrete Issue-Datei unter `docs/issues` lesen.

## Wichtige Dokumente

Die zentralen Markdown-Dateien liegen im Repository-Root:

- `AGENTS.md`: verbindliche Regeln für KI-Agenten
- `ai-context.md`: dauerhafter Projektkontext
- `AI_SESSION_PROMPT.md`: wiederverwendbare Startvorlage für neue AI-Sessions
- `AI_HANDOFF.md`: aktueller Übergabestand
- `GITHUB_WORKFLOW.md`: GitHub-, Branch-, Issue- und PR-Ablauf
- `ARCHITECTURE.md`: Architektur und Schichten
- `CODE_STYLE.md`: Kotlin- und Compose-Stil
- `TESTING.md`: Teststrategie
- `SECURITY.md`: Sicherheits- und Datenschutzregeln

Fachliche Issues liegen unter `docs/issues`.
