# Prompt für die nächste AI-Session

Diese Datei ist die wiederverwendbare Startvorlage für neue AI-Sessions.

`AGENTS.md` enthält die verbindlichen Regeln und hat bei Abweichungen Vorrang.
`AI_HANDOFF.md` enthält den aktuellen Arbeitsstand.

```text
Analysiere den aktuellen Stand und bearbeite den erteilten Auftrag.

WICHTIG:
Lies zuerst vollständig:

* AGENTS.md
* AI_HANDOFF.md

Lies danach nur die Dateien, die für die konkrete Aufgabe nötig sind.

Bei Issue-Arbeiten zusätzlich lesen:

* docs/issues/overview.txt
* die konkrete relevante Datei unter docs/issues/...

Bei Bedarf zusätzlich lesen:

* ai-context.md, wenn dauerhafter Projektkontext oder Issue-Status benötigt wird
* GITHUB_WORKFLOW.md, wenn Branch-, Issue-, PR- oder Merge-Schritte betroffen sind
* ARCHITECTURE.md, wenn Architektur, Packages oder Schichten betroffen sind
* CODE_STYLE.md, wenn Code geändert wird
* TESTING.md, wenn Tests geplant, geändert oder bewertet werden
* SECURITY.md, wenn Berechtigungen, Netzwerk, Speicherung oder sensible Daten betroffen sind

Bei Android-, Build- oder Dependency-Fragen zusätzlich prüfen:

* settings.gradle.kts
* build.gradle.kts
* app/build.gradle.kts
* gradle/libs.versions.toml
* gradle.properties

Vermeide unnötigen Kontextverbrauch:

* keine vollständige Repository-Analyse ohne ausdrücklichen Auftrag
* keine pauschale Suche über alle Dateien, wenn die betroffenen Pfade ableitbar sind
* erst vorhandene Handoff- und Issue-Hinweise auswerten
* danach gezielt nur betroffene Dateien lesen

Halte dich an die bestehende SwitchWerk-Architektur:

* Kotlin
* Jetpack Compose
* MVVM
* Koin
* Coroutines und Flow
* Room nur für notwendige lokale Speicherung
* keine Netzwerklogik in UI oder Composables
* keine sensiblen Daten loggen
* keine Cloud-, Tracking- oder Account-Abhängigkeiten ohne ausdrücklichen Auftrag

Arbeitsmodell:

* ChatGPT Browser: Planung, Architekturfragen, Issue-Zuschnitt und Dokumentations-Review
* Codex CLI im Docker-Container: konkrete Codeänderungen mit minimalem Kontext
* Ubuntu-Host: Android Studio, Gradle-Builds, ADB, Installation und Gerätetests

Bei der Implementierung eines neuen fachlichen Issues:

1. Prüfe Git-Status, aktuellen Branch und vorhandene Änderungen.
2. Wechsle auf main.
3. Hole den aktuellen Stand mit git pull.
4. Bestimme das nächste offene Issue aus docs/issues.
5. Prüfe, ob das zugehörige GitHub-Issue oder der Feature-Branch bereits existiert.
6. Erzeuge nur dann ein GitHub-Issue aus der lokalen Issue-Datei, wenn noch keines existiert.
7. Lege danach einen eigenen fachlichen Branch an oder verwende den vorhandenen Branch.
8. Zeige den vereinbarten Issue-Scope an.
9. Analysiere nur die betroffenen Architektur-, Package- und Pattern-Dateien.
10. Implementiere ausschließlich den vereinbarten Issue-Scope.
11. Prüfe Änderungen und Diff.
12. Führe nur verfügbare und sinnvolle Prüfungen in der aktuellen Umgebung aus.
13. Aktualisiere AI_HANDOFF.md im Hauptverzeichnis.

Jeder aktualisierte AI_HANDOFF.md muss für die nächste Session ausdrücklich auf
AI_SESSION_PROMPT.md als wiederverwendbare Startvorlage verweisen. Der Verweis
darf beim Ersetzen oder Kürzen des Handoffs nicht entfernt werden.

Nutze externe Repositories nur dann als funktionale Referenz, wenn der Auftrag
oder AI_HANDOFF.md dies ausdrücklich verlangt. Übernimm keinen fremden Code.

Nach der Implementierung ausgeben:

* Architekturentscheidung
* Liste aller geänderten Dateien
* Zusammenfassung des Diffs
* ausgeführte Prüfungen und deren Ergebnisse
* offene Probleme oder Annahmen
* Bewertung aller Akzeptanzkriterien
* vollständige Copy-&-Paste-Befehle für Build, Installation und manuelle Tests auf dem Host

Build und Installation auf dem Host mindestens mit:

./gradlew clean assembleDebug
./gradlew installDebug

Melde den Build erst als erfolgreich, nachdem der Benutzer ein erfolgreiches
Ergebnis vom Host zurückgemeldet hat.

Ohne ausdrückliche Anforderung des Benutzers:

* NICHT committen
* NICHT pushen
* KEINEN Pull Request erstellen
* NICHT mergen
* KEIN GitHub-Issue schließen
* KEINEN Branch löschen
* die lokale Issue-Datei NICHT abhaken

Veröffentlichung und Abschluss erfolgen ausschließlich nach ausdrücklicher
Freigabe und gemäß Phase 2 in AGENTS.md.

Aktualisiere ai-context.md nur, wenn sich dauerhafter Projektkontext oder der
dokumentierte Issue-Status tatsächlich geändert hat.
```
