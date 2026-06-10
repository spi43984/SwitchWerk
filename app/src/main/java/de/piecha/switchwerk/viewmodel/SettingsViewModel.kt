package de.piecha.switchwerk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.piecha.switchwerk.data.repository.WifiProfileRepository
import de.piecha.switchwerk.domain.model.WifiProfile
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class WifiProfileFormState(
    val id: String? = null,
    val ssid: String = "",
    val password: String = "",
    val hasSavedPassword: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isPasswordChanged: Boolean = false
)

data class SettingsUiState(
    val wifiProfiles: List<WifiProfile> = emptyList(),
    val form: WifiProfileFormState = WifiProfileFormState(),
    val isEditingWifiProfile: Boolean = false,
    val errorMessage: String? = null
)

class SettingsViewModel(
    private val wifiProfileRepository: WifiProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        observeWifiProfiles()
    }

    fun startNewWifiProfile() {
        _uiState.value = _uiState.value.copy(
            form = WifiProfileFormState(
                isPasswordChanged = true
            ),
            isEditingWifiProfile = true,
            errorMessage = null
        )
    }

    fun startEditWifiProfile(profile: WifiProfile) {
        _uiState.value = _uiState.value.copy(
            form = WifiProfileFormState(
                id = profile.id,
                ssid = profile.ssid
            ),
            isEditingWifiProfile = true,
            errorMessage = null
        )

        viewModelScope.launch {
            val savedPassword = runCatching {
                wifiProfileRepository.getPassword(profile.id)
            }.getOrNull()

            _uiState.value = _uiState.value.copy(
                form = _uiState.value.form.copy(
                    password = savedPassword
                        ?.takeIf { it.isNotEmpty() }
                        ?.let { PASSWORD_MASK }
                        .orEmpty(),
                    hasSavedPassword = !savedPassword.isNullOrEmpty(),
                    isPasswordChanged = false
                )
            )
        }
    }

    fun cancelWifiProfileEdit() {
        _uiState.value = _uiState.value.copy(
            form = WifiProfileFormState(),
            isEditingWifiProfile = false,
            errorMessage = null
        )
    }

    fun updateWifiProfileSsid(ssid: String) {
        _uiState.value = _uiState.value.copy(
            form = _uiState.value.form.copy(ssid = ssid),
            errorMessage = null
        )
    }

    fun updateWifiProfilePassword(password: String) {
        _uiState.value = _uiState.value.copy(
            form = _uiState.value.form.copy(
                password = password,
                hasSavedPassword = false,
                isPasswordChanged = true
            ),
            errorMessage = null
        )
    }

    fun clearWifiProfilePassword() {
        _uiState.value = _uiState.value.copy(
            form = _uiState.value.form.copy(
                password = "",
                hasSavedPassword = false,
                isPasswordVisible = false,
                isPasswordChanged = true
            ),
            errorMessage = null
        )
    }

    fun toggleWifiPasswordVisibility() {
        val form = _uiState.value.form

        if (form.isPasswordVisible) {
            _uiState.value = _uiState.value.copy(
                form = form.copy(
                    password = if (form.hasSavedPassword && !form.isPasswordChanged) {
                        PASSWORD_MASK
                    } else {
                        form.password
                    },
                    isPasswordVisible = false
                )
            )
            return
        }

        if (form.hasSavedPassword && !form.isPasswordChanged && form.id != null) {
            viewModelScope.launch {
                val savedPassword = runCatching {
                    wifiProfileRepository.getPassword(form.id)
                }.getOrNull()

                _uiState.value = _uiState.value.copy(
                    form = _uiState.value.form.copy(
                        password = savedPassword ?: PASSWORD_MASK,
                        isPasswordVisible = true
                    )
                )
            }
        } else {
            _uiState.value = _uiState.value.copy(
                form = form.copy(
                    isPasswordVisible = true
                )
            )
        }
    }

    fun saveWifiProfile() {
        val form = _uiState.value.form
        val trimmedSsid = form.ssid.trim()

        if (trimmedSsid.isBlank()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "SSID darf nicht leer sein"
            )
            return
        }

        viewModelScope.launch {
            runCatching {
                val profile = WifiProfile(
                    id = form.id ?: UUID.randomUUID().toString(),
                    ssid = trimmedSsid
                )

                val passwordToSave = if (form.isPasswordChanged) {
                    form.password.takeIf { it.isNotEmpty() }
                } else {
                    null
                }

                wifiProfileRepository.saveWifiProfile(
                    profile = profile,
                    password = passwordToSave,
                    shouldUpdatePassword = form.isPasswordChanged
                )
            }.onSuccess {
                _uiState.value = _uiState.value.copy(
                    form = WifiProfileFormState(),
                    isEditingWifiProfile = false,
                    errorMessage = null
                )
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "WLAN-Profil konnte nicht gespeichert werden"
                )
            }
        }
    }

    fun deleteWifiProfile(profileId: String) {
        viewModelScope.launch {
            runCatching {
                wifiProfileRepository.deleteWifiProfile(profileId)
            }.onFailure { error ->
                _uiState.value = _uiState.value.copy(
                    errorMessage = error.message ?: "WLAN-Profil konnte nicht gelöscht werden"
                )
            }
        }
    }

    private fun observeWifiProfiles() {
        viewModelScope.launch {
            wifiProfileRepository.observeWifiProfiles().collect { profiles ->
                _uiState.value = _uiState.value.copy(
                    wifiProfiles = profiles,
                    errorMessage = null
                )
            }
        }
    }

    private companion object {
        const val PASSWORD_MASK = "********"
    }
}
