package proton.android.pass.data.impl.fakes

import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.dao.ItemsDao
import proton.android.pass.data.impl.db.dao.PassEventsDao
import proton.android.pass.data.impl.db.dao.PlanLimitsDao
import proton.android.pass.data.impl.db.dao.SearchEntryDao
import proton.android.pass.data.impl.db.dao.ShareKeysDao
import proton.android.pass.data.impl.db.dao.SharesDao
import proton.android.pass.data.impl.db.dao.TelemetryDao

class TestPassDatabase : PassDatabase {
    override fun sharesDao(): SharesDao {
        throw IllegalStateException("This method should not be called")
    }

    override fun itemsDao(): ItemsDao {
        throw IllegalStateException("This method should not be called")
    }

    override fun shareKeysDao(): ShareKeysDao {
        throw IllegalStateException("This method should not be called")
    }

    override fun passEventsDao(): PassEventsDao {
        throw IllegalStateException("This method should not be called")
    }

    override fun telemetryEventsDao(): TelemetryDao {
        throw IllegalStateException("This method should not be called")
    }

    override fun searchEntryDao(): SearchEntryDao {
        throw IllegalStateException("This method should not be called")
    }

    override fun planLimitsDao(): PlanLimitsDao {
        throw IllegalStateException("This method should not be called")
    }

    override suspend fun <R> inTransaction(block: suspend () -> R): R = block()
}
