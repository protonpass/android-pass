package me.proton.android.pass.ui.home

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.proton.core.compose.component.ProtonModalBottomSheetLayout

@ExperimentalMaterialApi
@ExperimentalComposeUiApi
@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    homeScreenNavigation: HomeScreenNavigation,
    onDrawerIconClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.homeUiState.collectAsStateWithLifecycle()
    val bottomSheetState = rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val scope = rememberCoroutineScope()

    ProtonModalBottomSheetLayout(
        sheetState = bottomSheetState,
        sheetContent = {
            BottomSheetContents(
                state = bottomSheetState,
                navigation = homeScreenNavigation,
                shareId = uiState.homeListUiState.selectedShare
            )
        }
    ) {
        HomeContent(
            modifier = modifier,
            uiState = uiState,
            homeScreenNavigation = homeScreenNavigation,
            onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
            onEnterSearch = { viewModel.onEnterSearch() },
            onStopSearching = { viewModel.onStopSearching() },
            sendItemToTrash = { viewModel.sendItemToTrash(it) },
            onDrawerIconClick = onDrawerIconClick,
            onAddItemClick = { scope.launch { bottomSheetState.show() } },
            onRefresh = { viewModel.onRefresh() }
        )
    }
}
