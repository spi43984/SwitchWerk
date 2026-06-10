package de.piecha.switchwerk.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import de.piecha.switchwerk.data.local.dao.DeviceConnectionDao
import de.piecha.switchwerk.data.local.dao.DeviceDao
import de.piecha.switchwerk.data.local.dao.WifiProfileDao
import de.piecha.switchwerk.data.local.entity.DeviceConnectionEntity
import de.piecha.switchwerk.data.local.entity.DeviceEntity
import de.piecha.switchwerk.data.local.entity.WifiProfileEntity

@Database(
    entities = [
        DeviceEntity::class,
        WifiProfileEntity::class,
        DeviceConnectionEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun deviceDao(): DeviceDao

    abstract fun wifiProfileDao(): WifiProfileDao

    abstract fun deviceConnectionDao(): DeviceConnectionDao
}
