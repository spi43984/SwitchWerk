package de.piecha.switchwerk.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import de.piecha.switchwerk.data.local.entity.SwitchGroupMemberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SwitchGroupMemberDao {
    @Query("SELECT * FROM switch_group_members ORDER BY sortOrder")
    fun observeAll(): Flow<List<SwitchGroupMemberEntity>>

    @Query("SELECT * FROM switch_group_members ORDER BY sortOrder")
    suspend fun getAll(): List<SwitchGroupMemberEntity>

    @Query("SELECT * FROM switch_group_members WHERE groupId = :groupId ORDER BY sortOrder")
    suspend fun getForGroup(groupId: String): List<SwitchGroupMemberEntity>

    @Upsert
    suspend fun upsertAll(members: List<SwitchGroupMemberEntity>)

    @Query("DELETE FROM switch_group_members WHERE groupId = :groupId")
    suspend fun deleteForGroup(groupId: String)

    @Query("DELETE FROM switch_group_members WHERE deviceId = :deviceId")
    suspend fun deleteForDevice(deviceId: String)

    @Query("DELETE FROM switch_group_members")
    suspend fun deleteAll()
}
