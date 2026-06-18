# Issue #12: Import/Export

## Ziel

Konfigurationen austauschbar machen.

Die App soll bestehende Geräte- und WLAN-Konfigurationen exportieren und aus
vertrauenswürdigen Dateien oder URLs wieder importieren können.

## Scope

- Export von WLAN-Profilen und Geräten
- Export von Geräteaktionen inklusive Button-Beschriftung
- Export der einem Gerät zugewiesenen WLAN-Profile
- Erhalt der gespeicherten WLAN-Reihenfolge pro Gerät
- Import aus Datei
- Import aus URL
- JSON-basiertes, versioniertes Austauschformat
- WLAN-Passwörter werden standardmäßig nicht exportiert
- unverschlüsselter Export mit WLAN-Passwörtern ist optional möglich
- Import akzeptiert Dateien mit und ohne WLAN-Passwörter

## Passwort-Entscheidung

Der normale Export enthält keine WLAN-Passwörter.

Optional kann der Benutzer bewusst einen unverschlüsselten Export mit
WLAN-Passwörtern erstellen. Vor diesem Export muss deutlich gewarnt werden,
dass die Exportdatei WLAN-Passwörter im Klartext enthält und nur mit Personen
geteilt werden darf, die diese Passwörter kennen dürfen.

Beim Import werden WLAN-Passwörter akzeptiert, wenn sie in der Datei enthalten
sind. Vor dem Import einer Datei mit Passwörtern muss deutlich gewarnt werden,
dass die Datei WLAN-Passwörter im Klartext enthält und nur aus einer
vertrauenswürdigen Quelle importiert werden darf.

Importierte WLAN-Passwörter werden nicht im Klartext gespeichert, sondern über
den bestehenden sicheren Passwortspeicher der App abgelegt.

## Format-Hinweise

Das Austauschformat soll versioniert sein, z. B. über `schemaVersion`.

Geräte müssen mindestens folgende Informationen abbilden können:

- Gerätename
- Button-Beschriftung / Aktionstitel
- HTTP-Methode
- API-Pfad oder Ziel-URL gemäß bestehendem Gerätemodell
- zugewiesene WLAN-Profile
- Reihenfolge der zugewiesenen WLAN-Profile

WLAN-Profile müssen mindestens folgende Informationen abbilden können:

- Profilname
- SSID
- Sicherheits-/Konfigurationsdaten gemäß bestehendem WLAN-Profilmodell
- optional Passwort, nur wenn der Export mit Passwörtern ausdrücklich gewählt wurde

## Nicht Bestandteil

- verschlüsseltes Backup-Format
- Cloud-Synchronisation
- QR-Code-Import
- Änderung der Geräteaktion- oder WLAN-Verbindungslogik
- Änderung der Dashboard-Sortierung
- Änderung der Geräte-Reihenfolge im Dashboard

## UI- und Sicherheitsregeln

Bei Warn- und Sicherheitsdialogen steht die sichere Abbruchaktion rechts.

Beispiele:

- Export mit Passwörtern: links `Passwörter exportieren`, rechts `Abbrechen`
- Import einer Datei mit Passwörtern: links `Importieren`, rechts `Abbrechen`

## Akzeptanzkriterien

- [x] Export ohne WLAN-Passwörter ist möglich und Standard
- [x] Optionaler unverschlüsselter Export mit WLAN-Passwörtern ist möglich
- [x] Vor Export mit WLAN-Passwörtern wird deutlich gewarnt
- [x] Import aus Datei ist möglich
- [x] Import aus URL ist möglich
- [x] Import akzeptiert Dateien mit und ohne WLAN-Passwörter
- [x] Vor Import einer Datei mit WLAN-Passwörtern wird deutlich gewarnt
- [x] Importierte WLAN-Passwörter werden sicher gespeichert
- [x] Geräte-Button-Beschriftung wird exportiert und importiert
- [x] Geräteaktion wird exportiert und importiert
- [x] Zugewiesene WLAN-Profile pro Gerät werden exportiert und importiert
- [x] Reihenfolge der WLAN-Profile pro Gerät bleibt erhalten
- [x] Exportformat ist versioniert
- [x] Ungültige oder inkompatible Importdateien werden verständlich abgelehnt
- [x] Sichere Abbruchaktion steht in Warn- und Sicherheitsdialogen rechts

## Testhinweise

- Export ohne Passwörter erstellen und prüfen, dass keine Passwortfelder enthalten sind
- Export mit Passwörtern erstellen und prüfen, dass vorher gewarnt wird
- Import ohne Passwörter durchführen
- Import mit Passwörtern durchführen und prüfen, dass vorher gewarnt wird
- App nach Import neu starten und importierte Konfiguration prüfen
- Gerät mit mehreren zugewiesenen WLAN-Profilen importieren und Reihenfolge prüfen
- ungültige JSON-Datei importieren
- Datei mit nicht unterstützter `schemaVersion` importieren
- Import aus URL mit nicht erreichbarer URL testen
