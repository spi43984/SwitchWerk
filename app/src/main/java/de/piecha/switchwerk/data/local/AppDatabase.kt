package de.piecha.switchwerk.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import de.piecha.switchwerk.data.local.dao.DeviceConnectionDao
import de.piecha.switchwerk.data.local.dao.DeviceDao
import de.piecha.switchwerk.data.local.dao.SwitchGroupDao
import de.piecha.switchwerk.data.local.dao.SwitchGroupMemberDao
import de.piecha.switchwerk.data.local.dao.WifiProfileDao
import de.piecha.switchwerk.data.local.entity.DeviceConnectionEntity
import de.piecha.switchwerk.data.local.entity.DeviceEntity
import de.piecha.switchwerk.data.local.entity.SwitchGroupEntity
import de.piecha.switchwerk.data.local.entity.SwitchGroupMemberEntity
import de.piecha.switchwerk.data.local.entity.WifiProfileEntity

@Database(
    entities = [
        DeviceEntity::class,
        WifiProfileEntity::class,
        DeviceConnectionEntity::class,
        SwitchGroupEntity::class,
        SwitchGroupMemberEntity::class
    ],
    version = 12,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun deviceDao(): DeviceDao

    abstract fun wifiProfileDao(): WifiProfileDao

    abstract fun deviceConnectionDao(): DeviceConnectionDao

    abstract fun switchGroupDao(): SwitchGroupDao

    abstract fun switchGroupMemberDao(): SwitchGroupMemberDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                if (!db.hasColumn(tableName = "devices", columnName = "sortOrder")) {
                    db.execSQL(
                        """
                        CREATE TABLE devices_new (
                            id TEXT NOT NULL,
                            name TEXT NOT NULL,
                            actionLabel TEXT NOT NULL,
                            apiMethod TEXT NOT NULL,
                            apiPath TEXT NOT NULL,
                            sortOrder INTEGER NOT NULL,
                            PRIMARY KEY(id)
                        )
                        """.trimIndent()
                    )

                    val cursor = db.query(
                        "SELECT id, name, actionLabel, apiMethod, apiPath FROM devices ORDER BY rowid"
                    )
                    cursor.use {
                        var sortOrder = 0
                        while (it.moveToNext()) {
                            db.execSQL(
                                """
                                INSERT INTO devices_new (
                                    id,
                                    name,
                                    actionLabel,
                                    apiMethod,
                                    apiPath,
                                    sortOrder
                                ) VALUES (?, ?, ?, ?, ?, ?)
                                """.trimIndent(),
                                arrayOf(
                                    it.getString(0),
                                    it.getString(1),
                                    it.getString(2),
                                    it.getString(3),
                                    it.getString(4),
                                    sortOrder
                                )
                            )
                            sortOrder += 1
                        }
                    }
                    db.execSQL("DROP TABLE devices")
                    db.execSQL("ALTER TABLE devices_new RENAME TO devices")
                }
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                if (!db.hasColumn(tableName = "wifi_profiles", columnName = "name")) {
                    db.execSQL("ALTER TABLE wifi_profiles ADD COLUMN name TEXT NOT NULL DEFAULT ''")

                    val usedNames = mutableSetOf<String>()
                    val cursor = db.query("SELECT id, ssid FROM wifi_profiles ORDER BY rowid")
                    cursor.use {
                        while (it.moveToNext()) {
                            val id = it.getString(0)
                            val ssid = it.getString(1).trim()
                            val name = uniqueProfileName(
                                baseName = ssid.ifBlank { "WLAN-Profil" },
                                usedNames = usedNames
                            )
                            db.execSQL(
                                "UPDATE wifi_profiles SET name = ? WHERE id = ?",
                                arrayOf(name, id)
                            )
                        }
                    }
                }
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE wifi_profiles_new (
                        id TEXT NOT NULL,
                        name TEXT NOT NULL,
                        ssid TEXT NOT NULL,
                        securityType TEXT,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO wifi_profiles_new (id, name, ssid, securityType)
                    SELECT id, name, ssid, securityType FROM wifi_profiles
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE wifi_profiles")
                db.execSQL("ALTER TABLE wifi_profiles_new RENAME TO wifi_profiles")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE wifi_profiles
                    ADD COLUMN securityTypeVerifiedLocally INTEGER NOT NULL DEFAULT 1
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE wifi_profiles
                    ADD COLUMN connectionMode TEXT NOT NULL DEFAULT 'SWITCHWERK_MANAGED'
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE devices
                    ADD COLUMN apiProtocol TEXT NOT NULL DEFAULT 'HTTP'
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE devices
                    ADD COLUMN apiRequestBody TEXT NOT NULL DEFAULT ''
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    ALTER TABLE devices
                    ADD COLUMN apiContentType TEXT NOT NULL DEFAULT 'APPLICATION_JSON'
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE devices ADD COLUMN shortcutEnabled INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS switch_groups (
                        id TEXT NOT NULL,
                        name TEXT NOT NULL,
                        actionLabel TEXT NOT NULL,
                        sortOrder INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS switch_group_members (
                        id TEXT NOT NULL,
                        groupId TEXT NOT NULL,
                        deviceId TEXT NOT NULL,
                        sortOrder INTEGER NOT NULL,
                        pauseAfterMillis INTEGER NOT NULL,
                        PRIMARY KEY(id)
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE switch_groups
                    ADD COLUMN errorStrategy TEXT NOT NULL DEFAULT 'ABORT_ON_ERROR'
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    ALTER TABLE switch_groups
                    ADD COLUMN shortcutEnabled INTEGER NOT NULL DEFAULT 0
                    """.trimIndent()
                )
            }
        }
    }
}

private fun uniqueProfileName(
    baseName: String,
    usedNames: MutableSet<String>
): String {
    var candidate = baseName
    var suffix = 2
    while (!usedNames.add(candidate.lowercase())) {
        candidate = "$baseName ($suffix)"
        suffix += 1
    }
    return candidate
}

private fun SupportSQLiteDatabase.hasColumn(
    tableName: String,
    columnName: String
): Boolean {
    val cursor = query("PRAGMA table_info($tableName)")
    cursor.use {
        val nameIndex = it.getColumnIndex("name")
        while (it.moveToNext()) {
            if (it.getString(nameIndex) == columnName) {
                return true
            }
        }
    }
    return false
}
