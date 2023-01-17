package proton.android.pass.data.impl.db.dao

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import proton.android.pass.data.impl.db.entities.SelectedShareEntity
import proton.android.pass.data.impl.db.entities.ShareEntity
import proton.android.pass.data.impl.db.entities.SelectedShareEntity.Companion.TABLE as SSETable
import proton.android.pass.data.impl.db.entities.ShareEntity.Companion.TABLE as SETable

@Suppress("UnnecessaryAbstractClass")
@Dao
abstract class ShareSelectedShareDao {

    @Query(
        """
        SELECT $SETable.* FROM $SETable, $SSETable
        WHERE $SETable.${ShareEntity.Columns.USER_ID} = :userId
          AND $SETable.${ShareEntity.Columns.ID} = $SSETable.${SelectedShareEntity.Columns.SHARE_ID}
        """
    )
    abstract fun observeSelectedForUser(userId: String): Flow<List<ShareEntity>>
}
