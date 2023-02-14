package proton.android.pass.data.impl.db.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.data.room.db.BaseDao
import proton.android.pass.data.impl.db.entities.ShareKeyEntity

@Dao
abstract class ShareKeysDao : BaseDao<ShareKeyEntity>() {
    @Query(
        """
        SELECT * FROM ${ShareKeyEntity.TABLE}
        WHERE ${ShareKeyEntity.Columns.USER_ID} = :userId
          AND ${ShareKeyEntity.Columns.SHARE_ID} = :shareId
        """
    )
    abstract fun getAllForShare(userId: String, shareId: String): Flow<List<ShareKeyEntity>>

    @Query(
        """
        SELECT * FROM ${ShareKeyEntity.TABLE}
        WHERE ${ShareKeyEntity.Columns.USER_ID} = :userId
          AND ${ShareKeyEntity.Columns.SHARE_ID} = :shareId
          AND ${ShareKeyEntity.Columns.ROTATION} = :rotation
        """
    )
    abstract fun getByShareAndRotation(
        userId: String,
        shareId: String,
        rotation: Long
    ): Flow<ShareKeyEntity?>
}
