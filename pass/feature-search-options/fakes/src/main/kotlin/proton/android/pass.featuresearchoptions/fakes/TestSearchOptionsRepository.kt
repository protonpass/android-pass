package proton.android.pass.featuresearchoptions.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import proton.android.pass.featuresearchoptions.api.SearchOptions
import proton.android.pass.featuresearchoptions.api.SearchOptionsRepository
import proton.android.pass.featuresearchoptions.api.SortingOption
import proton.android.pass.featuresearchoptions.api.VaultSelectionOption
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestSearchOptionsRepository @Inject constructor() : SearchOptionsRepository {

    private val searchOptionsFlow = MutableStateFlow(SearchOptions.Initial)

    private val sortingOptionFlow = MutableStateFlow(SearchOptions.Initial.sortingOption)
    private val vaultSelectionOptionFlow: MutableStateFlow<VaultSelectionOption> =
        MutableStateFlow(VaultSelectionOption.AllVaults)

    override fun observeSearchOptions(): Flow<SearchOptions> = searchOptionsFlow

    override fun observeSortingOption(): Flow<SortingOption> = sortingOptionFlow

    override fun observeVaultSelectionOption(): Flow<VaultSelectionOption> =
        vaultSelectionOptionFlow

    override fun setSortingOption(sortingOption: SortingOption) {
        sortingOptionFlow.update { sortingOption }
    }

    override fun setVaultSelectionOption(vaultSelectionOption: VaultSelectionOption) {
        vaultSelectionOptionFlow.update { vaultSelectionOption }
    }

    override fun clearSearchOptions() {
    }
}
