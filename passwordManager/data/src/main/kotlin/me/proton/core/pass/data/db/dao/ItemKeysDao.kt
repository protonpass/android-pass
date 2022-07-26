package me.proton.core.pass.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import me.proton.core.data.room.db.BaseDao
import me.proton.core.pass.data.db.entities.ItemKeyEntity

@Dao
abstract class ItemKeysDao : BaseDao<ItemKeyEntity>() {
    @Query(
        """
        SELECT * FROM ${ItemKeyEntity.TABLE}
        WHERE ${ItemKeyEntity.Columns.USER_ID} = :userId
          AND ${ItemKeyEntity.Columns.SHARE_ID} = :shareId
        """
    )
    abstract suspend fun getAllForShare(userId: String, shareId: String): List<ItemKeyEntity>

    @Query(
        """
        SELECT * FROM ${ItemKeyEntity.TABLE}
        WHERE ${ItemKeyEntity.Columns.USER_ID} = :userId
          AND ${ItemKeyEntity.Columns.SHARE_ID} = :shareId
          AND ${ItemKeyEntity.Columns.ID} = :keyId
        LIMIT 1
        """
    )
    abstract suspend fun getById(userId: String, shareId: String, keyId: String): ItemKeyEntity?
}
