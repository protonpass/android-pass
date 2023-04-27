package proton.android.featuresearchoptions.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.featuresearchoptions.api.SearchOptionsRepository
import proton.android.pass.featuresearchoptions.api.SearchSortingType
import proton.android.pass.featuresearchoptions.api.SortingOption
import proton.android.pass.navigation.api.SortingTypeNavArgId
import javax.inject.Inject

@HiltViewModel
class SortingBottomSheetViewModel @Inject constructor(
    private val searchOptionsRepository: SearchOptionsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val sortingType: SearchSortingType =
        SearchSortingType.valueOf(requireNotNull(savedStateHandle.get<String>(SortingTypeNavArgId.key)))
    val state: StateFlow<SearchSortingType> = MutableStateFlow(sortingType)
        .stateIn(viewModelScope, SharingStarted.Eagerly, sortingType)

    fun onSortingTypeChanged(searchSortingType: SearchSortingType) {
        searchOptionsRepository.setSortingOption(SortingOption(searchSortingType))
    }
}
