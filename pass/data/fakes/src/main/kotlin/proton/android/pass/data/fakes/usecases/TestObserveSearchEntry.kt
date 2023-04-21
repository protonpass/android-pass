package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import proton.android.pass.common.api.FlowUtils.testFlow
import proton.android.pass.data.api.SearchEntry
import proton.android.pass.data.api.usecases.searchentry.ObserveSearchEntry
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestObserveSearchEntry @Inject constructor() : ObserveSearchEntry {

    private val flow = testFlow<List<SearchEntry>>()

    fun emit(value: List<SearchEntry>) {
        flow.tryEmit(value)
    }

    override fun invoke(
        searchEntrySelection: ObserveSearchEntry.SearchEntrySelection
    ): Flow<List<SearchEntry>> = flow
}
