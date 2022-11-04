package me.proton.pass.data.db

import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.data.room.db.Database
import me.proton.core.data.room.db.migration.DatabaseMigration
import me.proton.pass.data.db.dao.ItemKeysDao
import me.proton.pass.data.db.dao.ItemsDao
import me.proton.pass.data.db.dao.SharesDao
import me.proton.pass.data.db.dao.VaultKeysDao
import me.proton.pass.data.db.entities.ExternalColumns
import me.proton.pass.data.db.entities.ItemEntity
import me.proton.pass.data.db.entities.ItemKeyEntity
import me.proton.pass.data.db.entities.ShareEntity
import me.proton.pass.data.db.entities.VaultKeyEntity

interface PassDatabase : Database {

    fun sharesDao(): SharesDao
    fun itemsDao(): ItemsDao
    fun vaultKeysDao(): VaultKeysDao
    fun itemKeysDao(): ItemKeysDao

    companion object {
        val MIGRATION_0 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                listOf(
                    ShareEntity.TABLE,
                    ItemEntity.TABLE,
                    VaultKeyEntity.TABLE,
                    ItemKeyEntity.TABLE
                ).forEach {
                    "CREATE INDEX IF NOT EXISTS `${it}_user_id_index` ON `$it` (`${ExternalColumns.ADDRESS_ID}`)"
                }
            }
        }
    }
}
