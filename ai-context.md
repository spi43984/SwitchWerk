# AI Context – SwitchWerk

## Projekt

SwitchWerk ist eine Android-App für Teamwerk.

Die App verbindet sich mit dem WLAN eines Zielgeräts (z. B. Shelly), führt HTTP- oder RPC-Aufrufe aus und unterstützt die Verwaltung mehrerer Geräte und WLAN-Profile.

Ziel ist eine einfache, robuste, sichere und cloudfreie Lösung.

---

## Zweck dieser Datei

`ai-context.md` enthält dauerhaften Projektkontext, der über einzelne Sessions hinaus gültig bleibt.

Diese Datei ist nicht als vollständiger Startprompt für Codex gedacht. Codex liest sie nur, wenn dauerhafter Projektkontext oder Projektentscheidungen für die konkrete Aufgabe benötigt werden.

Status, Priorisierung und Reihenfolge der Issues stehen ausschließlich in `docs/issues/overview.txt`.

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
11. Nach erfolgreichem Merge immer `docs/issues/overview.txt` aktualisieren, bevor das GitHub-Issue geschlossen wird. `ai-context.md` nur bei geändertem dauerhaftem Projektkontext oder geänderten Projektentscheidungen anpassen.

---

## Issue-Status

Die vollständige Liste der offenen, abgeschlossenen und zurückgestellten Issues
sowie ihre Priorisierung und empfohlene Umsetzungsreihenfolge steht in
`docs/issues/overview.txt`. Sie wird nicht in dieser Datei dupliziert.

---

## UI-Regeln

Bei Sicherheitsabfragen steht die sichere Abbruchaktion rechts.

Beispiele:

- links: Ja, Löschen, Bestätigen
- rechts: Nein, Abbrechen

Die Hauptnavigation wird über das rechte Hamburger-Menü im Dashboard geöffnet.

Das Dashboard unterstützt eine persistierte Listen- und Widget-Darstellung.
Die Widget-Darstellung verwendet ein adaptives Compose-Grid und dieselbe über
`sortOrder` gespeicherte Gerätereihenfolge wie die Liste. Beide Darstellungen
verwenden kompakte Hoch-/Runter-Aktionen; Drag-and-Drop und freie
Widget-Positionen sind nicht Bestandteil des Layouts. Im Landscape-Modus wird
der Dashboard-Kopf in einer Zeile dargestellt und ein aktivierter
Aktionsdetailbereich nur visuell ausgeblendet, ohne die Benutzereinstellung zu
verändern. Die Dashboard-Darstellung ist Teil des Konfigurationsexports; fehlt
sie in älteren Importen, bleibt die aktuelle Benutzereinstellung erhalten.
Die Widget-Mindestbreite wird zur Laufzeit aus verfügbarer Dashboardbreite,
`fontScale` und Aktionsbeschriftungen bestimmt. Bei verkleinerter Systemschrift
gilt eine kompakte Basisskalierung von `0,75`; normale und größere Schrift wird
weiter proportional berücksichtigt. Passen zwei Widgets nicht sauber
nebeneinander, wird die Umschaltung zwischen Liste und Widgets ausgeblendet,
ohne die persistierte Auswahl zu verändern. Gerätenamen dürfen in Widgets
maximal zweizeilig sein, Aktionsbeschriftungen bleiben einzeilig.
Die Layoutdiagnose ist als ältester, nicht persistierter Eintrag in den
Aktionsdetails verfügbar. Sie scrollt mit den echten Ereignissen und erscheint
nach dem Löschen des Aktionsprotokolls wieder zuerst.

Die Einstellungen sind in die exklusiv sichtbaren Bereiche WLAN-Profile,
Geräte, System und Backup gegliedert. Gemeinsame Dialog-, Button-, Tab- und
Hilfe-Komponenten liegen unter `ui/components/`. Hamburger-Menü und
System-Einstellungen öffnen dieselbe Hilfeansicht.

Die App unterstützt eine persistierte Sprachauswahl für System, Deutsch und
Englisch über dieselbe `AppSettings`-Infrastruktur wie den Theme-Modus. Sichtbare
App-Texte liegen in Android-String-Ressourcen; Gerätenamen, WLAN-Namen, Hosts,
URLs, HTTP/RPC-Befehle und sonstige Benutzereingaben werden nicht übersetzt.

Die einem Gerät zugeordneten WLAN-Profile werden über Hoch/Runter-Pfeile in die
verbindliche Verbindungsreihenfolge gebracht. Diese robuste Bedienung wurde nach
Gerätetests bewusst gegenüber Drag-and-Drop gewählt.

Geräte-WLAN-Zuordnungen verwenden technisch die interne WLAN-Profil-ID, nicht
den Profilnamen. Beim Merge-Import gilt eine gleiche WLAN-Profil-ID als
Identität: Das vorhandene lokale Profil wird überschrieben beziehungsweise
aktualisiert, auch wenn der Profilname abweicht. Ein gleicher Profilname mit
anderer Profil-ID wird beim Merge-Import abgelehnt. Importierte
Geräte-Zuordnungen bleiben auf die in der Importdatei referenzierten
Profil-IDs bezogen.

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
