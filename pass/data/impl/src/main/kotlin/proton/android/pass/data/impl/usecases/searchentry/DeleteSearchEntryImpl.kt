package proton.android.pass.data.impl.usecases.searchentry

import proton.android.pass.data.api.repositories.SearchEntryRepository
import proton.android.pass.data.api.usecases.searchentry.DeleteSearchEntry
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

class DeleteSearchEntryImpl @Inject constructor(
    private val searchEntryRepository: SearchEntryRepository
) : DeleteSearchEntry {

    override suspend fun invoke(shareId: ShareId, itemId: ItemId) {
        searchEntryRepository.deleteEntry(shareId, itemId)
    }
}
