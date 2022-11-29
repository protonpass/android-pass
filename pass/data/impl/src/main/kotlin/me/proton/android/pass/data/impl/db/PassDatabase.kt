package me.proton.android.pass.data.impl.db

import androidx.sqlite.db.SupportSQLiteDatabase
import me.proton.android.pass.data.impl.db.dao.ItemKeysDao
import me.proton.android.pass.data.impl.db.dao.ItemsDao
import me.proton.android.pass.data.impl.db.dao.SharesDao
import me.proton.android.pass.data.impl.db.dao.VaultKeysDao
import me.proton.core.data.room.db.Database
import me.proton.core.data.room.db.migration.DatabaseMigration

interface PassDatabase : Database {

    fun sharesDao(): SharesDao
    fun itemsDao(): ItemsDao
    fun vaultKeysDao(): VaultKeysDao
    fun itemKeysDao(): ItemKeysDao

    companion object {
        val MIGRATION_4 = object : DatabaseMigration {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE ItemEntity ADD COLUMN item_type INTEGER NOT NULL DEFAULT '-1'")
            }
        }
    }
}
