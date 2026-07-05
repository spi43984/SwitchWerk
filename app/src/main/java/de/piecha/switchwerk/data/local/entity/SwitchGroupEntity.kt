package de.piecha.switchwerk.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "switch_groups")
data class SwitchGroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val actionLabel: String,
    val sortOrder: Int,
    val errorStrategy: String = "ABORT_ON_ERROR"
)
