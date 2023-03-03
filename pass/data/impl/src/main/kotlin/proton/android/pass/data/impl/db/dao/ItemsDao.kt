package proton.android.pass.data.impl.db.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.data.room.db.BaseDao
import proton.android.pass.data.impl.db.entities.ItemEntity
import proton.pass.domain.ItemStateValues

data class SummaryRow(
    val itemKind: Int,
    val itemCount: Long
)

data class ShareItemCountRow(
    val shareId: String,
    val activeItemCount: Long,
    val trashedItemCount: Long
)

@Dao
abstract class ItemsDao : BaseDao<ItemEntity>() {
    @Query(
        """
        SELECT * FROM ${ItemEntity.TABLE} 
        WHERE ${ItemEntity.Columns.USER_ID} = :userId
          AND ${ItemEntity.Columns.STATE} = :itemState
        ORDER BY ${ItemEntity.Columns.CREATE_TIME} DESC
        """
    )
    abstract fun observeAllForAddress(userId: String, itemState: Int): Flow<List<ItemEntity>>

    @Query(
        """
        SELECT * FROM ${ItemEntity.TABLE} 
        WHERE ${ItemEntity.Columns.USER_ID} = :userId
          AND ${ItemEntity.Columns.STATE} = :itemState
          AND ${ItemEntity.Columns.ITEM_TYPE} = :itemType
        ORDER BY ${ItemEntity.Columns.CREATE_TIME} DESC
        """
    )
    abstract fun observeAllForAddress(
        userId: String,
        itemState: Int,
        itemType: Int
    ): Flow<List<ItemEntity>>

    @Query(
        """
        SELECT * FROM ${ItemEntity.TABLE} 
        WHERE ${ItemEntity.Columns.USER_ID} = :userId
          AND ${ItemEntity.Columns.SHARE_ID} = :shareId
          AND ${ItemEntity.Columns.STATE} = :itemState
        ORDER BY ${ItemEntity.Columns.CREATE_TIME} DESC
        """
    )
    abstract fun observerAllForShare(
        userId: String,
        shareId: String,
        itemState: Int
    ): Flow<List<ItemEntity>>

    @Query(
        """
        SELECT * FROM ${ItemEntity.TABLE} 
        WHERE ${ItemEntity.Columns.USER_ID} = :userId
          AND ${ItemEntity.Columns.SHARE_ID} = :shareId
          AND ${ItemEntity.Columns.STATE} = :itemState
          AND ${ItemEntity.Columns.ITEM_TYPE} = :itemType
        ORDER BY ${ItemEntity.Columns.CREATE_TIME} DESC
        """
    )
    abstract fun observeAllForShare(
        userId: String,
        shareId: String,
        itemState: Int,
        itemType: Int
    ): Flow<List<ItemEntity>>

    @Query(
        """
        SELECT * FROM ${ItemEntity.TABLE}
        WHERE ${ItemEntity.Columns.SHARE_ID} = :shareId
          AND ${ItemEntity.Columns.ID} = :itemId
        """
    )
    abstract suspend fun getById(shareId: String, itemId: String): ItemEntity?

    @Query(
        """
        SELECT * FROM ${ItemEntity.TABLE}
        WHERE ${ItemEntity.Columns.USER_ID} = :userId
          AND ${ItemEntity.Columns.STATE} = :state
        ORDER BY ${ItemEntity.Columns.CREATE_TIME} DESC
        """
    )
    abstract suspend fun getItemsWithState(userId: String, state: Int): List<ItemEntity>

    @Query(
        """
        UPDATE ${ItemEntity.TABLE}
        SET ${ItemEntity.Columns.STATE} = :state
        WHERE ${ItemEntity.Columns.SHARE_ID} = :shareId
          AND ${ItemEntity.Columns.ID} = :itemId
        """
    )
    abstract suspend fun setItemState(shareId: String, itemId: String, state: Int)

    @Query(
        """
        DELETE FROM ${ItemEntity.TABLE} 
        WHERE ${ItemEntity.Columns.SHARE_ID} = :shareId
          AND ${ItemEntity.Columns.ID} = :itemId
        """
    )
    abstract suspend fun delete(shareId: String, itemId: String): Int

    @Query(
        """
        SELECT COUNT(*)
        FROM ${ItemEntity.TABLE}
        WHERE ${ItemEntity.Columns.USER_ID} = :userId
          AND ${ItemEntity.Columns.SHARE_ID} = :shareId
        """
    )
    abstract suspend fun countItems(userId: String, shareId: String): Int

    @Query(
        """
        SELECT 
            ${ItemEntity.Columns.ITEM_TYPE} as itemKind,
            COUNT(${ItemEntity.Columns.ITEM_TYPE}) as itemCount
        FROM ${ItemEntity.TABLE}
        WHERE ${ItemEntity.Columns.USER_ID} = :userId
          AND ${ItemEntity.Columns.SHARE_ID} IN (:shareIds)
          AND ${ItemEntity.Columns.STATE} = ${ItemStateValues.ACTIVE}
        GROUP BY ${ItemEntity.Columns.ITEM_TYPE}
        """
    )
    abstract fun itemSummary(userId: String, shareIds: List<String>): Flow<List<SummaryRow>>

    @Query(
        """
        SELECT
            ${ItemEntity.Columns.SHARE_ID} as shareId,
            (
                SELECT COUNT(*)
                FROM ${ItemEntity.TABLE}
                WHERE ${ItemEntity.Columns.SHARE_ID} = o.${ItemEntity.Columns.SHARE_ID}
                  AND ${ItemEntity.Columns.STATE} = ${ItemStateValues.ACTIVE}
            ) as activeItemCount,
            (
                SELECT COUNT(*)
                FROM ${ItemEntity.TABLE}
                WHERE ${ItemEntity.Columns.SHARE_ID} = o.${ItemEntity.Columns.SHARE_ID}
                  AND ${ItemEntity.Columns.STATE} = ${ItemStateValues.TRASHED}
            ) as trashedItemCount
        FROM ${ItemEntity.TABLE} o
        WHERE o.${ItemEntity.Columns.SHARE_ID} IN (:shareIds)
        GROUP BY o.${ItemEntity.Columns.SHARE_ID}
        """
    )
    abstract fun countItemsForShares(shareIds: List<String>): Flow<List<ShareItemCountRow>>

    @Query(
        """
        UPDATE ${ItemEntity.TABLE}
        SET ${ItemEntity.Columns.LAST_USED_TIME} = :now
        WHERE ${ItemEntity.Columns.ID} = :itemId
          AND ${ItemEntity.Columns.SHARE_ID} = :shareId
        """
    )
    abstract fun updateLastUsedTime(shareId: String, itemId: String, now: Long)
}
