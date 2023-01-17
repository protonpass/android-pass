package proton.android.pass.data.impl.db.dao

import androidx.room.Dao
import me.proton.core.data.room.db.BaseDao
import proton.android.pass.data.impl.db.entities.SelectedShareEntity

@Suppress("UnnecessaryAbstractClass")
@Dao
abstract class SelectedShareDao : BaseDao<SelectedShareEntity>()
