package proton.android.pass.data.impl.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import me.proton.core.data.room.db.BaseDao
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.entities.ShareEntity

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
        SELECT * FROM ${ShareEntity.TABLE} 
        WHERE ${ShareEntity.Columns.USER_ID} = :userId
        """
    )
    abstract fun getAllForUser(userId: String): List<ShareEntity>

    @Query(
        """
        DELETE FROM ${ShareEntity.TABLE} 
        WHERE ${ShareEntity.Columns.ID} IN (:shareIdList)
        """
    )
    abstract suspend fun delete(shareIdList: Array<String>): Int

    @Query(
        """
        SELECT COUNT(*) 
        FROM ${ShareEntity.TABLE}
        WHERE ${ShareEntity.Columns.USER_ID} = :userId
        """
    )
    abstract suspend fun countShares(userId: String): Int

    @Transaction
    open suspend fun evictAndUpsertShares(userId: UserId, vararg entities: ShareEntity) {
        val insertOrUpdateShares = entities.asList().map { it.id }
        val toDelete = getAllForUser(userId.id)
            .filterNot { insertOrUpdateShares.contains(it.id) }
        delete(*toDelete.toTypedArray())
        update(*entities)
        insertOrIgnore(*entities)
    }
}
