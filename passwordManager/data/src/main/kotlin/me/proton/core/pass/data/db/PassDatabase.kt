package me.proton.core.pass.data.db

import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.core.data.room.db.Database
import me.proton.core.data.room.db.migration.DatabaseMigration
import me.proton.core.pass.data.db.dao.ItemKeysDao
import me.proton.core.pass.data.db.dao.ItemsDao
import me.proton.core.pass.data.db.dao.SecretsDao
import me.proton.core.pass.data.db.dao.SharesDao
import me.proton.core.pass.data.db.dao.VaultKeysDao
import me.proton.core.pass.data.db.entities.ExternalColumns
import me.proton.core.pass.data.db.entities.ItemEntity
import me.proton.core.pass.data.db.entities.ItemKeyEntity
import me.proton.core.pass.data.db.entities.SecretEntity
import me.proton.core.pass.data.db.entities.ShareEntity
import me.proton.core.pass.data.db.entities.VaultKeyEntity

interface PassDatabase : Database {

    fun secretsDao(): SecretsDao
    fun sharesDao(): SharesDao
    fun itemsDao(): ItemsDao
    fun vaultKeysDao(): VaultKeysDao
    fun itemKeysDao(): ItemKeysDao

    companion object {
        val MIGRATION_0 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                listOf(
                    SecretEntity.TABLE,
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
