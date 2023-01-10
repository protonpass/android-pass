package proton.android.pass.data.impl.db

import androidx.sqlite.db.SupportSQLiteDatabase
import proton.android.pass.data.impl.db.dao.ItemKeysDao
import proton.android.pass.data.impl.db.dao.ItemsDao
import proton.android.pass.data.impl.db.dao.PassEventsDao
import proton.android.pass.data.impl.db.dao.SharesDao
import proton.android.pass.data.impl.db.dao.VaultKeysDao
import me.proton.core.data.room.db.Database
import me.proton.core.data.room.db.migration.DatabaseMigration

interface PassDatabase : Database {

    fun sharesDao(): SharesDao
    fun itemsDao(): ItemsDao
    fun vaultKeysDao(): VaultKeysDao
    fun itemKeysDao(): ItemKeysDao
    fun passEventsDao(): PassEventsDao

    companion object {
        val MIGRATION_4 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE ItemEntity ADD COLUMN item_type INTEGER NOT NULL DEFAULT '-1'")
            }
        }
    }
}
