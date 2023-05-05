package proton.android.pass.autofill.ui.autofill.select

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.ui.autofill.navigation.SelectItemNavigation

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun SelectItemScreen(
    modifier: Modifier = Modifier,
    autofillAppState: AutofillAppState,
    onNavigate: (SelectItemNavigation) -> Unit,
    viewModel: SelectItemViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.setInitialState(autofillAppState)
    }

    OnItemSelectLaunchEffect(uiState.listUiState.itemClickedEvent, onNavigate)
    SelectItemScreenContent(
        modifier = modifier,
        uiState = uiState,
        packageInfo = autofillAppState.packageInfoUi,
        webDomain = autofillAppState.webDomain.value(),
        onItemClicked = { item, shouldAssociate ->
            viewModel.onItemClicked(item, autofillAppState, shouldAssociate)
        },
        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
        onEnterSearch = { viewModel.onEnterSearch() },
        onStopSearching = { viewModel.onStopSearching() },
        onScrollToTop = { viewModel.onScrollToTop() },
        onNavigate = onNavigate
    )
}

@Composable
private fun OnItemSelectLaunchEffect(
    event: AutofillItemClickedEvent,
    onNavigate: (SelectItemNavigation) -> Unit
) {
    if (event is AutofillItemClickedEvent.Clicked) {
        LaunchedEffect(Unit) {
            onNavigate(SelectItemNavigation.ItemSelected(event.autofillMappings))
        }
    }
}
