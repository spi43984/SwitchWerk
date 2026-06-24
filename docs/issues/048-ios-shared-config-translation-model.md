# Issue 048 iOS: Shared Config & Translation Model

## Metadaten

- Status: Backlog
- Priorität: P4
- Plattform: Shared / iOS / Android
- iOS-Phase: 3 von 6
- Typ: Datenmodell / Planung

## Ziel

Plattformneutrale Quellen für Konfigurationsdaten, sichtbare Texte und Befehlsvorlagen definieren, damit Android und eine spätere iOS-App dieselben fachlichen Inhalte verwenden können.

## Scope

- Geräte-, WLAN- und Befehlsmodell fachlich beschreiben.
- Ein versioniertes JSON- oder YAML-Schema für austauschbare, plattformneutrale Konfigurationen bewerten und festlegen.
- Übersetzungsschlüssel und Sprachdateien für sichtbare App-Texte definieren.
- Kommando-Templates für HTTP- und RPC-Geräte beschreiben, ohne Geheimnisse in Vorlagen oder Beispielen zu speichern.
- Beispielkonfigurationen erstellen, die keine realen SSIDs, Passwörter, Token, Hosts oder lokalen IP-Adressen enthalten.
- Regeln für Schema-Versionierung, Validierung und Rückwärtskompatibilität festhalten.

## Abgrenzung zu bestehender Android-Arbeit

Die Android-Konfiguration, Android-String-Ressourcen und der bestehende Import/Export werden nicht migriert oder umgebaut. Dieses Issue definiert nur gemeinsame Quellen und einen möglichen schrittweisen Integrationspfad. Eine spätere Übernahme in Android oder iOS benötigt jeweils ein eigenes Implementierungs-Issue.

## Abhängigkeiten

- Die Geräte- und WLANfelder müssen mit den abgeschlossenen Android-Issues 001, 007, 008, 010 und 012 fachlich abgeglichen werden.
- Passwortbehandlung und Exportregeln aus Issue 040 dürfen nicht geschwächt werden.
- Die iOS-Netzwerkgrenzen aus Issue 047 beeinflussen nur iOS-spezifische Felder, nicht das gemeinsame Kernschema.

## Sicherheits- und Datenschutzhinweise

- Gemeinsame Beispiele enthalten nie Zugangsdaten oder reale lokale Netzwerkinformationen.
- Geheimnisse bleiben plattformspezifisch in sicherer Speicherung; sie sind kein verpflichtender Bestandteil der gemeinsamen Quellen.
- Keine Übersetzungs-, Cloud- oder Analyseabhängigkeit einführen.

## Akzeptanzkriterien

- [ ] Ein versioniertes, plattformneutrales Modell für Geräte, WLANs und Befehle ist dokumentiert.
- [ ] Format, Validierungsregeln und mindestens eine bereinigte Beispielkonfiguration sind definiert.
- [ ] Übersetzungsschlüssel und Textquellen sind plattformneutral beschrieben.
- [ ] Kommando-Templates sind ohne sensible Daten dokumentiert.
- [ ] Der Migrationsausschluss für die bestehende Android-App ist klar festgehalten.
