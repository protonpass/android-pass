package me.proton.android.pass.db

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.featureflag.data.db.FeatureFlagDatabase
import me.proton.core.humanverification.data.db.HumanVerificationDatabase
import me.proton.core.pass.data.db.PassDatabase
import me.proton.core.usersettings.data.db.OrganizationDatabase

object AppDatabaseMigrations {

    val MIGRATION_0_1 = object : Migration(0, 1) {
        override fun migrate(database: SupportSQLiteDatabase) {
            PassDatabase.MIGRATION_0.migrate(database)
            OrganizationDatabase.MIGRATION_0.migrate(database)
        }
    }

    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            FeatureFlagDatabase.MIGRATION_1.migrate(database)
            FeatureFlagDatabase.MIGRATION_2.migrate(database)
            FeatureFlagDatabase.MIGRATION_3.migrate(database)
            HumanVerificationDatabase.MIGRATION_1.migrate(database)
            HumanVerificationDatabase.MIGRATION_2.migrate(database)
        }
    }
}
