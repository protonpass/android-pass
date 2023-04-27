package proton.android.pass.featuresearchoptions.api

import kotlinx.coroutines.flow.Flow

interface SearchOptionsRepository {
    fun observeSearchOptions(): Flow<SearchOptions>
    fun observeSortingOption(): Flow<SortingOption>
    fun setSortingOption(sortingOption: SortingOption)
    fun clearSearchOptions()
}

data class SearchOptions(
    val sortingOption: SortingOption
) {
    companion object {
        val Initial = SearchOptions(
            sortingOption = SortingOption(SearchSortingType.MostRecent)
        )
    }
}


data class SortingOption(val searchSortingType: SearchSortingType)

