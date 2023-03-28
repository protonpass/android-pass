package proton.android.pass.data.impl.usecases.searchentry

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import proton.android.pass.data.api.SearchEntry
import proton.android.pass.data.api.repositories.SearchEntryRepository
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.searchentry.ObserveSearchEntry
import javax.inject.Inject

class ObserveSearchEntryImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val searchEntryRepository: SearchEntryRepository
) : ObserveSearchEntry {

    override fun invoke(
        searchEntrySelection: ObserveSearchEntry.SearchEntrySelection
    ): Flow<List<SearchEntry>> =
        when (searchEntrySelection) {
            ObserveSearchEntry.SearchEntrySelection.AllVaults -> observeCurrentUser()
                .flatMapLatest { searchEntryRepository.observeAll(it.userId) }
            is ObserveSearchEntry.SearchEntrySelection.Vault ->
                searchEntryRepository.observeAllByShare(searchEntrySelection.shareId)
        }
}
