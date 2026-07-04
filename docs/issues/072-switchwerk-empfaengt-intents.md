# Issue 072: SwitchWerk empfängt Intents

## Metadaten

- Status: Abgeschlossen
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

- [x] SwitchWerk kann einen expliziten Intent zum Starten einer bestehenden Geräteaktion empfangen.
- [x] Aktionen werden über stabile lokale IDs referenziert.
- [x] Ungültige, fehlende oder unbekannte Parameter werden sicher abgelehnt.
- [x] Es werden keine beliebigen URLs oder Befehle aus externen Intents ausgeführt.
- [x] Die bestehende Geräteaktionslogik wird wiederverwendet.
- [x] Fortschritt, Erfolg und Fehler werden verständlich angezeigt.
- [x] Sicherheitseinstellungen oder Freigaben sind geprüft und umgesetzt, falls erforderlich.
- [x] Intent-Nutzung ist dokumentiert.
- [x] Deutsche und englische Texte sind konsistent gepflegt, sofern UI-Texte ergänzt werden.

## Umsetzung

- GitHub-Issue: #168
- Pull Request: #169
- Externe Intents sind global aktivierbar und standardmäßig deaktiviert.
- Die Action `de.piecha.switchwerk.action.RUN_DEVICE_ACTION` akzeptiert nur das
  String-Extra `de.piecha.switchwerk.extra.DEVICE_ID`.
- Die Activity validiert den Intent über einen kleinen Parser und delegiert die
  lokale Geräte-ID an das MainViewModel. Netzwerk- und Geräteaktionslogik bleibt
  im bestehenden DeviceActionService.
- Hilfe-, Info- und Fehlertexte sind auf Deutsch und Englisch gepflegt; die
  README dokumentiert den ADB-Aufruf und das Vordergrundverhalten.

## Prüfergebnis

- `./gradlew lintDebug`: erfolgreich
- `./gradlew testDebugUnitTest`: erfolgreich
- GitHub Android Quality Checks: erfolgreich
- GitHub Dependency Submission: erfolgreich
- Manueller ADB-Test mit einer konfigurierten Geräte-ID: erfolgreich
- Nachkorrektur: Bekannten Geräten zuordenbare Intent-Fehler erscheinen wie
  andere Aktionsfehler vier Sekunden am Geräte-Widget. Nicht zuordenbare Fehler
  erscheinen vier Sekunden oberhalb der Geräteliste. Alle Intent-Fehler bleiben
  bis zum manuellen Löschen in den Aktionsdetails erhalten.
- Nachkorrektur-Pull-Request: #170
- Die App-Hilfe enthält deutsch/englisch konsistente, auswählbare ADB-Beispiele
  für einen gültigen Aufruf und einen Fehlertest ohne Geräte-ID.
- Die Nachkorrektur und die kopierbaren Hilfebeispiele wurden vom Benutzer auf
  dem Gerät erfolgreich getestet.

## Testhinweise

- Expliziten Intent für eine gültige Geräteaktion auslösen.
- Intent mit unbekannter Geräteaktions-ID auslösen.
- Intent ohne Pflichtparameter auslösen.
- Intent mit zusätzlichen unerwarteten Parametern auslösen.
- Verhalten bei nicht erreichbarem Zielgerät prüfen.
- Prüfen, dass keine sensiblen Daten im Log erscheinen.
