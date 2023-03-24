package proton.android.pass.data.impl.local

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.impl.db.PassDatabase
import proton.android.pass.data.impl.db.entities.SearchEntryEntity
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

interface LocalSearchEntryDataSource {
    suspend fun store(entity: SearchEntryEntity)
    suspend fun deleteAll(userId: UserId)
    suspend fun deleteAllByShare(shareId: ShareId)
    suspend fun deleteEntry(shareId: ShareId, itemId: ItemId)
    fun observeAll(userId: UserId): Flow<List<SearchEntryEntity>>
    fun observeAllByShare(shareId: ShareId): Flow<List<SearchEntryEntity>>
}

class LocalSearchEntryDataSourceImpl @Inject constructor(
    private val db: PassDatabase
) : LocalSearchEntryDataSource {
    override suspend fun store(entity: SearchEntryEntity) {
        db.searchEntryDao().insertOrUpdate(entity)
    }

    override suspend fun deleteAll(userId: UserId) {
        db.searchEntryDao().deleteAll(userId.id)
    }

    override suspend fun deleteAllByShare(shareId: ShareId) {
        db.searchEntryDao().deleteAllByShare(shareId.id)
    }

    override suspend fun deleteEntry(shareId: ShareId, itemId: ItemId) {
        db.searchEntryDao().deleteEntry(shareId.id, itemId.id)
    }

    override fun observeAll(userId: UserId): Flow<List<SearchEntryEntity>> =
        db.searchEntryDao().observeAll(userId.id)

    override fun observeAllByShare(shareId: ShareId): Flow<List<SearchEntryEntity>> =
        db.searchEntryDao().observeAllByShare(shareId.id)
}
