package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.api.repositories.ItemSyncStatus
import proton.android.pass.data.api.repositories.ItemSyncStatusRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestItemSyncStatusRepository @Inject constructor() : ItemSyncStatusRepository {

    private val flow = testFlow<ItemSyncStatus>()

    override fun emit(status: ItemSyncStatus) {
        flow.tryEmit(status)
    }

    override fun observeSyncStatus(): Flow<ItemSyncStatus> = flow
}
