package me.proton.android.pass.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.pass.data.db.PassDatabase
import me.proton.core.usersettings.data.db.OrganizationDatabase

object AppDatabaseMigrations {

    val MIGRATION_0_1 = object : Migration(0, 1) {
        override fun migrate(database: SupportSQLiteDatabase) {
            PassDatabase.MIGRATION_0.migrate(database)
            OrganizationDatabase.MIGRATION_0.migrate(database)
        }
    }

}
