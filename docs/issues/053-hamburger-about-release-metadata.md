# 053 Hamburger And About Release Metadata

## Metadaten

- Status: Abgeschlossen
- Priorität: P1
- Typ: GUI / Navigation / About / Release-Metadaten

## Ziel

Das offene Hamburger-Menü und das About-Menü sollen die aktuelle App-Version und das Release-Datum klar sichtbar anzeigen.

## Scope

### Hamburger-Menü

- Im geöffneten Hamburger-Menü ganz unten rechtsbündig anzeigen:
  - Version
  - Release-Datum
- Version und Datum stehen jeweils in einer eigenen Zeile.
- Darunter wird das App-Icon so angezeigt, dass es das Menü in der Breite ausfüllt.
- Die Darstellung muss in das Menü passen und darf keine Navigationseinträge verdrängen oder überdecken.
- Deutsch und Englisch konsistent pflegen.

### About-Menü

- Im About-Menü unterhalb der Version zusätzlich das Datum der Release-Version anzeigen.
- Die redundante Bezeichnung „Über SwitchWerk“ unterhalb des About-Icons entfällt;
  die Seitenüberschrift bleibt maßgeblich.
- Darstellung in Light Mode und Dark Mode prüfen.
- Das bestehende About-Icon weiterverwenden.

## Nicht im Scope

- Keine Änderung des Release-Prozesses.
- Keine automatische Update-Prüfung.
- Keine neue externe Abhängigkeit.
- Keine Änderung an WLAN-, Geräte- oder Aktionslogik.

## Architekturhinweise

- Änderung möglichst in der Compose-UI halten.
- Bestehende About-/Hilfe-Struktur und String-Ressourcen weiterverwenden.
- Release-Metadaten möglichst aus vorhandenen BuildConfig-/Versionsinformationen ableiten oder zentral definieren.
- Keine Netzwerklogik in Composables ergänzen.

## Akzeptanzkriterien

- [x] Hamburger-Menü zeigt unten rechtsbündig die Version.
- [x] Hamburger-Menü zeigt darunter das Release-Datum.
- [x] Hamburger-Menü zeigt darunter klein das bestehende About-Icon.
- [x] About-Menü zeigt unterhalb der Version das Release-Datum.
- [x] Texte sind auf Deutsch und Englisch konsistent gepflegt.
- [x] Darstellung passt in Portrait und Landscape.
- [x] Light Mode und Dark Mode funktionieren weiterhin.
- [x] Build erfolgreich.

## Testhinweise

- App starten und Hamburger-Menü öffnen.
- Prüfen, dass Version und Release-Datum unten rechtsbündig sichtbar sind.
- Prüfen, dass das kleine About-Icon darunter sichtbar ist und nicht überläuft.
- About-Menü öffnen und Release-Datum unterhalb der Version prüfen.
- Portrait und Landscape testen.
- Deutsch und Englisch testen.
- Light Mode und Dark Mode testen.

## Abschluss

- GitHub-Issue: #112
- Pull Request: #113
- Merge-Commit: `d01d402`
- Host-Build, Installation und die manuellen Prüfungen wurden bestätigt.
