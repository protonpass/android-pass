package me.proton.pass.data.db.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.data.room.db.BaseDao
import me.proton.pass.data.db.entities.ShareEntity

@Dao
abstract class SharesDao : BaseDao<ShareEntity>() {
    @Query(
        """
        SELECT * FROM ${ShareEntity.TABLE} 
        WHERE ${ShareEntity.Columns.ADDRESS_ID} = :addressId
        """
    )
    abstract fun observeAllForAddress(addressId: String): Flow<List<ShareEntity>>

    @Query(
        """
        SELECT * FROM ${ShareEntity.TABLE}
        WHERE ${ShareEntity.Columns.USER_ID} = :userId
          AND ${ShareEntity.Columns.ID} = :shareId
        LIMIT 1
        """
    )
    abstract suspend fun getById(userId: String, shareId: String): ShareEntity?

    @Query(
        """
        SELECT * FROM ${ShareEntity.TABLE} 
        WHERE ${ShareEntity.Columns.USER_ID} = :userId
        """
    )
    abstract fun observeAllForUser(userId: String): Flow<List<ShareEntity>>

    @Query(
        """
        DELETE FROM ${ShareEntity.TABLE} 
        WHERE ${ShareEntity.Columns.ID} = :shareId
        """
    )
    abstract suspend fun delete(shareId: String): Int

    @Query(
        """
        SELECT COUNT(*) 
        FROM ${ShareEntity.TABLE}
        WHERE ${ShareEntity.Columns.USER_ID} = :userId
        """
    )
    abstract suspend fun countShares(userId: String): Int
}
