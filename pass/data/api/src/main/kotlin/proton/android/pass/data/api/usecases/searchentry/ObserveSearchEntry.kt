package proton.android.pass.data.api.usecases.searchentry

import kotlinx.coroutines.flow.Flow
import proton.android.pass.data.api.SearchEntry
import proton.pass.domain.ShareId

interface ObserveSearchEntry {
    operator fun invoke(searchEntrySelection: SearchEntrySelection): Flow<List<SearchEntry>>

    sealed class SearchEntrySelection {
        object AllVaults : SearchEntrySelection()
        data class Vault(val shareId: ShareId) : SearchEntrySelection()
    }
}

