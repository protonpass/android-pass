package proton.android.pass.data.impl.db

import androidx.room.DeleteTable
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.key.data.db.PublicAddressDatabase
import me.proton.core.keytransparency.data.local.KeyTransparencyDatabase
import me.proton.core.observability.data.db.ObservabilityDatabase
import me.proton.core.usersettings.data.db.OrganizationDatabase
import me.proton.core.user.data.db.AddressDatabase

@Suppress("ClassNaming")
object AppDatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            ObservabilityDatabase.MIGRATION_0.migrate(database)
        }
    }

    @DeleteTable.Entries(value = [DeleteTable(tableName = "SelectedShareEntity")])
    class MIGRATION_2_3 : AutoMigrationSpec

    val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            OrganizationDatabase.MIGRATION_2.migrate(database)
        }
    }

    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("UPDATE AccountMetadataEntity SET product = 'Pass'")
        }
    }

    val MIGRATION_8_9 = object : Migration(8, 9) {
        override fun migrate(database: SupportSQLiteDatabase) {
            AddressDatabase.MIGRATION_4.migrate(database)
            PublicAddressDatabase.MIGRATION_2.migrate(database)
            KeyTransparencyDatabase.MIGRATION_0.migrate(database)
        }
    }
}
