package me.proton.core.pass.data.db

import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.data.room.db.Database
import me.proton.core.data.room.db.migration.DatabaseMigration
import me.proton.core.pass.data.db.entities.ExternalColumns
import me.proton.core.pass.data.db.entities.SecretEntity

interface PassDatabase: Database {

    companion object {
        val MIGRATION_1 = object: DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                listOf(SecretEntity.TABLE).forEach {
                    "CREATE INDEX IF NOT EXISTS `${it}_user_id_index` ON `${it}` (`${ExternalColumns.ADDRESS_ID}`)"
                }
            }
        }
    }
}