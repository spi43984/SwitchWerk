# Issue 029 – Language Setting

## Ziel

SwitchWerk soll mehrsprachig werden. Initial werden Deutsch, Englisch und Systemsprache unterstützt.

## Optionen

- System
- Deutsch
- Englisch

## Umsetzungsidee

Android-Standardressourcen verwenden:

- res/values/strings.xml als Default
- res/values-de/strings.xml für Deutsch
- res/values-en/strings.xml für Englisch

UI-Texte sollen schrittweise aus fest codierten Strings in String-Ressourcen verschoben werden.

Für die App-Sprachauswahl wird eine gespeicherte Einstellung verwendet. Bei "System" wird die Android-Systemsprache genutzt.

## Anforderungen

- Sprachauswahl in den Einstellungen
- Standardwert: System
- Deutsch und Englisch initial unterstützen
- Keine Übersetzung von Benutzerdaten wie Gerätenamen, WLAN-Namen oder URLs
- Keine externe Übersetzungsbibliothek erforderlich

## Akzeptanzkriterien

- [ ] Einstellung "Sprache" ist sichtbar
- [ ] System/Deutsch/Englisch auswählbar
- [ ] Auswahl bleibt nach App-Neustart erhalten
- [ ] UI-Texte werden über String-Ressourcen geladen
- [ ] Gerätenamen und Nutzereingaben bleiben unverändert
