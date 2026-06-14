# AI Handoff

Stand: 14. Juni 2026

## Abgeschlossene Arbeit

Issue 011 "Device Action With WiFi Fallback" ist implementiert, geprüft,
veröffentlicht und nach `main` gemergt.

- GitHub-Issue: #21
- Pull Request: #23
- Merge-Commit: `39f18e026605c93f51414d22550dd3e6f60a0755`

## Aktuelle Dokumentationsänderung

Dieser Branch aktualisiert ausschließlich Markdown-Dokumentation, um den
Kontextverbrauch von Codex zu reduzieren.

Branch:

```text
docs-codex-cost-optimization
```

Ziele:

- zentrale Markdown-Dateien im Repository-Root belassen
- Verweise auf Root-Dateien konsistent halten
- pauschale Pflichtlektüre für Codex reduzieren
- Rollen von ChatGPT Browser, Codex CLI, GitHub und Ubuntu-Host klarer trennen
- dauerhaftes Projektwissen dokumentieren, statt es nur in Codex-Sessions zu halten

Es wurden keine App-Code-Dateien geändert.

## Geänderte Root-Markdown-Dateien

```text
AGENTS.md
AI_HANDOFF.md
AI_SESSION_PROMPT.md
GITHUB_WORKFLOW.md
README.md
ai-context.md
```

## Wichtige Dokumentationsentscheidungen

- `AGENTS.md` bleibt die verbindliche Regeldatei für KI-Agenten.
- `ai-context.md` bleibt dauerhafter Projektkontext.
- `AI_SESSION_PROMPT.md` bleibt die wiederverwendbare Startvorlage für neue AI-Sessions.
- `AI_HANDOFF.md` bleibt der aktuelle Übergabestand.
- Alle diese Dateien bleiben im Hauptverzeichnis des Repositorys.
- Dateien unter `docs/issues` bleiben weiterhin die fachliche Issue-Planung.

## Codex-Minimalkontext

Für neue Codex-Aufgaben gilt:

1. zuerst `AGENTS.md` und `AI_HANDOFF.md` lesen
2. bei Issue-Arbeiten zusätzlich `docs/issues/overview.txt` und die konkrete Issue-Datei lesen
3. danach nur gezielt weitere Dateien lesen, wenn sie für die konkrete Aufgabe nötig sind
4. keine vollständige Repository-Analyse ohne ausdrücklichen Auftrag

## Umgebung und Rollen

- ChatGPT Browser: Planung, Architekturfragen, Issue-Zuschnitt, Dokumentations-Review
- Codex CLI im Docker-Container: konkrete Codeänderungen mit minimalem Kontext
- Ubuntu-Host: Android Studio, Gradle-Builds, ADB, Installation und Gerätetests
- GitHub: Issues, Branches, Pull Requests und Review

## Nächster fachlicher App-Schritt

Das nächste offene fachliche App-Issue ist:

```text
docs/issues/012-import-export.md
```

Vor Beginn einer neuen Implementierung `AGENTS.md`, `AI_HANDOFF.md` und bei Issue-Arbeiten die konkrete Issue-Datei lesen. `AI_SESSION_PROMPT.md` bleibt die wiederverwendbare Startvorlage für neue Sessions.

## Nicht erneut analysieren

Für das nächste fachliche App-Issue muss diese Dokumentationsoptimierung nicht erneut vollständig analysiert werden. Relevant ist nur, dass Codex künftig weniger pauschal lesen und stärker vom konkreten Issue ausgehen soll.
