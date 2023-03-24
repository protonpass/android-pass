package proton.android.pass.data.impl.db.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import me.proton.core.data.room.db.BaseDao
import proton.android.pass.data.impl.db.entities.SearchEntryEntity

@Dao
abstract class SearchEntryDao : BaseDao<SearchEntryEntity>() {

    @Query(
        """
        DELETE FROM ${SearchEntryEntity.TABLE} 
        WHERE ${SearchEntryEntity.Columns.USER_ID} = :userId
      """
    )
    abstract suspend fun deleteAll(userId: String)

    @Query(
        """
        DELETE FROM ${SearchEntryEntity.TABLE} 
        WHERE ${SearchEntryEntity.Columns.SHARE_ID} = :shareId
      """
    )
    abstract suspend fun deleteAllByShare(shareId: String)

    @Query(
        """
        DELETE FROM ${SearchEntryEntity.TABLE} 
        WHERE ${SearchEntryEntity.Columns.SHARE_ID} = :shareId
          AND ${SearchEntryEntity.Columns.ITEM_ID} = :itemId
      """
    )
    abstract suspend fun deleteEntry(shareId: String, itemId: String)

    @Query(
        """
        SELECT * FROM ${SearchEntryEntity.TABLE} 
        WHERE ${SearchEntryEntity.Columns.USER_ID} = :userId
        ORDER BY ${SearchEntryEntity.Columns.CREATE_TIME} ASC
        """
    )
    abstract fun observeAll(userId: String): Flow<List<SearchEntryEntity>>

    @Query(
        """
        SELECT * FROM ${SearchEntryEntity.TABLE} 
        WHERE ${SearchEntryEntity.Columns.SHARE_ID} = :shareId
        ORDER BY ${SearchEntryEntity.Columns.CREATE_TIME} ASC
        """
    )
    abstract fun observeAllByShare(shareId: String): Flow<List<SearchEntryEntity>>
}
