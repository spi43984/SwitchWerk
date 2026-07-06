package de.piecha.switchwerk.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "devices")
data class DeviceEntity(
    @PrimaryKey val id: String,
    val name: String,
    val actionLabel: String,
    val apiProtocol: String,
    val apiMethod: String,
    val apiPath: String,
    val apiRequestBody: String,
    val apiContentType: String,
    val sortOrder: Int,
    val shortcutEnabled: Boolean = false,
    val color: String = "NONE"
)
