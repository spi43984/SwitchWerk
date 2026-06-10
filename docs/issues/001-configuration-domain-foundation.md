# Issue #1: Configuration Domain Foundation

## Ziel

Die fachlichen Grundmodelle der App werden definiert.

SwitchWerk benötigt klare Domain-Modelle für WLAN-Profile, Geräte, Geräteverbindungen und API-Aufrufe.

## Hintergrund

Die App soll später Geräte wie Shelly oder ähnliche API-basierte Geräte schalten. Dafür braucht die App eine saubere fachliche Grundlage, bevor UI, Speicherung, WLAN-Verbindung und HTTP-Aufrufe implementiert werden.

## Anforderungen

- Modell für WLAN-Profil
- Modell für Gerät
- Modell für Geräteverbindung zu einem WLAN
- Modell für API-Aufruf
- Enum oder Modell für HTTP-Methode
- Repository-Schnittstellen für Geräte und WLAN-Profile
- Fake-Implementierungen für Entwicklung und UI-Tests

## Nicht Bestandteil

- echte Speicherung
- echte WLAN-Verbindung
- echte HTTP-Aufrufe
- Passwortverschlüsselung
- Import/Export

## Akzeptanzkriterien

- [ ] Domain-Modelle vorhanden
- [ ] Repository-Schnittstellen vorhanden
- [ ] Fake-Repositories vorhanden
- [ ] Build erfolgreich
- [ ] installDebug erfolgreich
