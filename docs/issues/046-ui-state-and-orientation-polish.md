# Issue #46: UI State And Orientation Polish

## Metadaten

- Status: Offen
- Priorität: P0
- Typ: GUI / Navigation / Orientierung / Text

## Ziel

Mehrere kleine, aber störende UI-, Zustands- und Textprobleme sollen behoben werden, damit die App bei Sprachwechseln, Rotation, QR-Code-Import und About-Informationen vorhersehbar und verständlich bleibt.

## Hintergrund

Aktuell gibt es mehrere Situationen, in denen die App unerwartet zum Dashboard zurückkehrt oder die Orientierung für Benutzer irritierend wirkt. Zusätzlich soll der About-Text klarer erklären, dass SwitchWerk lokale Geräte direkt über deren Access Point steuert.

## Scope

### Dashboard Aktionsdetails

- Im Rahmen `Aktionsdetails` die Positionen von Sortierung `Neueste oben/unten` und Mülleimer tauschen
- Der Mülleimer soll rechts angezeigt werden
- Die Sortierauswahl soll links vom Mülleimer stehen

### Spracheinstellung

- Bei Umstellung der Sprache soll die App im Bereich `Einstellungen -> System` bleiben
- Kein Rücksprung zum Dashboard
- Der aktive Einstellungsbereich soll nach der Sprachumstellung erhalten bleiben

### QR-Code-Scanner Orientierung

- Wird der QR-Code-Scanner im Portrait-Modus gestartet, soll er auch im Portrait-Modus bleiben
- Wird er im Landscape-Modus gestartet, soll er im Landscape-Modus bleiben
- Kein erzwungener Landscape-Start, wenn das Gerät hochkant gehalten wird

### Rotation in Einstellungen

- Wenn der Benutzer in den Einstellungen ist und das Gerät dreht, soll die App in den Einstellungen bleiben
- Der aktive Bereich, z. B. `System`, soll nach Rotation erhalten bleiben
- Kein Rücksprung zum Dashboard durch Konfigurationswechsel
- Geöffnete Dialoge sollen nach Rotation erhalten bleiben
- Geöffnete Bearbeitungsdialoge dürfen nicht automatisch geschlossen werden

### About-Text

- Der About-Text soll verständlicher beschreiben, wofür SwitchWerk gedacht ist
- Sinngemäß ergänzen:
  - SwitchWerk steuert Geräte im lokalen Netzwerk, die sonst nicht über ein WLAN-Netzwerk oder Internet erreichbar sind
  - SwitchWerk verbindet sich dazu direkt mit dem Access Point des zu steuernden Gerätes
- Deutsche und englische String-Ressourcen konsistent anpassen
- Keine technischen Details wie Passwörter, Tokens oder konkrete lokale IPs im About-Text anzeigen

## Nicht im Scope

- Änderung der WLAN-Logik
- Änderung der Geräteaktionslogik
- Änderung der Import-/Export-Logik außer QR-Scanner-Startverhalten
- Neues Navigationsframework
- Neue externe Abhängigkeiten, sofern vermeidbar

## Architekturhinweise

- Änderungen möglichst in der Compose-UI- und Navigationsschicht halten
- Bestehende Settings- und AppSettings-Infrastruktur weiterverwenden
- UI-Zustand für aktuellen Hauptbereich und Einstellungsbereich muss rotations- und sprachwechselstabil sein
- UI-Zustände über rememberSaveable bzw. SavedStateHandle sichern
- About-Texte über Android-String-Ressourcen pflegen
- Keine Netzwerklogik in Composables ergänzen

## Akzeptanzkriterien

- [ ] Mülleimer in `Aktionsdetails` steht rechts
- [ ] Sortierung `Neueste oben/unten` steht links vom Mülleimer
- [ ] Sprachwechsel in `Einstellungen -> System` bleibt in `Einstellungen -> System`
- [ ] Sprachwechsel aus anderen Einstellungsbereichen verlässt die Einstellungen nicht
- [ ] QR-Code-Scanner startet im Portrait-Modus, wenn das Gerät hochkant gehalten wird
- [ ] QR-Code-Scanner startet im Landscape-Modus, wenn das Gerät quer gehalten wird
- [ ] Rotation in den Einstellungen führt nicht zurück zum Dashboard
- [ ] Aktiver Einstellungsbereich bleibt nach Rotation erhalten
- [ ] Geöffnete Dialoge bleiben nach Rotation erhalten
- [ ] About-Text erklärt lokalen Netzwerk-/Access-Point-Anwendungsfall verständlich
- [ ] About-Text ist auf Deutsch und Englisch konsistent gepflegt
- [ ] Dashboard bleibt weiterhin normal erreichbar
- [ ] Dark Mode und Light Mode funktionieren weiterhin
- [ ] Build erfolgreich

## Testhinweise

- App starten und Aktionsdetails aktivieren
- Prüfen, dass der Mülleimer rechts im Aktionsdetails-Rahmen steht
- Einstellungen öffnen, Bereich `System` wählen, Sprache ändern
- Prüfen, dass `Einstellungen -> System` aktiv bleibt
- Einstellungen öffnen, Bereich `WLAN-Profile`, `Geräte`, `System` und `Backup` wählen und jeweils Gerät drehen
- WLAN-Profil bearbeiten und während des Dialogs rotieren
- Gerät bearbeiten und während des Dialogs rotieren
- QR-Code-Scanner im Portrait-Modus starten
- QR-Code-Scanner im Landscape-Modus starten
- About/Hilfe in Deutsch prüfen
- About/Hilfe in Englisch prüfen
- Rücknavigation aus Scanner und Einstellungen testen
