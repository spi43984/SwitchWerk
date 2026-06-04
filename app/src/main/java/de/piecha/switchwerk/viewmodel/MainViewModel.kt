package de.piecha.switchwerk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.piecha.switchwerk.data.repository.DeviceRepository
import de.piecha.switchwerk.domain.model.Device
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MainUiState(
    val devices: List<Device> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class MainViewModel(
    private val repository: DeviceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        loadDevices()
    }

    private fun loadDevices() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            runCatching {
                repository.getDevices()
            }.onSuccess { devices ->
                _uiState.value = MainUiState(devices = devices)
            }.onFailure { error ->
                _uiState.value = MainUiState(
                    errorMessage = error.message ?: "Geräte konnten nicht geladen werden"
                )
            }
        }
    }
}
