package de.piecha.switchwerk.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "switch_group_members")
data class SwitchGroupMemberEntity(
    @PrimaryKey val id: String,
    val groupId: String,
    val deviceId: String,
    val sortOrder: Int,
    val pauseAfterMillis: Long
)
