package me.proton.android.pass.data.impl.db.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.android.pass.data.impl.db.entities.ItemEntity
import me.proton.core.data.room.db.BaseDao
import me.proton.pass.domain.ItemStateValues

data class SummaryRow(
    val itemKind: Int,
    val itemCount: Long
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
          AND ${ItemEntity.Columns.SHARE_ID} = :shareId
          AND ${ItemEntity.Columns.STATE} = :itemState
        ORDER BY ${ItemEntity.Columns.CREATE_TIME} DESC
        """
    )
    abstract fun observerAllForShare(userId: String, shareId: String, itemState: Int): Flow<List<ItemEntity>>

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
          AND ${ItemEntity.Columns.SHARE_ID} = :shareId
          AND ${ItemEntity.Columns.STATE} = ${ItemStateValues.ACTIVE}
        GROUP BY ${ItemEntity.Columns.ITEM_TYPE}
        """
    )
    abstract fun itemSummary(userId: String, shareId: String): Flow<List<SummaryRow>>

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
