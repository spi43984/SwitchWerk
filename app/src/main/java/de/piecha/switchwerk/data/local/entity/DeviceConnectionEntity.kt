package de.piecha.switchwerk.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "connections")
data class DeviceConnectionEntity(
    @PrimaryKey val id: String,
    val deviceId: String,
    val wifiProfileId: String,
    val host: String,
    val priority: Int
)
