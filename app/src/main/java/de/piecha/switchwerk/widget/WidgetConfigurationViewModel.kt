package de.piecha.switchwerk.widget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.piecha.switchwerk.data.repository.DeviceRepository
import de.piecha.switchwerk.data.repository.SwitchGroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class WidgetConfigurationUiState(
    val title: String = "",
    val columnMode: WidgetColumnMode = WidgetColumnMode.AUTO,
    val availableActions: List<AvailableWidgetAction> = emptyList(),
    val selectedTargets: List<WidgetActionTarget> = emptyList(),
    val isLoading: Boolean = true
)

class WidgetConfigurationViewModel(
    private val deviceRepository: DeviceRepository,
    private val switchGroupRepository: SwitchGroupRepository,
    private val store: WidgetActionStore,
    private val renderer: SwitchWerkWidgetRenderer
) : ViewModel() {

    private val _uiState = MutableStateFlow(WidgetConfigurationUiState())
    val uiState: StateFlow<WidgetConfigurationUiState> = _uiState.asStateFlow()
    private var appWidgetId: Int? = null

    fun load(appWidgetId: Int, defaultTitle: String) {
        if (this.appWidgetId == appWidgetId) return
        this.appWidgetId = appWidgetId
        val isExistingWidget = appWidgetId in store.getWidgetIds()
        _uiState.value = _uiState.value.copy(
            title = if (isExistingWidget) store.getTitle(appWidgetId) else defaultTitle,
            columnMode = store.getColumnMode(appWidgetId),
            selectedTargets = store.getTargets(appWidgetId)
        )
        viewModelScope.launch {
            combine(
                deviceRepository.observeDevices(),
                switchGroupRepository.observeSwitchGroups()
            ) { devices, groups ->
                availableWidgetActions(devices, groups)
            }.collect { actions ->
                val availableTargets = actions.map { it.target }.toSet()
                _uiState.value = _uiState.value.copy(
                    availableActions = actions,
                    selectedTargets = _uiState.value.selectedTargets.filter { it in availableTargets },
                    isLoading = false
                )
            }
        }
    }

    fun toggle(target: WidgetActionTarget) {
        val current = _uiState.value.selectedTargets
        val updated = if (target in current) {
            current.filterNot { it == target }
        } else {
            current + target
        }
        _uiState.value = _uiState.value.copy(selectedTargets = updated)
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title)
    }

    fun updateColumnMode(columnMode: WidgetColumnMode) {
        _uiState.value = _uiState.value.copy(columnMode = columnMode)
    }

    suspend fun save(): Boolean {
        val widgetId = appWidgetId ?: return false
        val selectedTargets = _uiState.value.selectedTargets
        if (selectedTargets.isEmpty()) return false
        store.saveWidget(
            widgetId,
            _uiState.value.title,
            _uiState.value.columnMode,
            selectedTargets
        )
        renderer.updateWidget(widgetId)
        return true
    }
}
