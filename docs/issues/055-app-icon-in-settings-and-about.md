# 055 App Icon In Settings And About

## Metadaten

- Status: Offen
- Priorität: P0
- Typ: Branding / UI / Android Resources

## Ziel

Das Icon im Einstellungsmenü und im About-Menü soll durch das aktuelle App-Icon ersetzt werden.

Größe, Position, Abstände und bestehendes Layout sollen dabei unverändert bleiben.

## Hintergrund

Issue 054 hat das offizielle App-Icon ersetzt. In der App selbst werden im Einstellungs- und About-Bereich noch ältere bzw. separate Icon-Darstellungen verwendet. Diese sollen durch das App-Icon ersetzt werden, damit die App ein einheitliches Erscheinungsbild erhält.

## Scope

- Icon im Einstellungsmenü durch das aktuelle App-Icon ersetzen
- Icon im About-Menü durch das aktuelle App-Icon ersetzen
- Größe, Position und Abstände unverändert beibehalten
- Vorhandene App-Icon-Ressourcen wiederverwenden
- Version, Release-Datum und übriges Layout unverändert lassen

## Nicht im Scope

- Keine Änderung des Launcher-Icons
- Keine Änderung an App-Name, Paketname oder Versionierung
- Keine Änderung des Release-Prozesses
- Keine neuen Icon-Ressourcen erzeugen
- Keine Änderungen an WLAN-, Geräte-, HTTP- oder Backup-Funktionen

## Architekturhinweise

- Änderungen ausschließlich auf die betroffenen Compose-UI-Dateien beschränken
- Bestehende Ressourcenreferenzen bevorzugt weiterverwenden
- Keine Business-Logik ändern
- Kein neuer ViewModel-State erforderlich

## Akzeptanzkriterien

- [ ] Einstellungsmenü verwendet das aktuelle App-Icon.
- [ ] About-Menü verwendet das aktuelle App-Icon.
- [ ] Größe und Position bleiben unverändert.
- [ ] Version und Release-Datum bleiben unverändert.
- [ ] Launcher-Icon bleibt unverändert.
- [ ] Keine neue externe Abhängigkeit.
- [ ] Build erfolgreich.

## Testhinweise

- App starten.
- Einstellungsmenü öffnen und Icon prüfen.
- About-Menü öffnen und Icon prüfen.
- Größe, Position und Abstände mit der bisherigen Darstellung vergleichen.

Build:

    ./gradlew clean assembleDebug
    ./gradlew installDebug

## Abschlussnotizen

Nach der Implementierung ergänzen:

- betroffene Dateien:
- verwendete Icon-Ressource:
- manuelle Prüfung:
