package de.piecha.switchwerk.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import de.piecha.switchwerk.data.local.entity.DeviceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY sortOrder")
    fun observeAll(): Flow<List<DeviceEntity>>

    @Query("SELECT * FROM devices ORDER BY sortOrder")
    suspend fun getAll(): List<DeviceEntity>

    @Query("SELECT * FROM devices WHERE id = :id")
    suspend fun getById(id: String): DeviceEntity?

    @Upsert
    suspend fun upsert(device: DeviceEntity)

    @Upsert
    suspend fun upsertAll(devices: List<DeviceEntity>)

    @Query("UPDATE devices SET sortOrder = :sortOrder WHERE id = :id")
    suspend fun updateSortOrder(id: String, sortOrder: Int)

    @Query("DELETE FROM devices WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM devices")
    suspend fun deleteAll()
}
