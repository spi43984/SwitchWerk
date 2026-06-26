# Issue 058: Setup Wizard / Einrichtungs-Assistent

## Metadaten

- Status: Offen
- Priorität: P0
- Typ: UX / Onboarding
- Bereich: App-Start / Einstellungen / Hilfe

## Ziel

Beim ersten Start der App soll der Benutzer durch einen kurzen Einrichtungs-Assistenten geführt werden. Der Wizard dient ausschließlich der Einführung in die Bedienung und soll keine Konfiguration erzwingen.

Der Wizard erscheint außerdem erneut, wenn die Konfiguration zurückgesetzt wurde.

Der Benutzer kann:

- den Wizard überspringen, wodurch er beim nächsten App-Start erneut angezeigt wird
- den Wizard dauerhaft ausblenden über **Nicht mehr anzeigen**

## Hintergrund

Neue Benutzer sollen beim ersten Start nachvollziehen können, wofür SwitchWerk gedacht ist und wie die App grundsätzlich verwendet wird.

SwitchWerk verbindet sich mit WLAN-Geräten, die nicht Teil anderer Netzwerke sind, sondern ein eigenes WLAN bereitstellen. Danach löst die App konfigurierte Aktionen über HTTP/RPC aus.

Der Wizard soll erklären:

- was SwitchWerk grundsätzlich macht
- wie vorbereitete Konfigurationen importiert werden
- wie WLAN-Profile und Geräte manuell angelegt werden
- wie der Wizard später erneut aufgerufen werden kann

## Scope

### Anzeige

- Vollbild bzw. nahezu Vollbild.
- Titel:
  - Deutsch: **Einrichtungs-Assistent**
  - Englisch: **Setup Wizard**
- Optisch drei getrennte, scrollbare Blöcke.
- Zwei Buttons am Ende des Dialogs:
  - links: **Überspringen**
  - rechts: **Nicht mehr anzeigen**

### Block 1: Einführung

Beschreibung der App:

> SwitchWerk schaltet WLAN-Geräte, die nicht Teil anderer Netzwerke und darüber erreichbar sind. Sofern diese Geräte ein eigenes kleines WLAN aufspannen, kann SwitchWerk sich mit diesem verbinden und Aktionen auslösen. Dazu ist die Kenntnis erforderlich, welcher Art Aktionen diese Geräte unterstützen - Informationen dazu finden sich üblicherweise beim Hersteller oder entsprechenden Internetforen.

Zusätzlich kurze Beschreibung der grundsätzlichen Funktionsweise:

1. Im Menü **Einstellungen → WLAN-Profile** werden die WLANs eingerichtet, mit denen sich SwitchWerk verbinden soll.
2. Im Menü **Einstellungen → Geräte** werden die Geräte angelegt und einem WLAN-Profil zugeordnet.
3. Auf der **Hauptseite der App (Dashboard)** können anschließend die konfigurierten Aktionen ausgelöst werden.

Dieser Block dient ausschließlich der Erklärung der Funktionsweise und enthält bewusst keine Links.

### Block 2: Konfiguration importieren

Kurze Erklärung:

- SwitchWerk kann eine vorbereitete Konfiguration importieren.
- Dadurch lassen sich WLAN-Profile, Geräte und Einstellungen übernehmen.
- Dies ist der empfohlene Weg für bereits vorbereitete Installationen.

Link:

- **Backup öffnen**

Beim Verlassen der Backup-Seite über Zurück-Pfeil oder Zurück-Geste wird wieder der Wizard angezeigt, solange dieser noch nicht geschlossen wurde.

### Block 3: Manuelle Einrichtung

Kurze Erklärung:

- WLAN-Profile manuell anlegen.
  - Link: **WLAN-Profile öffnen**
- Geräte anlegen und einem WLAN-Profil zuordnen.
  - Link: **Geräte öffnen**
- Anschließend können die Geräte auf der Hauptseite (Dashboard) geschaltet werden.
  - Link: **Dashboard öffnen**

Beim Verlassen einer geöffneten Seite über Zurück-Pfeil oder Zurück-Geste wird wieder der Wizard angezeigt, solange dieser noch nicht geschlossen wurde.

### Zusätzliche Einstellungen

Unter **Einstellungen → System** einen neuen Eintrag hinzufügen:

- **Einrichtungs-Assistent erneut anzeigen**

Zusätzlich denselben Eintrag im Bereich **Hilfe** anzeigen.

Beim Auswählen wird der Wizard sofort erneut geöffnet.

### Verhalten

Wizard erscheint:

- beim ersten Start der App
- nach **Konfiguration zurücksetzen**

Wizard erscheint nicht:

- wenn zuvor **Nicht mehr anzeigen** gewählt wurde

Bei Auswahl von **Überspringen**:

- Wizard wird geschlossen
- keine dauerhafte Deaktivierung
- erneute Anzeige beim nächsten App-Start

Bei Auswahl von **Nicht mehr anzeigen**:

- Wizard wird dauerhaft deaktiviert
- erneute Anzeige nur nach **Konfiguration zurücksetzen** oder über **Einrichtungs-Assistent erneut anzeigen**

### Navigation

Während der Wizard geöffnet ist:

- Links aus Block 2 und Block 3 öffnen die vorhandenen Seiten der App.
- Beim Verlassen dieser Seiten über Zurück-Pfeil oder Zurück-Geste kehrt der Benutzer wieder in den Wizard zurück.
- Erst nach Auswahl von **Überspringen** oder **Nicht mehr anzeigen** wird der Wizard geschlossen.

## Nicht im Scope

- Neue Import-/Export-Formate
- Änderung der Geräte- oder WLAN-Profile-Datenmodelle
- Änderung der HTTP/RPC-Ausführungslogik
- Änderung des Dashboard-Layouts außerhalb der Wizard-Verlinkung
- Cloud-Synchronisation
- GitHub-Issue, Branch, Pull Request oder Merge

## Architekturhinweise

- Bestehende Compose-, Navigation- und Settings-Struktur beibehalten.
- Wizard-State persistent speichern.
- Der Wizard soll über App-Start, Konfigurationsreset, **Einstellungen → System** und **Hilfe** erneut aufrufbar sein.
- Texte müssen internationalisierbar sein.
- Bei Dialog-Buttons gilt die bestehende UI-Regel: sichere Abbruchaktion rechts.
- Keine neue externe Abhängigkeit einführen.

## Akzeptanzkriterien

- [ ] Wizard erscheint beim ersten Start automatisch.
- [ ] Wizard erscheint nach **Konfiguration zurücksetzen**.
- [ ] Wizard nutzt Vollbild- bzw. nahezu Vollbild-Darstellung.
- [ ] Drei klar getrennte, scrollbare Informationsblöcke sind vorhanden.
- [ ] Block 1 erklärt Zweck und grundsätzliche Funktionsweise der App.
- [ ] Block 1 enthält keine Links.
- [ ] Block 1 verwendet die Bezeichnungen **Einstellungen → WLAN-Profile**, **Einstellungen → Geräte** und **Hauptseite der App (Dashboard)**.
- [ ] Block 2 erklärt den Import einer Konfiguration.
- [ ] Block 2 enthält einen Link zum Bereich **Backup**.
- [ ] Block 3 erklärt die manuelle Einrichtung.
- [ ] Jeder Schritt in Block 3 besitzt einen Link zur passenden Seite.
- [ ] Rückkehr von einer geöffneten Seite führt wieder in den Wizard.
- [ ] Button **Überspringen** schließt den Wizard ohne dauerhafte Deaktivierung.
- [ ] Button **Nicht mehr anzeigen** deaktiviert den Wizard dauerhaft.
- [ ] Unter **Einstellungen → System** existiert der Eintrag **Einrichtungs-Assistent erneut anzeigen**.
- [ ] Derselbe Eintrag ist zusätzlich im Bereich **Hilfe** vorhanden.
- [ ] Alle Texte sind internationalisierbar Deutsch/Englisch.
