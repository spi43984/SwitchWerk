# ARCHITECTURE.md

## Zielarchitektur

Die App nutzt eine einfache MVVM-Struktur.

```text
UI / Compose
    ↓
ViewModel
    ↓
Repository
    ↓
API Client / Local Storage
```

## Paketstruktur

Empfohlen:

```text
app/src/main/java/.../
├── ui/
│   ├── screens/
│   ├── components/
│   └── theme/
├── viewmodel/
├── data/
│   ├── repository/
│   ├── remote/
│   └── local/
├── domain/
│   └── model/
└── di/
```

## UI

- Jetpack Compose
- Material Design 3
- Keine Netzwerkaufrufe direkt aus Composables
- Composables möglichst zustandslos halten
- Zustand kommt aus dem ViewModel

## ViewModel

ViewModels enthalten:

- UI-State
- Benutzeraktionen
- Aufruf des Repository

ViewModels enthalten nicht:

- HTTP-Details
- JSON-Verarbeitung
- Datenbankdetails

## Repository

Das Repository kapselt:

- Shelly API-Aufrufe
- Andere Geräte-APIs
- Lokale Speicherung, falls nötig

Beispiel:

```kotlin
interface DeviceRepository {
    suspend fun switchOn(deviceId: String): Result<Unit>
    suspend fun switchOff(deviceId: String): Result<Unit>
    suspend fun toggle(deviceId: String): Result<Unit>
}
```

## Dependency Injection

Koin verwenden.

Beispielstruktur:

```text
di/
├── AppModule.kt
├── NetworkModule.kt
└── RepositoryModule.kt
```

## Lokale Speicherung

Nur speichern, was nötig ist:

- Gerätename
- IP-Adresse / Hostname
- Gerätetyp
- API-Pfad
- optional Raum / Gruppe

Sensible Daten nur verschlüsselt speichern.
