package de.piecha.switchwerk.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import de.piecha.switchwerk.data.local.entity.DeviceConnectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceConnectionDao {

    @Query("SELECT * FROM connections ORDER BY priority")
    fun observeAll(): Flow<List<DeviceConnectionEntity>>

    @Query("SELECT * FROM connections ORDER BY priority")
    suspend fun getAll(): List<DeviceConnectionEntity>

    @Query("SELECT * FROM connections WHERE deviceId = :deviceId ORDER BY priority")
    fun observeForDevice(deviceId: String): Flow<List<DeviceConnectionEntity>>

    @Query("SELECT * FROM connections WHERE deviceId = :deviceId ORDER BY priority")
    suspend fun getForDevice(deviceId: String): List<DeviceConnectionEntity>

    @Upsert
    suspend fun upsert(connection: DeviceConnectionEntity)

    @Upsert
    suspend fun upsertAll(connections: List<DeviceConnectionEntity>)

    @Query("DELETE FROM connections WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM connections WHERE deviceId = :deviceId")
    suspend fun deleteForDevice(deviceId: String)

    @Query("DELETE FROM connections")
    suspend fun deleteAll()
}
