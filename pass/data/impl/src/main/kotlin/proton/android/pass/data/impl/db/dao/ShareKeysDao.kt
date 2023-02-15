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
        WHERE ${ShareKeyEntity.Columns.SHARE_ID} = :shareId
        ORDER BY ${ShareKeyEntity.Columns.ROTATION} DESC
        LIMIT 1
        """
    )
    abstract fun getLatestKeyForShare(shareId: String): Flow<ShareKeyEntity>

    @Query(
        """
        SELECT * FROM ${ShareKeyEntity.TABLE}
        WHERE ${ShareKeyEntity.Columns.USER_ID} = :userId
          AND ${ShareKeyEntity.Columns.SHARE_ID} = :shareId
          AND ${ShareKeyEntity.Columns.ROTATION} = :rotation
        LIMIT 1
        """
    )
    abstract fun getByShareAndRotation(
        userId: String,
        shareId: String,
        rotation: Long
    ): Flow<ShareKeyEntity?>
}
