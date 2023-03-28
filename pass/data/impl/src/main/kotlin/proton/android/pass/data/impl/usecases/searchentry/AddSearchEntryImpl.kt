package proton.android.pass.data.impl.usecases.searchentry

import kotlinx.coroutines.flow.first
import proton.android.pass.data.api.repositories.SearchEntryRepository
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.searchentry.AddSearchEntry
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

class AddSearchEntryImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val searchEntryRepository: SearchEntryRepository
) : AddSearchEntry {

    override suspend fun invoke(shareId: ShareId, itemId: ItemId) {
        val user = requireNotNull(observeCurrentUser().first())
        searchEntryRepository.store(user.userId, shareId, itemId)
    }
}
