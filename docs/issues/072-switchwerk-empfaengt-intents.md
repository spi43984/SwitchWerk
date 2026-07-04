# Issue 072: SwitchWerk empfängt Intents

## Metadaten

- Status: Offen
- Priorität: P1
- Typ: Feature / Integration
- Bereich: Android Intents / Automatisierung / Geräteaktionen

## Ziel

SwitchWerk soll explizite Android Intents empfangen können, damit andere Apps gezielt eine konfigurierte SwitchWerk-Aktion starten können.

## Hintergrund

SwitchWerk eignet sich für lokale, kurze Geräteaktionen. Andere Android-Apps wie Automatisierungs-Apps, NFC-Auslöser oder QR-Code-Scanner sollen SwitchWerk gezielt aufrufen können, ohne eigene WLAN-, HTTP- oder RPC-Logik nachbauen zu müssen.

## Scope

### Eingehende Intents

- Einen klar dokumentierten expliziten Intent-Einstieg für Geräteaktionen definieren.
- Geräteaktionen über stabile interne IDs adressieren.
- Optional verständliche Fehler anzeigen, wenn eine referenzierte Aktion nicht existiert.
- Intent-Ausführung über bestehende SwitchWerk-Aktionslogik durchführen.

### Sicherheit

- Keine Passwörter, Tokens, vollständigen URLs oder sensiblen Konfigurationsdaten per Intent akzeptieren.
- Keine beliebigen HTTP-URLs oder RPC-Befehle direkt aus fremden Intents ausführen.
- Nur bereits lokal konfigurierte Geräteaktionen ausführen.
- Fehlende oder ungültige Parameter sicher ablehnen.

### Nutzerkontrolle

- Prüfen, ob Intent-Ausführung global aktivierbar/deaktivierbar sein soll.
- Prüfen, ob pro Gerät oder Aktion eine Freigabe für externe Auslösung sinnvoll ist.
- Kritische Aktionen dürfen nicht versehentlich durch fremde Apps ausgelöst werden.

### Dokumentation

- Intent-Action, erforderliche Extras und Fehlerverhalten dokumentieren.
- Neutrale Beispielwerte verwenden.

## Nicht im Scope

- Keine App Shortcuts.
- Keine Quick Settings Tiles.
- Keine Widgets.
- Keine Broadcast-Rückmeldungen.
- Keine Ausführung beliebiger fremder URLs.
- Keine Cloud- oder Account-Funktion.

## Architekturhinweise

- Intent-Handling schlank halten und an bestehende Aktionslogik delegieren.
- Keine Netzwerklogik in Activity, Receiver oder Intent-Parser verschieben.
- Eingaben strikt validieren.
- Für Beispiele ausschließlich neutrale Platzhalter verwenden.
- Keine sensiblen Daten loggen.

## Akzeptanzkriterien

- [ ] SwitchWerk kann einen expliziten Intent zum Starten einer bestehenden Geräteaktion empfangen.
- [ ] Aktionen werden über stabile lokale IDs referenziert.
- [ ] Ungültige, fehlende oder unbekannte Parameter werden sicher abgelehnt.
- [ ] Es werden keine beliebigen URLs oder Befehle aus externen Intents ausgeführt.
- [ ] Die bestehende Geräteaktionslogik wird wiederverwendet.
- [ ] Fortschritt, Erfolg und Fehler werden verständlich angezeigt.
- [ ] Sicherheitseinstellungen oder Freigaben sind geprüft und umgesetzt, falls erforderlich.
- [ ] Intent-Nutzung ist dokumentiert.
- [ ] Deutsche und englische Texte sind konsistent gepflegt, sofern UI-Texte ergänzt werden.

## Testhinweise

- Expliziten Intent für eine gültige Geräteaktion auslösen.
- Intent mit unbekannter Geräteaktions-ID auslösen.
- Intent ohne Pflichtparameter auslösen.
- Intent mit zusätzlichen unerwarteten Parametern auslösen.
- Verhalten bei nicht erreichbarem Zielgerät prüfen.
- Prüfen, dass keine sensiblen Daten im Log erscheinen.
