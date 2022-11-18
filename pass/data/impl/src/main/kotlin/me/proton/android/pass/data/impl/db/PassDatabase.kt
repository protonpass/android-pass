package me.proton.android.pass.data.impl.db

import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.android.pass.data.impl.db.dao.ItemKeysDao
import me.proton.android.pass.data.impl.db.dao.ItemsDao
import me.proton.android.pass.data.impl.db.dao.SharesDao
import me.proton.android.pass.data.impl.db.dao.VaultKeysDao
import me.proton.android.pass.data.impl.db.entities.ExternalColumns
import me.proton.android.pass.data.impl.db.entities.ItemEntity
import me.proton.android.pass.data.impl.db.entities.ItemKeyEntity
import me.proton.android.pass.data.impl.db.entities.ShareEntity
import me.proton.android.pass.data.impl.db.entities.VaultKeyEntity
import me.proton.core.data.room.db.Database
import me.proton.core.data.room.db.migration.DatabaseMigration

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
