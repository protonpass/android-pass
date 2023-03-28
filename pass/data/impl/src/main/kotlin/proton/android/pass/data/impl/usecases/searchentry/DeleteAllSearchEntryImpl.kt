package proton.android.pass.data.impl.usecases.searchentry

import kotlinx.coroutines.flow.first
import proton.android.pass.data.api.repositories.SearchEntryRepository
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.searchentry.DeleteAllSearchEntry
import javax.inject.Inject

class DeleteAllSearchEntryImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val searchEntryRepository: SearchEntryRepository
) : DeleteAllSearchEntry {

    override suspend fun invoke() {
        val user = requireNotNull(observeCurrentUser().first())
        searchEntryRepository.deleteAll(user.userId)
    }
}
