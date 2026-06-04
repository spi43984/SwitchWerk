# Issue #2: Configuration Domain Foundation

## Ziel

SwitchWerk soll später WLAN-Profile und steuerbare Geräte konfigurieren können.

Dieses Issue erstellt nur die Domain-Modelle und Repository-Schnittstellen. Es werden noch keine echten WLAN-Verbindungen, HTTP-Aufrufe, Speicherung oder UI-Dialoge implementiert.

## Hintergrund

Die alte ShellyPulse-App zeigt funktional, dass folgende Funktionen benötigt werden:

- WLAN-Verbindung per Android Network API
- primäres WLAN pro Gerät
- Fallback auf weitere WLANs
- API-Aufruf über das verbundene Netzwerk
- verschlüsselte Speicherung von WLAN-Passwörtern
- Export der Konfiguration ohne WLAN-Passwörter
- Import aus Datei oder URL

Der alte Code wird nicht übernommen.

## Datenmodelle

### WifiProfile

Ein WLAN-Profil enthält:

- id
- ssid
- passwordReference oder encryptedPassword

Die SSID darf im Klartext gespeichert werden.

WLAN-Passwörter gehören nicht direkt in das exportierbare WLAN-Profil. Sie werden später separat und verschlüsselt gespeichert.

### WifiCredentialStore

Die spätere Passwortspeicherung soll über eine eigene Schnittstelle erfolgen.

Beispiel:

```kotlin
interface WifiCredentialStore {
    suspend fun savePassword(wifiProfileId: String, password: String)
    suspend fun getPassword(wifiProfileId: String): String?
    suspend fun deletePassword(wifiProfileId: String)
}
```

In diesem Issue wird die Schnittstelle noch nicht zwingend implementiert.

### Device

Ein Gerät enthält:

- id
- name
- actionLabel
- apiCall
- connections
- sortOrder

Ein Gerät hat genau einen API-Aufruf.

### DeviceConnection

Ein Gerät kann über eine oder mehrere Verbindungen erreichbar sein.

Eine DeviceConnection enthält:

- wifiProfileId
- host

Beispiele für host:

- 192.168.33.1
- shellyplus1.local
- garage-device.local

Die Reihenfolge der `connections` ist wichtig:

1. erste Verbindung zuerst versuchen
2. bei Fehler die nächste Verbindung versuchen
3. wenn keine Verbindung funktioniert, Fehler zurückgeben

### ApiCall

Ein ApiCall enthält:

- method
- path

Zunächst genügt GET.

Beispiel:

/rpc/Switch.Set?id=0&on=true&toggle_after=1

### ApiMethod

Zunächst:

- GET
- POST

## Repository-Schnittstellen

Es sollen Interfaces angelegt werden für:

- WifiProfileRepository
- DeviceRepository

Noch keine echte Speicherung.

Fake-Implementierungen sind erlaubt.

## Nicht Bestandteil dieses Issues

- echte WLAN-Verbindung
- echter HTTP-Request
- Room
- DataStore
- QR-Code-Scanner
- Import aus URL
- Export-Datei
- Einstellungsdialoge
- Dashboard-Reihenfolge per Drag & Drop

## Architekturvorgaben

Umsetzung gemäß:

- AGENTS.md
- ARCHITECTURE.md
- SECURITY.md
- CODE_STYLE.md
- TESTING.md
- GITHUB_WORKFLOW.md

## Sicherheitsvorgaben

- Keine WLAN-Passwörter loggen
- Keine Passwörter im Export
- Keine echten Passwörter im Code
- Keine Cloud-Kommunikation
- Keine Tracker
- Keine unnötigen Berechtigungen in diesem Issue hinzufügen

## Akzeptanzkriterien

- [ ] WifiProfile Modell vorhanden
- [ ] Device Modell vorhanden
- [ ] DeviceConnection Modell vorhanden
- [ ] ApiCall Modell vorhanden
- [ ] ApiMethod Enum vorhanden
- [ ] WifiProfileRepository Interface vorhanden
- [ ] DeviceRepository Interface vorhanden
- [ ] Fake-Implementierungen vorhanden
- [ ] MainViewModel lädt Geräte aus DeviceRepository
- [ ] StartScreen zeigt Anzahl der Geräte
- [ ] Build erfolgreich
- [ ] installDebug erfolgreich

## Definition of Done

- Code baut erfolgreich
- App startet auf physischem Gerät
- Keine echten Passwörter im Code
- Keine ShellyPulse-Dateien übernommen
- Commit verweist auf dieses Issue
