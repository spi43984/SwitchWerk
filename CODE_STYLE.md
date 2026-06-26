# CODE_STYLE.md

## Kotlin

- Verständliche Namen verwenden.
- Kleine Funktionen schreiben.
- `val` bevorzugen.
- Nullable-Werte bewusst behandeln.
- Keine unnötigen Abstraktionen.

## Compose

- Kleine Composables.
- Business-Logik ins ViewModel.
- UI-State als Data Class.
- Events klar benennen.
- Sicherheitskritische One-Shot-Schalter nicht dauerhaft aktiv halten: Nach
  Abschluss, Abbruch oder Fehler der Aktion immer auf den sicheren Default
  zurücksetzen.

Beispiel:

```kotlin
data class DeviceUiState(
    val devices: List<Device> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
```

## Fehlerbehandlung

Nicht einfach Exceptions verschlucken.

Besser:

```kotlin
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val message: String, val cause: Throwable? = null) : AppResult<Nothing>
}
```

## Kommentare

Kommentare erklären das Warum, nicht das Offensichtliche.
