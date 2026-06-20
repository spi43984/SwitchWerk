# AI Context – SwitchWerk

## Projekt

SwitchWerk ist eine Android-App für Teamwerk.

Die App verbindet sich mit dem WLAN eines Zielgeräts (z. B. Shelly), führt HTTP- oder RPC-Aufrufe aus und unterstützt die Verwaltung mehrerer Geräte und WLAN-Profile.

Ziel ist eine einfache, robuste, sichere und cloudfreie Lösung.

---

## Zweck dieser Datei

`ai-context.md` enthält dauerhaften Projektkontext, der über einzelne Sessions hinaus gültig bleibt.

Diese Datei ist nicht als vollständiger Startprompt für Codex gedacht. Codex liest sie nur, wenn dauerhafter Projektkontext, Issue-Status oder Projektentscheidungen für die konkrete Aufgabe benötigt werden.

---

## KI-Arbeitsmodell

- ChatGPT Browser: Planung, Architekturfragen, Issue-Zuschnitt, Dokumentations-Review und größere Analysen.
- Codex CLI im Docker-Container: konkrete, abgegrenzte Codeänderungen mit minimalem Kontext.
- Ubuntu-Host: Android Studio, Gradle-Builds, ADB, Installation und Gerätetests.
- GitHub: zentrale Quellcodeverwaltung, Issues, Branches und Pull Requests.

Projektwissen soll dauerhaft in Markdown-Dateien dokumentiert werden und nicht nur in Codex-Sessions liegen.

---

## Kontextdateien im Repository-Root

Die zentralen Markdown-Dateien bleiben im Hauptverzeichnis des Repositorys:

- `AGENTS.md`: verbindliche Regeln für KI-Agenten
- `AI_HANDOFF.md`: aktueller Übergabestand für die nächste Session
- `AI_SESSION_PROMPT.md`: wiederverwendbare Startvorlage
- `GITHUB_WORKFLOW.md`: GitHub-, Branch-, Issue- und PR-Ablauf
- `ARCHITECTURE.md`: Architektur und Schichten
- `CODE_STYLE.md`: Kotlin- und Compose-Stil
- `TESTING.md`: Teststrategie
- `SECURITY.md`: Sicherheits- und Datenschutzregeln
- `README.md`: Einstieg für Menschen

Bei Issue-Arbeiten zusätzlich lesen:

- `docs/issues/overview.txt`
- die konkrete relevante Datei unter `docs/issues`

Bei Build- oder Android-Konfiguration zusätzlich lesen:

- `settings.gradle.kts`
- `build.gradle.kts`
- `app/build.gradle.kts`
- `gradle/libs.versions.toml`
- `gradle.properties`

---

## Technologie

- Kotlin
- Jetpack Compose
- MVVM
- Coroutines
- Room
- Koin
- Android Studio
- GitHub Actions

---

## Architekturprinzipien

- Clean und wartbarer Code
- MVVM strikt einhalten
- UI und Business-Logik trennen
- Bestehende Architektur weiterentwickeln
- Keine unnötigen Framework-Wechsel
- Keine unnötigen Abhängigkeiten
- Sicherheit vor Komfort
- Lokale Datenspeicherung bevorzugen
- Cloud-Anbindung nur wenn ausdrücklich erforderlich

---

## Entwicklungsworkflow

Der verbindliche Workflow steht in `AGENTS.md` und `GITHUB_WORKFLOW.md`.

Kurzfassung:

1. Nicht direkt auf `main` implementieren.
2. Passendes Issue nach `docs/issues/overview.txt` bestimmen: zuerst Status `offen`, dann Priorität `P0` bis `P4`, danach Issue-ID aufsteigend.
3. Lokale Issue-Dateien dürfen als Planung angelegt werden, ohne sofort ein GitHub-Issue zu erstellen.
4. Erst wenn ein konkretes Issue implementiert werden soll, prüfen, ob dafür bereits ein GitHub-Issue oder Feature-Branch existiert.
5. Nur vor Beginn dieser Implementierung und nur falls noch kein passendes GitHub-Issue existiert, ein GitHub-Issue aus der lokalen Issue-Datei erzeugen.
6. Eigenen fachlichen Branch anlegen oder vorhandenen passenden Branch verwenden.
7. Scope eng halten.
8. Änderungen prüfen.
9. Build, Installation und Gerätetests auf dem Ubuntu-Host bestätigen lassen.
10. Ohne ausdrückliche Freigabe nicht veröffentlichen, pushen, PR erstellen oder mergen.
11. Nach erfolgreichem Merge immer `docs/issues/overview.txt` und `ai-context.md` aktualisieren, bevor das GitHub-Issue geschlossen wird.

---

## Aktueller Stand

### Abgeschlossen

- 001 Configuration Domain Foundation
- 002 Dashboard Device List
- 003 Settings Navigation Foundation
- 004 UI Foundation Cleanup
- 005 Local Persistence Foundation
- 006 Encrypted WiFi Password Storage
- 007 WiFi Profile Management
- 008 Device Management
- 009 WiFi Connection Service
- 010 HTTP/API Call Service
- 011 Device Action With WiFi Fallback
- 012 Import/Export
- 013 QR Code Import
- 014 Dashboard Device Reordering
- 015 WiFi Profile Dialog Management
- 016 Edit Items By Name Click
- 017 Unique WiFi Profile Name
- 018 Adaptive WiFi Security Fallback
- 023 Settings Display And Action Details
- 027 WiFi Timeout Analysis And Stabilization
- 028 Theme Mode Setting

### Offen

- 019 Configurable WiFi List Sorting
- 020 Device Assigned WiFi Order
- 021 HTTP/HTTPS Device Actions
- 022 Request Body And Content-Type Support
- 025 Dashboard Widget Layout
- 026 Settings UI Rework
- 029 Language Setting
- 030 WiFi Profile Deletion Safety
- 031 Import Enforces Unique WiFi Profile Names
- 032 Room Schema And Migration Test Coverage
- 033 Android-managed WiFi networks
- 034 Collapsible Action Details Panel
- 035 App Identity, Help And Release Packaging

### Zurückgestellt / Backlog

- 024 Authenticated Import Sources Backlog

### Nächstes Issue

- 026 Settings UI Rework

### Pflegehinweis

Die Listen "Abgeschlossen", "Offen", "Zurückgestellt / Backlog" und "Nächstes Issue" sind nach jedem abgeschlossenen oder neu angelegten Issue in `ai-context.md` zu aktualisieren.

Nach erfolgreichem Build, Installation, Merge und vor dem Schließen des GitHub-Issues müssen außerdem `docs/issues/overview.txt` und die passende Datei unter `docs/issues` aktualisiert werden.

---

## UI-Regeln

Bei Sicherheitsabfragen steht die sichere Abbruchaktion rechts.

Beispiele:

- links: Ja, Löschen, Bestätigen
- rechts: Nein, Abbrechen

---

## Zielgeräte

Primär:

- Shelly 1 Mini Gen3

Später möglich:

- weitere Shelly-Geräte
- HTTP-API-Geräte
- REST-API-Geräte

---

## Randbedingungen

- Android 16 als Referenzplattform
- Entwicklung auf Ubuntu Linux
- GitHub als zentrale Quellcodeverwaltung
- App soll auch von technisch weniger versierten Vereinsmitgliedern nutzbar sein
