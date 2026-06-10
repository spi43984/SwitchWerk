# AI Context – SwitchWerk

## Projekt

SwitchWerk ist eine Android-App für Teamwerk.

Die App verbindet sich kurzzeitig mit dem WLAN eines Zielgeräts (z. B. Shelly 1 Mini Gen3), sendet einen HTTP- oder RPC-Befehl und kehrt anschließend in das primäre WLAN zurück.

Ziel ist eine einfache und robuste Bedienung ohne Cloud-Abhängigkeit.

---

## Technologie

* Kotlin
* Jetpack Compose
* MVVM
* Coroutines
* Room
* Android Studio
* GitHub Actions

---

## Architekturprinzipien

* Clean und wartbarer Code
* MVVM strikt einhalten
* UI und Business-Logik trennen
* Keine unnötigen Abhängigkeiten
* Sicherheit vor Komfort
* Lokale Datenspeicherung bevorzugen
* Cloud-Anbindung nur wenn ausdrücklich erforderlich

---

## Aktueller Stand

### Bereits umgesetzt

* WLAN-Wechsel zu Geräte-AP
* HTTP/RPC-Aufrufe an Shelly
* Rückwechsel ins primäre WLAN
* Sichere Speicherung sensibler Daten mittels EncryptedSharedPreferences
* Grundlegende Geräteverwaltung

### In Arbeit

* Verbesserung der Geräteverwaltung
* Release-Prozess über GitHub Actions
* APK-Erstellung bei Git-Tags

---

## Zielgeräte

Primär:

* Shelly 1 Mini Gen3

Später möglich:

* Weitere Shelly-Geräte
* Geräte mit HTTP-API
* Geräte mit REST-API

---

## Wichtige Randbedingungen

* Android 16 als Referenzplattform
* Entwicklung auf Ubuntu Linux
* Quellcode liegt auf GitHub
* App soll auch von technisch weniger versierten Vereinsmitgliedern nutzbar sein

---

## Vorgehensweise für KI-Assistenten

Bei Vorschlägen bevorzugen:

1. Kotlin Best Practices
2. Jetpack Compose
3. MVVM
4. Coroutines
5. Room
6. GitHub Actions

Bei Architekturfragen stets bestehende Architektur weiterentwickeln und keine kompletten Neuentwürfe vorschlagen.

Bei Änderungen immer Auswirkungen auf Sicherheit, Wartbarkeit und Android-Kompatibilität berücksichtigen.

