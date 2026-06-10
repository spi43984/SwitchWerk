package de.piecha.switchwerk.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wifi_profiles")
data class WifiProfileEntity(
    @PrimaryKey val id: String,
    val ssid: String,
    val securityType: String
)
