# Issue #40: Import/Export Password Handling

## Metadaten

* Status: offen
* Priorität: P1
* Typ: Backup / Sicherheit

## Ziel

Der Export und Import von Konfigurationen soll den Umgang mit gespeicherten Passwörtern transparent, sicher und eindeutig gestalten.

Beim Export soll auswählbar sein, ob Passwörter exportiert werden.

Beim Import soll eine Konfiguration mit enthaltenen Passwörtern erkannt werden. Der Anwender kann dann entscheiden, ob die Passwörter übernommen werden, ignoriert werden oder der Import abgebrochen wird.

## Hintergrund

Konfigurationen können sensible Zugangsdaten enthalten, insbesondere WLAN-Passwörter und Geräte-Zugangsdaten.

Für Backup und Wiederherstellung ist ein Export mit Passwörtern sinnvoll.

Für Supportfälle, Beispielkonfigurationen oder den Austausch zwischen Anwendern ist ein Import ohne Passwörter häufig die sicherere Variante.

## Scope

### Export

Im Exportbereich wird eine zusätzliche Option eingeführt:

* Passwörter einschließen

Standard:

* deaktiviert

Wenn deaktiviert:

* WLAN-Passwörter werden nicht exportiert
* Geräte-Passwörter werden nicht exportiert
* sonstige Zugangsdaten werden nicht exportiert

Alle übrigen Konfigurationsdaten werden exportiert.

Wenn aktiviert:

* Passwortdaten werden exportiert
* Die Exportdatei kennzeichnet, dass Passwortdaten enthalten sind.

### Import

Beim Einlesen einer Konfiguration wird erkannt, ob Passwortdaten enthalten sind.

#### Konfiguration ohne Passwörter

Der Import erfolgt ohne zusätzliche Passwortwarnung.

#### Konfiguration mit Passwörtern

Es erscheint ein Sicherheitsdialog.

Titel:

Konfiguration enthält Passwörter

Text:

Diese Konfiguration enthält gespeicherte Zugangsdaten. Möchten Sie diese ebenfalls importieren?

Buttons:

* Mit Passwörtern importieren
* Ohne Passwörter importieren
* Abbrechen

Die Auswahl gilt für den gesamten Importvorgang.

## Importregeln

### Ohne Passwörter importieren

Passwortdaten gelten als nicht Bestandteil des Imports.

#### Neue Datensätze

Werden neue WLANs oder Geräte angelegt:

* alle Passwortfelder bleiben leer

#### Bestehende Datensätze bei „Ergänzen/Überschreiben“

Werden vorhandene WLANs oder Geräte aktualisiert:

* importierte Passwortfelder werden ignoriert
* vorhandene Passwortfelder bleiben unverändert
* alle übrigen Felder werden gemäß bestehender Importlogik aktualisiert

Es dürfen keine vorhandenen Passwörter gelöscht oder überschrieben werden, wenn der Anwender „Ohne Passwörter importieren“ gewählt hat.

#### Alles ersetzen

Bei „Alles ersetzen“ und „Ohne Passwörter importieren“ werden importierte Datensätze ohne Passwortdaten gespeichert.

### Mit Passwörtern importieren

Passwortdaten werden wie alle anderen Konfigurationsdaten importiert.

#### Neue Datensätze

* importierte Passwörter werden übernommen

#### Bestehende Datensätze bei „Ergänzen/Überschreiben“

* importierte Passwörter überschreiben vorhandene Passwörter

#### Alles ersetzen

* importierte Passwörter ersetzen vorhandene Passwörter

## Architektur

### Exportmodell

Das Exportmodell soll kennzeichnen, ob Passwortdaten enthalten sind.

Beispiel:

```json
{
  "containsPasswords": true
}
```

Passwortfelder werden nur serialisiert, wenn der Export mit Passwörtern gewählt wurde.

### Importservice

Der Importservice erhält eine explizite Option:

* includePasswords = true
* includePasswords = false

Bei includePasswords = false:

* Passwortfelder werden ignoriert
* vorhandene Passwortwerte bleiben bei „Ergänzen/Überschreiben“ erhalten

## UI

### Export

* Option „Passwörter einschließen“
* Standard: deaktiviert

### Import

Sichere Abbruchaktion rechts gemäß Projektregel.

Links:

* Mit Passwörtern importieren
* Ohne Passwörter importieren

Rechts:

* Abbrechen

## Nicht Bestandteil

* Änderung der Passwortverschlüsselung
* Cloud-Synchronisation
* Änderung der WLAN-Verbindungslogik
* Änderung der Geräteaktionslogik
* passwortweiser Einzelimport pro WLAN oder Gerät

## Sicherheitsanforderungen

* Passwörter dürfen nicht geloggt werden
* Passwörter dürfen nicht in Diagnosemeldungen erscheinen
* Export mit Passwörtern darf nicht Standard sein
* Import ohne Passwörter darf bestehende Passwörter bei „Ergänzen/Überschreiben“ nicht löschen

## Akzeptanzkriterien

* [ ] Export ohne Passwörter enthält keine Zugangsdaten
* [ ] Export mit Passwörtern enthält Zugangsdaten
* [ ] Passwortdaten werden zuverlässig erkannt
* [ ] Sicherheitsdialog erscheint bei passworthaltigen Dateien
* [ ] Import mit Passwörtern übernimmt Passwortdaten
* [ ] Import ohne Passwörter übernimmt keine Passwortdaten
* [ ] Vorhandene Passwörter bleiben bei „Ergänzen/Überschreiben“ erhalten
* [ ] Neue Datensätze bleiben ohne Passwort, wenn ohne Passwörter importiert wird
* [ ] Abbrechen beendet den Import ohne Änderungen
* [ ] Bestehende Exportdateien bleiben kompatibel

## Testhinweise

### Export

* Export ohne Passwörter
* Export mit Passwörtern

### Import

* Datei ohne Passwörter importieren
* Datei mit Passwörtern und Übernahme importieren
* Datei mit Passwörtern ohne Übernahme importieren
* Import abbrechen

### Ergänzen/Überschreiben

Vorhandenes WLAN mit Passwort:

* Mit Passwörtern importieren → Passwort wird aktualisiert
* Ohne Passwörter importieren → Passwort bleibt unverändert

### Neue Datensätze

* Mit Passwörtern → Passwort wird übernommen
* Ohne Passwörter → Passwort bleibt leer

### Alles ersetzen

* Mit Passwörtern → Passwortdaten werden übernommen
* Ohne Passwörter → keine Passwortdaten werden gespeichert
