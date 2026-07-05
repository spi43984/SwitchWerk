# Issue 075: Schnellkacheln

## Metadaten

- Status: Backlog
- Priorität: P2
- Typ: Feature / Android Integration
- Bereich: Quick Settings Tile / Geräteaktionen

## Ziel

SwitchWerk soll mindestens eine Android-Schnellkachel bereitstellen, damit eine häufige Geräteaktion direkt aus den Android-Schnelleinstellungen gestartet werden kann.

## Hintergrund

Für häufige Aktionen wie Licht, Tor oder Steckdose ist der Weg über App öffnen und Gerät auswählen manchmal zu lang. Eine Schnellkachel erlaubt den Start direkt aus dem heruntergezogenen Android-Schnelleinstellungsbereich.

## Scope

### Schnellkachel

- Eine Android Quick Settings Tile bereitstellen.
- Die Kachel startet eine vom Anwender ausgewählte SwitchWerk-Aktion.
- Falls keine Aktion ausgewählt ist, öffnet die Kachel SwitchWerk zur Einrichtung.
- Fortschritt, Erfolg und Fehler müssen verständlich erkennbar sein.

### Konfiguration

- In SwitchWerk eine Aktion für die Schnellkachel auswählen.
- Auswahl ändern oder entfernen können.
- Kachelbeschriftung sinnvoll aktualisieren, soweit Android dies unterstützt.

### Sicherheit

- Keine Passwörter, Tokens oder vollständigen technischen URLs in Tile-Status oder Logs schreiben.
- Keine beliebigen externen Befehle ausführen.
- Nur lokal konfigurierte Geräteaktionen starten.
- Kritische Aktionen und Fehlbedienung berücksichtigen.

## Nicht im Scope

- Keine App Shortcuts.
- Keine Homescreen-Widgets.
- Keine komplexe Mehrfachkachel-Verwaltung, sofern Android dies nicht einfach unterstützt.
- Keine Cloud-, Account- oder Tracking-Funktion.
- Keine Broadcast-Rückmeldungen.

## Architekturhinweise

- TileService nur als Einstieg verwenden.
- Geräteaktion an bestehende Aktionslogik delegieren.
- Keine HTTP-, RPC- oder WLAN-Logik im TileService duplizieren.
- Zustand und Konfiguration über bestehende lokale Speicherung integrieren.
- Android-Versionen und Berechtigungen prüfen.

## Akzeptanzkriterien

- [ ] SwitchWerk stellt eine Android-Schnellkachel bereit.
- [ ] Eine lokal konfigurierte Geräteaktion kann der Schnellkachel zugeordnet werden.
- [ ] Tippen auf die Kachel startet die zugeordnete Aktion.
- [ ] Ohne konfigurierte Aktion öffnet die Kachel eine sinnvolle Einrichtung in SwitchWerk.
- [ ] Erfolg und Fehler werden verständlich dargestellt.
- [ ] Es werden keine sensiblen Daten im Tile-Status oder Log ausgegeben.
- [ ] Die bestehende Geräteaktionslogik wird wiederverwendet.
- [ ] Deutsche und englische Texte sind konsistent gepflegt.
- [ ] Hilfe-, Info- und Tooltip-Texte wurden geprüft und bei Bedarf aktualisiert.

## Testhinweise

- Schnellkachel zu Android-Schnelleinstellungen hinzufügen.
- Aktion zuordnen.
- Kachel ausführen.
- Verhalten ohne zugeordnete Aktion prüfen.
- Verhalten bei nicht erreichbarem Zielgerät prüfen.
- Aktion ändern und erneut ausführen.
- Aktion entfernen und Kachel erneut ausführen.
