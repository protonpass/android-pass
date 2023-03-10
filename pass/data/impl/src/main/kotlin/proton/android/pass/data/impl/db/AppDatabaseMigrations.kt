package proton.android.pass.data.impl.db

import androidx.room.DeleteTable
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.observability.data.db.ObservabilityDatabase

@Suppress("ClassNaming")
object AppDatabaseMigrations {
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            ObservabilityDatabase.MIGRATION_0.migrate(database)
        }
    }

    @DeleteTable.Entries(value = [DeleteTable(tableName = "SelectedShareEntity")])
    class MIGRATION_2_3 : AutoMigrationSpec
}
