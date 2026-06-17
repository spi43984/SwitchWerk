package de.piecha.switchwerk.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import de.piecha.switchwerk.data.local.entity.WifiProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WifiProfileDao {
    @Query("SELECT * FROM wifi_profiles ORDER BY name, ssid")
    fun observeAll(): Flow<List<WifiProfileEntity>>

    @Query("SELECT * FROM wifi_profiles ORDER BY name, ssid")
    suspend fun getAll(): List<WifiProfileEntity>

    @Query("SELECT * FROM wifi_profiles WHERE id = :id")
    suspend fun getById(id: String): WifiProfileEntity?

    @Upsert
    suspend fun upsert(profile: WifiProfileEntity)

    @Upsert
    suspend fun upsertAll(profiles: List<WifiProfileEntity>)

    @Query("DELETE FROM wifi_profiles WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM wifi_profiles")
    suspend fun deleteAll()
}
