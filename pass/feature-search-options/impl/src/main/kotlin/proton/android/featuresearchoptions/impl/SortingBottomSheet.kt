package proton.android.featuresearchoptions.impl

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterialApi::class, ExperimentalLifecycleComposeApi::class)
@Composable
fun SortingBottomSheet(
    modifier: Modifier = Modifier,
    onNavigateEvent: (SortingNavigation) -> Unit,
    viewModel: SortingBottomSheetViewModel = hiltViewModel()
) {
    val sortingType by viewModel.state.collectAsStateWithLifecycle()
    SortingBottomSheetContents(
        modifier = modifier,
        sortingType = sortingType,
        onSortingTypeSelected = {
            viewModel.onSortingTypeChanged(it)
            onNavigateEvent(SortingNavigation.SelectSorting)
        }
    )
}

sealed interface SortingNavigation {
    object SelectSorting : SortingNavigation
}

