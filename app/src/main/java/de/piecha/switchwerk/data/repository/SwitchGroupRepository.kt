package de.piecha.switchwerk.data.repository

import de.piecha.switchwerk.domain.model.SwitchGroup
import kotlinx.coroutines.flow.Flow

interface SwitchGroupRepository {
    fun observeSwitchGroups(): Flow<List<SwitchGroup>>
    suspend fun getSwitchGroups(): List<SwitchGroup>
    suspend fun saveSwitchGroup(group: SwitchGroup)
    suspend fun updateSwitchGroupOrder(groupIds: List<String>)
    suspend fun updateSwitchGroupSortOrders(sortOrders: Map<String, Int>)
    suspend fun deleteSwitchGroup(groupId: String)
}
