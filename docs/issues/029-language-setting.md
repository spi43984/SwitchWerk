# Issue #29: Language Setting

## Metadaten

- Status: Abgeschlossen
- Priorität: P1
- Typ: GUI / Einstellungen

## Ziel

SwitchWerk soll mehrsprachig werden. Initial werden Deutsch, Englisch und Systemsprache unterstützt.

## Optionen

- System
- Deutsch
- Englisch

## Umsetzungsidee

Android-Standardressourcen verwenden:

- `res/values/strings.xml` als Default
- `res/values-de/strings.xml` für Deutsch
- `res/values-en/strings.xml` für Englisch

UI-Texte sollen aus fest codierten Strings in String-Ressourcen verschoben werden.

Für die App-Sprachauswahl wird eine gespeicherte Einstellung verwendet. Bei "System" wird die Android-Systemsprache genutzt.

## Architekturhinweis

Sprache und Theme sollen möglichst über dieselbe Einstellungs-Infrastruktur gespeichert werden, damit Issue 028 und Issue 029 technisch konsistent umgesetzt werden können.

Vorgeschlagene Struktur:

- `domain/SettingsRepository`
- `data/AppSettingsStorage`
- `ui/SettingsScreen`

Die UI liest die aktuelle Spracheinstellung aus dem Settings-State. Änderungen werden über das ViewModel an das Repository weitergegeben und dauerhaft gespeichert.

Benutzerdaten wie Gerätenamen, WLAN-Namen und technische Eingaben werden nicht übersetzt.

## Anforderungen

- Sprachauswahl in den Einstellungen
- Standardwert: System
- Deutsch und Englisch initial unterstützen (für Englisch Übersetzung erstellen und zur Prüfung vorlegen)
- Keine Übersetzung von Benutzerdaten wie Gerätenamen, WLAN-Namen oder technischen Eingaben
- Keine externe Übersetzungsbibliothek erforderlich
- Gemeinsame Einstellungs-Infrastruktur mit dem Theme-Mode-Issue bevorzugen

## Akzeptanzkriterien

- [x] Einstellung "Sprache" ist sichtbar
- [x] System/Deutsch/Englisch auswählbar
- [x] Auswahl bleibt nach App-Neustart erhalten
- [x] UI-Texte werden über String-Ressourcen geladen
- [x] Gerätenamen und Nutzereingaben bleiben unverändert
- [x] Architektur passt zur Theme-Mode-Einstellung aus Issue 028

## Implementierungsstand

- Sprache ist Teil des bestehenden `AppSettings`-State und wird zusammen mit
  den übrigen Einstellungen im vorhandenen SharedPreferences-Repository gespeichert.
- Android 13 und neuer verwendet `LocaleManager`; Android 8 bis 12 verwendet
  eine lokale Ressourcen-Konfiguration ohne zusätzliche AppCompat-Abhängigkeit.
- Default-, deutsche und englische Ressourcen enthalten denselben vollständigen
  Schlüsselsatz für sichtbare UI-, Status-, Validierungs- und Diagnosetexte.
- Benutzerdaten und technische Eingaben werden ausschließlich als unveränderte
  Formatargumente eingesetzt.

Bestätigte Prüfungen:

```text
git diff --check
./gradlew testDebugUnitTest
./gradlew clean assembleDebug
./gradlew installDebug
manuelle Prüfung der Sprachvarianten und Neustartpersistenz
```

Issue 029 wurde nach dem Merge abgeschlossen.
