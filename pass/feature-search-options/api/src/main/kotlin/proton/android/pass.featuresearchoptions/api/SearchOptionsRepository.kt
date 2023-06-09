package proton.android.pass.featuresearchoptions.api

import kotlinx.coroutines.flow.Flow
import proton.pass.domain.ShareId

interface SearchOptionsRepository {
    fun observeSearchOptions(): Flow<SearchOptions>
    fun observeSortingOption(): Flow<SortingOption>
    fun observeVaultSelectionOption(): Flow<VaultSelectionOption>
    fun setSortingOption(sortingOption: SortingOption)
    fun setVaultSelectionOption(vaultSelectionOption: VaultSelectionOption)
    fun clearSearchOptions()
}

data class SearchOptions(
    val sortingOption: SortingOption,
    val vaultSelectionOption: VaultSelectionOption
) {
    companion object {
        val Initial = SearchOptions(
            sortingOption = SortingOption(SearchSortingType.MostRecent),
            vaultSelectionOption = VaultSelectionOption.AllVaults
        )
    }
}


data class SortingOption(val searchSortingType: SearchSortingType)

sealed class VaultSelectionOption {
    object AllVaults : VaultSelectionOption()
    object Trash : VaultSelectionOption()
    data class Vault(val shareId: ShareId) : VaultSelectionOption()
}
