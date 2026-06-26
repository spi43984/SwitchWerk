package de.piecha.switchwerk.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {

    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun deleteDatabaseBeforeTest() {
        context.deleteDatabase(TEST_DATABASE)
    }

    @After
    fun deleteDatabaseAfterTest() {
        context.deleteDatabase(TEST_DATABASE)
    }

    @Test
    fun migration2To3PreservesWifiProfilesAndCreatesNames() {
        val helper = createDatabase(version = 2) { db ->
            createVersion2Schema(db)
            insertVersion2WifiProfile(
                db = db,
                id = "wifi-1",
                ssid = "Example WiFi",
                securityType = "WPA2"
            )
            insertVersion2WifiProfile(
                db = db,
                id = "wifi-2",
                ssid = "Example WiFi",
                securityType = "WPA2"
            )
            insertVersion2WifiProfile(
                db = db,
                id = "wifi-3",
                ssid = "",
                securityType = "OPEN"
            )
        }

        helper.useDatabase { db ->
            AppDatabase.MIGRATION_2_3.migrate(db)

            val profiles = db.queryRows(
                "SELECT id, name, ssid, securityType FROM wifi_profiles ORDER BY id"
            )

            assertEquals(
                listOf(
                    listOf("wifi-1", "Example WiFi", "Example WiFi", "WPA2"),
                    listOf("wifi-2", "Example WiFi (2)", "Example WiFi", "WPA2"),
                    listOf("wifi-3", "WLAN-Profil", "", "OPEN")
                ),
                profiles
            )
        }
    }

    @Test
    fun migration3To4PreservesWifiProfilesWhenRebuildingTable() {
        val helper = createDatabase(version = 3) { db ->
            createVersion3Schema(db)
            insertVersion3WifiProfile(
                db = db,
                id = "wifi-1",
                name = "Example WiFi",
                ssid = "Example WiFi",
                securityType = "WPA2"
            )
            insertVersion3WifiProfile(
                db = db,
                id = "wifi-2",
                name = "Guest WiFi",
                ssid = "Guest WiFi",
                securityType = "OPEN"
            )
        }

        helper.useDatabase { db ->
            AppDatabase.MIGRATION_3_4.migrate(db)

            val profiles = db.queryRows(
                "SELECT id, name, ssid, securityType FROM wifi_profiles ORDER BY id"
            )

            assertEquals(
                listOf(
                    listOf("wifi-1", "Example WiFi", "Example WiFi", "WPA2"),
                    listOf("wifi-2", "Guest WiFi", "Guest WiFi", "OPEN")
                ),
                profiles
            )
        }
    }

    @Test
    fun migration4To5AddsVerifiedLocallyDefault() {
        val helper = createDatabase(version = 4) { db ->
            createVersion4Schema(db)
            insertVersion4WifiProfile(
                db = db,
                id = "wifi-1",
                name = "Example WiFi",
                ssid = "Example WiFi",
                securityType = null
            )
        }

        helper.useDatabase { db ->
            AppDatabase.MIGRATION_4_5.migrate(db)

            assertEquals(
                listOf(listOf("wifi-1", "1")),
                db.queryRows(
                    "SELECT id, securityTypeVerifiedLocally FROM wifi_profiles ORDER BY id"
                )
            )
        }
    }

    @Test
    fun migration5To6AddsConnectionModeDefault() {
        val helper = createDatabase(version = 5) { db ->
            createVersion5Schema(db)
            insertVersion5WifiProfile(
                db = db,
                id = "wifi-1",
                name = "Example WiFi",
                ssid = "Example WiFi",
                securityType = "WPA2",
                securityTypeVerifiedLocally = true
            )
        }

        helper.useDatabase { db ->
            AppDatabase.MIGRATION_5_6.migrate(db)

            assertEquals(
                listOf(listOf("wifi-1", "SWITCHWERK_MANAGED")),
                db.queryRows(
                    "SELECT id, connectionMode FROM wifi_profiles ORDER BY id"
                )
            )
        }
    }

    @Test
    fun migration6To7AddsApiProtocolDefault() {
        val helper = createDatabase(version = 6) { db ->
            createVersion6Schema(db)
            insertVersion6Device(db)
        }

        helper.useDatabase { db ->
            AppDatabase.MIGRATION_6_7.migrate(db)

            assertEquals(
                listOf(listOf("device-1", "HTTP")),
                db.queryRows("SELECT id, apiProtocol FROM devices ORDER BY id")
            )
        }
    }

    @Test
    fun migration7To8AddsRequestBodyAndContentTypeDefaults() {
        val helper = createDatabase(version = 7) { db ->
            createVersion7Schema(db)
            insertVersion7Device(db)
        }

        helper.useDatabase { db ->
            AppDatabase.MIGRATION_7_8.migrate(db)

            assertEquals(
                listOf(listOf("device-1", "", "APPLICATION_JSON")),
                db.queryRows(
                    """
                    SELECT id, apiRequestBody, apiContentType
                    FROM devices
                    ORDER BY id
                    """.trimIndent()
                )
            )
        }
    }

    @Test
    fun migration2ToCurrentVersionMigratesEndToEnd() {
        createDatabase(version = 2) { db ->
            createVersion2Schema(db)
            insertVersion2WifiProfile(
                db = db,
                id = "wifi-1",
                ssid = "Example WiFi",
                securityType = "WPA2"
            )
            insertVersion2Device(db)
            insertConnection(db)
        }.close()

        val database = Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            TEST_DATABASE
        )
            .addMigrations(*ALL_MIGRATIONS)
            .build()

        try {
            val db = database.openHelper.writableDatabase

            assertEquals(
                listOf(
                    listOf(
                        "wifi-1",
                        "Example WiFi",
                        "Example WiFi",
                        "WPA2",
                        "1",
                        "SWITCHWERK_MANAGED"
                    )
                ),
                db.queryRows(
                    """
                    SELECT id, name, ssid, securityType,
                        securityTypeVerifiedLocally, connectionMode
                    FROM wifi_profiles
                    ORDER BY id
                    """.trimIndent()
                )
            )
            assertEquals(
                listOf(
                    listOf(
                        "device-1",
                        "HTTP",
                        "GET",
                        "/relay/0",
                        "",
                        "APPLICATION_JSON",
                        "0"
                    )
                ),
                db.queryRows(
                    """
                    SELECT id, apiProtocol, apiMethod, apiPath,
                        apiRequestBody, apiContentType, sortOrder
                    FROM devices
                    ORDER BY id
                    """.trimIndent()
                )
            )
            assertEquals(
                listOf(listOf("connection-1", "device-1", "wifi-1", "device.local", "0")),
                db.queryRows(
                    """
                    SELECT id, deviceId, wifiProfileId, host, priority
                    FROM connections
                    ORDER BY id
                    """.trimIndent()
                )
            )
        } finally {
            database.close()
        }
    }

    private fun createDatabase(
        version: Int,
        createSchema: (SupportSQLiteDatabase) -> Unit
    ): SupportSQLiteOpenHelper {
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(TEST_DATABASE)
                .callback(
                    object : SupportSQLiteOpenHelper.Callback(version) {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            createSchema(db)
                        }

                        override fun onUpgrade(
                            db: SupportSQLiteDatabase,
                            oldVersion: Int,
                            newVersion: Int
                        ) = Unit
                    }
                )
                .build()
        )
        helper.writableDatabase.close()
        return helper
    }

    private fun SupportSQLiteOpenHelper.useDatabase(
        block: (SupportSQLiteDatabase) -> Unit
    ) {
        try {
            block(writableDatabase)
        } finally {
            close()
        }
    }

    private fun SupportSQLiteDatabase.queryRows(
        sql: String
    ): List<List<String?>> {
        val cursor = query(sql)
        cursor.use {
            val rows = mutableListOf<List<String?>>()
            while (it.moveToNext()) {
                val row = (0 until it.columnCount).map { index ->
                    if (it.isNull(index)) null else it.getString(index)
                }
                rows += row
            }
            return rows
        }
    }

    private fun createVersion2Schema(db: SupportSQLiteDatabase) {
        createVersion2DevicesTable(db)
        createVersion2WifiProfilesTable(db)
        createConnectionsTable(db)
    }

    private fun createVersion3Schema(db: SupportSQLiteDatabase) {
        createVersion2DevicesTable(db)
        createVersion3WifiProfilesTable(db)
        createConnectionsTable(db)
    }

    private fun createVersion4Schema(db: SupportSQLiteDatabase) {
        createVersion2DevicesTable(db)
        createVersion4WifiProfilesTable(db)
        createConnectionsTable(db)
    }

    private fun createVersion5Schema(db: SupportSQLiteDatabase) {
        createVersion2DevicesTable(db)
        createVersion5WifiProfilesTable(db)
        createConnectionsTable(db)
    }

    private fun createVersion6Schema(db: SupportSQLiteDatabase) {
        createVersion2DevicesTable(db)
        createVersion6WifiProfilesTable(db)
        createConnectionsTable(db)
    }

    private fun createVersion7Schema(db: SupportSQLiteDatabase) {
        createVersion7DevicesTable(db)
        createVersion6WifiProfilesTable(db)
        createConnectionsTable(db)
    }

    private fun createVersion2DevicesTable(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE devices (
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
    }

    private fun createVersion7DevicesTable(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE devices (
                id TEXT NOT NULL,
                name TEXT NOT NULL,
                actionLabel TEXT NOT NULL,
                apiProtocol TEXT NOT NULL,
                apiMethod TEXT NOT NULL,
                apiPath TEXT NOT NULL,
                sortOrder INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent()
        )
    }

    private fun createVersion2WifiProfilesTable(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE wifi_profiles (
                id TEXT NOT NULL,
                ssid TEXT NOT NULL,
                securityType TEXT NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent()
        )
    }

    private fun createVersion3WifiProfilesTable(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE wifi_profiles (
                id TEXT NOT NULL,
                name TEXT NOT NULL,
                ssid TEXT NOT NULL,
                securityType TEXT NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent()
        )
    }

    private fun createVersion4WifiProfilesTable(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE wifi_profiles (
                id TEXT NOT NULL,
                name TEXT NOT NULL,
                ssid TEXT NOT NULL,
                securityType TEXT,
                PRIMARY KEY(id)
            )
            """.trimIndent()
        )
    }

    private fun createVersion5WifiProfilesTable(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE wifi_profiles (
                id TEXT NOT NULL,
                name TEXT NOT NULL,
                ssid TEXT NOT NULL,
                securityType TEXT,
                securityTypeVerifiedLocally INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent()
        )
    }

    private fun createVersion6WifiProfilesTable(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE wifi_profiles (
                id TEXT NOT NULL,
                name TEXT NOT NULL,
                ssid TEXT NOT NULL,
                connectionMode TEXT NOT NULL,
                securityType TEXT,
                securityTypeVerifiedLocally INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent()
        )
    }

    private fun createConnectionsTable(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE connections (
                id TEXT NOT NULL,
                deviceId TEXT NOT NULL,
                wifiProfileId TEXT NOT NULL,
                host TEXT NOT NULL,
                priority INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent()
        )
    }

    private fun insertVersion2WifiProfile(
        db: SupportSQLiteDatabase,
        id: String,
        ssid: String,
        securityType: String
    ) {
        db.execSQL(
            "INSERT INTO wifi_profiles (id, ssid, securityType) VALUES (?, ?, ?)",
            arrayOf(id, ssid, securityType)
        )
    }

    private fun insertVersion3WifiProfile(
        db: SupportSQLiteDatabase,
        id: String,
        name: String,
        ssid: String,
        securityType: String
    ) {
        db.execSQL(
            """
            INSERT INTO wifi_profiles (id, name, ssid, securityType)
            VALUES (?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>(id, name, ssid, securityType)
        )
    }

    private fun insertVersion4WifiProfile(
        db: SupportSQLiteDatabase,
        id: String,
        name: String,
        ssid: String,
        securityType: String?
    ) {
        db.execSQL(
            """
            INSERT INTO wifi_profiles (id, name, ssid, securityType)
            VALUES (?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>(id, name, ssid, securityType)
        )
    }

    private fun insertVersion5WifiProfile(
        db: SupportSQLiteDatabase,
        id: String,
        name: String,
        ssid: String,
        securityType: String?,
        securityTypeVerifiedLocally: Boolean
    ) {
        db.execSQL(
            """
            INSERT INTO wifi_profiles (
                id,
                name,
                ssid,
                securityType,
                securityTypeVerifiedLocally
            ) VALUES (?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>(
                id,
                name,
                ssid,
                securityType,
                if (securityTypeVerifiedLocally) 1 else 0
            )
        )
    }

    private fun insertVersion6Device(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            INSERT INTO devices (id, name, actionLabel, apiMethod, apiPath, sortOrder)
            VALUES (?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>("device-1", "Example Device", "Toggle", "GET", "/relay/0", 0)
        )
    }

    private fun insertVersion7Device(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            INSERT INTO devices (
                id,
                name,
                actionLabel,
                apiProtocol,
                apiMethod,
                apiPath,
                sortOrder
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>("device-1", "Example Device", "Toggle", "HTTP", "GET", "/relay/0", 0)
        )
    }

    private fun insertVersion2Device(db: SupportSQLiteDatabase) {
        insertVersion6Device(db)
    }

    private fun insertConnection(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            INSERT INTO connections (id, deviceId, wifiProfileId, host, priority)
            VALUES (?, ?, ?, ?, ?)
            """.trimIndent(),
            arrayOf<Any?>("connection-1", "device-1", "wifi-1", "device.local", 0)
        )
    }

    private companion object {
        const val TEST_DATABASE = "migration-test.db"

        val ALL_MIGRATIONS: Array<Migration> = arrayOf(
            AppDatabase.MIGRATION_1_2,
            AppDatabase.MIGRATION_2_3,
            AppDatabase.MIGRATION_3_4,
            AppDatabase.MIGRATION_4_5,
            AppDatabase.MIGRATION_5_6,
            AppDatabase.MIGRATION_6_7,
            AppDatabase.MIGRATION_7_8
        )
    }
}
