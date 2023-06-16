/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.data.impl.db

import androidx.room.Room
import androidx.room.migration.AutoMigrationSpec
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

private const val VALIDATE_DROPPED_TABLES = true

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    // List of autoMigrations
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
        ).addMigrations(*AppDatabase.migrations.toTypedArray())

        autoMigrations.forEach { builder.addAutoMigrationSpec(it) }

        val database = builder.build()

        // Ensure we are on the latest version
        helper.runMigrationsAndValidate(
            AppDatabase.DB_NAME,
            AppDatabase.VERSION,
            VALIDATE_DROPPED_TABLES,
            *AppDatabase.migrations.toTypedArray()
        )

        // Open latest version of the database. Room will validate the schema
        // once all migrations execute.
        database.openHelper.writableDatabase.close()
    }
}
