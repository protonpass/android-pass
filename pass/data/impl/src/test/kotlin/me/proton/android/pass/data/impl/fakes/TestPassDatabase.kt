package me.proton.android.pass.data.impl.fakes

import me.proton.android.pass.data.impl.db.PassDatabase
import me.proton.android.pass.data.impl.db.dao.ItemKeysDao
import me.proton.android.pass.data.impl.db.dao.ItemsDao
import me.proton.android.pass.data.impl.db.dao.SharesDao
import me.proton.android.pass.data.impl.db.dao.VaultKeysDao

class TestPassDatabase : PassDatabase {
    override fun sharesDao(): SharesDao {
        throw IllegalStateException("This method should not be called")
    }

    override fun itemsDao(): ItemsDao {
        throw IllegalStateException("This method should not be called")
    }

    override fun vaultKeysDao(): VaultKeysDao {
        throw IllegalStateException("This method should not be called")
    }

    override fun itemKeysDao(): ItemKeysDao {
        throw IllegalStateException("This method should not be called")
    }

    override suspend fun <R> inTransaction(block: suspend () -> R): R = block()
}
