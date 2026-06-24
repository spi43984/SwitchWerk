# Issue 049 iOS: App Foundation SwiftUI

## Metadaten

- Status: Backlog
- Priorität: P4
- Plattform: iOS
- iOS-Phase: 4 von 6
- Typ: App-Foundation / Planung

## Ziel

Das spätere Grundgerüst einer nativen iOS-App mit SwiftUI planen. Das Implementierungsziel ist eine lauffähige App ohne produktive Geräteaktion.

## Geplanter Umfang

- Projektstruktur unter `ios/`, getrennt von der Android-App.
- SwiftUI-Navigation für Dashboard, Geräteverwaltung, WLAN-Verwaltung und Einstellungen.
- MVVM mit klar getrennten Views, ViewModels, Repositories und Plattformdiensten.
- Lokale Persistenz für nicht sensible Konfiguration und Keychain für Zugangsdaten.
- Anbindung an das spätere Shared-Config-&-Translation-Modell.
- Leere oder simulierte Ansichten, die Navigation und lokale Konfiguration demonstrieren, aber keine Geräte schalten.

## Architekturelle Leitplanken

- Native SwiftUI- und Apple-Standardmechanismen bevorzugen.
- Netzwerk-, WLAN- und Keychain-Details gehören nicht in Views.
- Kein Cloud-Backend, kein Account-System, keine Analytics und kein Tracking.
- Konkrete Paket-, Persistenz- und Dependency-Entscheidungen bleiben bis nach der Machbarkeitsprüfung aus Issue 047 offen.

## Abhängigkeiten

- Issue 047 muss die iOS-Machbarkeit mindestens mit Einschränkungen bestätigen.
- Issue 048 liefert gegebenenfalls die gemeinsamen Konfigurations- und Übersetzungsquellen.

## Nicht Bestandteil

- HTTP/RPC-Implementierung.
- Geräte-AP-Verbindung oder WLAN-Wechsel.
- Migration der Android-App oder gemeinsamer Runtime-Code.

## Akzeptanzkriterien

- [ ] Eine Zielstruktur unter `ios/` und die Verantwortlichkeiten der Schichten sind dokumentiert.
- [ ] Dashboard, Geräteverwaltung, WLAN-Verwaltung und Einstellungen sind als SwiftUI-Navigationsziele geplant.
- [ ] Persistenz und Keychain sind nach Datenklassifikation abgegrenzt.
- [ ] Das spätere Implementierungsziel ist eine startbare, nicht schaltende iOS-App.
- [ ] Offene Entscheidungen aus der Machbarkeitsprüfung sind nicht vorweg festgeschrieben.
