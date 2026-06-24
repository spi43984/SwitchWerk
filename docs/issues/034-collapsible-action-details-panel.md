# Issue #34: Collapsible Action Details Panel

## Metadaten

- Status: Abgeschlossen
- Priorität: P1
- Typ: GUI / Dashboard

## Ziel

Der Aktionsdetails-Bereich im Dashboard soll platzsparender werden.

Wenn die Detailanzeige aktiviert ist, aber keine Aktion läuft und keine Meldungen angezeigt werden, soll der Bereich nur als eine kompakte Zeile am unteren Bildschirmrand sichtbar sein.

## Hintergrund

Issue 023 hat den Detailbereich grundsätzlich eingeführt. Auf kleinen Displays nimmt der Detailbereich jedoch dauerhaft Platz ein, auch wenn gerade keine Diagnoseinformationen relevant sind.

## Scope

### Minimierter Zustand

- Der Aktionsdetails-Bereich wird auf eine Zeile am unteren Bildschirmrand reduziert, wenn:
  - Detailanzeige aktiviert ist
  - keine Geräteaktion läuft
  - keine Aktionsmeldungen angezeigt werden
- Die Zeile zeigt mindestens den Titel `Aktionsdetails`
- Die Bedienung des Dashboards bleibt dadurch besser nutzbar

### Automatisches Öffnen

Der Bereich öffnet automatisch auf die konfigurierte Höhe aus Issue 023, wenn:

- eine Geräteaktion gestartet wird
- neue Aktionsmeldungen angezeigt werden

### Manuelles Öffnen und Minimieren

- Klick auf die minimierte Zeile öffnet den Bereich auf die konfigurierte Höhe
- Klick auf den Titel `Aktionsdetails` im geöffneten Bereich minimiert den Bereich wieder
- Minimieren entfernt keine Meldungen
- Der Mülleimer-Button aus Issue 023 bleibt für das Leeren der Meldungen zuständig

### App verlassen

- Wenn die App verlassen wird, wird der Aktionsdetails-Bereich wieder minimiert
- Die Einstellung zur Detailanzeige bleibt unverändert
- Die konfigurierbare Höhe bleibt unverändert

## Nicht im Scope

- Änderung der WLAN-Logik
- Änderung der Geräteaktionslogik
- Änderung der Diagnosemeldungen
- Persistentes Speichern des geöffnet/minimiert-Zustands
- Export oder Import des minimiert/geöffnet-Zustands
- Änderung der Detailbereich-Höheneinstellung aus Issue 023

## Architektur

- Änderungen erfolgen in der Compose-Schicht des Dashboards
- Bestehende ViewModels und Repositories sollen möglichst unverändert bleiben
- Falls UI-Zustand benötigt wird, soll dieser lokal im Dashboard-UI-State gehalten werden
- Die bestehende Einstellung `Detailanzeige ein/aus` bleibt führend

## Akzeptanzkriterien

- [x] Detailanzeige aus: kein Aktionsdetails-Bereich sichtbar
- [x] Detailanzeige ein und keine Meldungen: nur eine kompakte Zeile sichtbar
- [x] Start einer Geräteaktion öffnet den Bereich automatisch
- [x] Neue Aktionsmeldung öffnet den Bereich automatisch
- [x] Klick auf minimierte Zeile öffnet den Bereich
- [x] Klick auf Titel `Aktionsdetails` minimiert den Bereich
- [x] Minimieren löscht keine Meldungen
- [x] Mülleimer-Button löscht weiterhin nur die Meldungen
- [x] Beim Verlassen der App wird der Bereich minimiert
- [x] Konfigurierte Höhe 20 % / 30 % / 40 % wird beim Öffnen weiterhin respektiert
- [x] Bedienung bleibt auf kleinen Displays nutzbar
- [x] Dark Mode und Light Mode funktionieren weiterhin
- [x] Build erfolgreich

## Abschluss

- GitHub-Issue: #104
- Pull Request: #105
- Merge-Commit: `30ee5e8`
- Der Bereich verwendet ausschließlich lokalen Compose-UI-State. Er wird bei
  neuen Meldungen oder einer laufenden Aktion geöffnet und bei `ON_PAUSE`
  minimiert; Einstellungen, Diagnosemeldungen und Aktionslogik bleiben
  unverändert.
- Die Host-Prüfungen für Build, Installation und die manuellen Tests wurden
  erfolgreich bestätigt.

## Testhinweise

- Detailanzeige deaktivieren
- Detailanzeige aktivieren ohne Meldungen
- Aktion erfolgreich ausführen
- Aktion mit WLAN-Fehler ausführen
- Aktion mit HTTP-Fehler ausführen
- Minimierte Zeile anklicken
- Titel `Aktionsdetails` anklicken
- Meldungen leeren
- App verlassen und wieder öffnen
- Höhe 20 %, 30 % und 40 % testen
- Portrait-Modus testen
- Landscape-Modus testen
