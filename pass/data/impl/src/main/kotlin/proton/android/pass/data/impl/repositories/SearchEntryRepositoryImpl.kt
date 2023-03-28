package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.data.api.SearchEntry
import proton.android.pass.data.api.repositories.SearchEntryRepository
import proton.android.pass.data.impl.db.entities.SearchEntryEntity
import proton.android.pass.data.impl.local.LocalSearchEntryDataSource
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

class SearchEntryRepositoryImpl @Inject constructor(
    private val localDataSource: LocalSearchEntryDataSource,
    private val clock: Clock
) : SearchEntryRepository {

    override suspend fun store(userId: UserId, shareId: ShareId, itemId: ItemId) {
        val searchEntryEntity = SearchEntryEntity(
            itemId = itemId.id,
            shareId = shareId.id,
            userId = userId.id,
            createTime = clock.now().epochSeconds
        )
        localDataSource.store(searchEntryEntity)
    }

    override suspend fun deleteAll(userId: UserId) {
        localDataSource.deleteAll(userId)
    }

    override suspend fun deleteAllByShare(shareId: ShareId) {
        localDataSource.deleteAllByShare(shareId)
    }

    override suspend fun deleteEntry(shareId: ShareId, itemId: ItemId) {
        localDataSource.deleteEntry(shareId, itemId)
    }

    override fun observeAll(userId: UserId): Flow<List<SearchEntry>> =
        localDataSource.observeAll(userId)
            .map { list ->
                list.map { it.toSearchEntry() }
            }

    override fun observeAllByShare(shareId: ShareId): Flow<List<SearchEntry>> =
        localDataSource.observeAllByShare(shareId)
            .map { list ->
                list.map { it.toSearchEntry() }
            }

    private fun SearchEntryEntity.toSearchEntry() = SearchEntry(
        itemId = ItemId(itemId),
        shareId = ShareId(shareId),
        userId = UserId(userId),
        createTime = createTime
    )
}
