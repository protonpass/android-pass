package me.proton.android.pass.data.impl.db

import androidx.room.Room
import androidx.room.migration.AutoMigrationSpec
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import me.proton.android.pass.data.impl.db.AppDatabaseMigrations.MIGRATION_1_2
import me.proton.android.pass.data.impl.db.AppDatabaseMigrations.MIGRATION_2_3
import me.proton.android.pass.data.impl.db.AppDatabaseMigrations.MIGRATION_3_4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    // Array of all migrations
    private val migrations = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
    private val autoMigrations: List<AutoMigrationSpec> = emptyList()

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
        autoMigrations,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrateAll() {
        // Create earliest version of the database.
        helper.createDatabase(AppDatabase.DB_NAME, 1).apply { close() }

        val builder = Room.databaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            AppDatabase::class.java,
            AppDatabase.DB_NAME
        ).addMigrations(*migrations)

        autoMigrations.forEach { builder.addAutoMigrationSpec(it) }

        val database = builder.build()

        // Ensure we are on the latest version
        helper.runMigrationsAndValidate(AppDatabase.DB_NAME, AppDatabase.VERSION, false, *migrations)

        // Open latest version of the database. Room will validate the schema
        // once all migrations execute.
        database.openHelper.writableDatabase.close()
    }
}
