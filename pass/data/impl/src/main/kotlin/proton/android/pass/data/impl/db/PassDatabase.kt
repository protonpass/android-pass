package proton.android.pass.data.impl.db

import me.proton.core.data.room.db.Database
import proton.android.pass.data.impl.db.dao.ItemsDao
import proton.android.pass.data.impl.db.dao.PassDataMigrationDao
import proton.android.pass.data.impl.db.dao.PassEventsDao
import proton.android.pass.data.impl.db.dao.PlanDao
import proton.android.pass.data.impl.db.dao.SearchEntryDao
import proton.android.pass.data.impl.db.dao.ShareKeysDao
import proton.android.pass.data.impl.db.dao.SharesDao
import proton.android.pass.data.impl.db.dao.TelemetryDao

interface PassDatabase : Database {

    fun sharesDao(): SharesDao
    fun itemsDao(): ItemsDao
    fun shareKeysDao(): ShareKeysDao
    fun passEventsDao(): PassEventsDao
    fun telemetryEventsDao(): TelemetryDao
    fun searchEntryDao(): SearchEntryDao
    fun planDao(): PlanDao
    fun dataMigrationDao(): PassDataMigrationDao
}
