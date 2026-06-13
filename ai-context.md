# AI Context – SwitchWerk

## Projekt

SwitchWerk ist eine Android-App für Teamwerk.

Die App verbindet sich mit dem WLAN eines Zielgeräts (z. B. Shelly), führt HTTP- oder RPC-Aufrufe aus und unterstützt die Verwaltung mehrerer Geräte und WLAN-Profile.

Ziel ist eine einfache, robuste, sichere und cloudfreie Lösung.

---

## Zwingend zu lesende Dateien

Vor jeder Analyse, Planung oder Implementierung zuerst lesen:

- ai-context.md
- AGENTS.md
- AI_SESSION_PROMPT.md
- AI_HANDOFF.md
- GITHUB_WORKFLOW.md
- ARCHITECTURE.md
- CODE_STYLE.md
- TESTING.md
- SECURITY.md
- README.md

Bei Build- oder Android-Konfiguration zusätzlich lesen:

- settings.gradle.kts
- build.gradle.kts
- app/build.gradle.kts
- gradle/libs.versions.toml
- gradle.properties

Bei Issue-Arbeiten zusätzlich lesen:

- docs/issues/overview.txt
- relevante Dateien unter docs/issues

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

Für jedes neue Issue:

1. Auf main wechseln

       git switch main

2. Aktuellen Stand holen

       git pull

3. Passendes Issue unter docs/issues bestimmen

4. Prüfen, ob bereits ein passendes GitHub-Issue oder ein Feature-Branch
   existiert

5. GitHub-Issue nur dann aus der Datei erzeugen, wenn noch keines existiert

       gh issue create \
         --title "Titel" \
         --body-file docs/issues/xxx.md

6. Eigenen Branch anlegen oder vorhandenen passenden Branch verwenden

       git switch -c fachlicher-branch-name

7. Implementieren

8. Build ausführen

       ./gradlew clean assembleDebug
       ./gradlew installDebug

9. Ohne ausdrücklichen Auftrag nicht committen, pushen oder einen Pull Request
   erstellen.

10. Erst nach ausdrücklicher Veröffentlichungsanforderung committen, Branch
   pushen und Pull Request erstellen.

11. Erst nach separater ausdrücklicher Merge-Freigabe nach `main` mergen.

12. Nach dem Merge die lokale Issue-Datei abhaken.

13. Zugehöriges GitHub-Issue schließen.

14. Feature-Branch lokal und remote löschen.

---

## Aktueller Stand

Abgeschlossen:

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

Offen:

- 011 Device Action With WiFi Fallback
- 012 Import/Export
- 013 QR Code Import
- 014 Dashboard Device Reordering
- 015 WiFi Profile Dialog Management
- 016 Edit Items By Name Click
- 017 Unique WiFi Profile Name
- 018 Adaptive WiFi Security Fallback
- 019 Configurable WiFi List Sorting
- 020 Device Assigned WiFi Order

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
