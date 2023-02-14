package proton.android.pass.data.impl.db

import me.proton.core.data.room.db.Database
import proton.android.pass.data.impl.db.dao.ItemKeysDao
import proton.android.pass.data.impl.db.dao.ItemsDao
import proton.android.pass.data.impl.db.dao.PassEventsDao
import proton.android.pass.data.impl.db.dao.SelectedShareDao
import proton.android.pass.data.impl.db.dao.ShareKeysDao
import proton.android.pass.data.impl.db.dao.ShareSelectedShareDao
import proton.android.pass.data.impl.db.dao.SharesDao

interface PassDatabase : Database {

    fun sharesDao(): SharesDao
    fun itemsDao(): ItemsDao
    fun shareKeysDao(): ShareKeysDao
    fun itemKeysDao(): ItemKeysDao
    fun passEventsDao(): PassEventsDao
    fun selectedShareDao(): SelectedShareDao
    fun shareSelectedShareDao(): ShareSelectedShareDao

}
