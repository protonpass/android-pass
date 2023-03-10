package proton.android.pass.data.impl.db

import me.proton.core.data.room.db.Database
import proton.android.pass.data.impl.db.dao.ItemsDao
import proton.android.pass.data.impl.db.dao.PassEventsDao
import proton.android.pass.data.impl.db.dao.ShareKeysDao
import proton.android.pass.data.impl.db.dao.SharesDao

interface PassDatabase : Database {

    fun sharesDao(): SharesDao
    fun itemsDao(): ItemsDao
    fun shareKeysDao(): ShareKeysDao
    fun passEventsDao(): PassEventsDao

}
