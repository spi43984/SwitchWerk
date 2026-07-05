package de.piecha.switchwerk.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import de.piecha.switchwerk.data.local.entity.SwitchGroupEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SwitchGroupDao {
    @Query("SELECT * FROM switch_groups ORDER BY sortOrder")
    fun observeAll(): Flow<List<SwitchGroupEntity>>

    @Query("SELECT * FROM switch_groups ORDER BY sortOrder")
    suspend fun getAll(): List<SwitchGroupEntity>

    @Upsert
    suspend fun upsert(group: SwitchGroupEntity)

    @Upsert
    suspend fun upsertAll(groups: List<SwitchGroupEntity>)

    @Query("UPDATE switch_groups SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: String, sortOrder: Int)

    @Query("DELETE FROM switch_groups WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM switch_groups")
    suspend fun deleteAll()
}
