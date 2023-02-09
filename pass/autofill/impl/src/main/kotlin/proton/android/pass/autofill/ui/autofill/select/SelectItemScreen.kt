package proton.android.pass.autofill.ui.autofill.select

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillMappings

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun SelectItemScreen(
    modifier: Modifier = Modifier,
    autofillAppState: AutofillAppState,
    onItemSelected: (AutofillMappings) -> Unit,
    onCreateLoginClicked: () -> Unit,
    onClose: () -> Unit,
    viewModel: SelectItemViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.setInitialState(autofillAppState)
    }

    OnItemSelectLaunchEffect(uiState.listUiState.itemClickedEvent, onItemSelected)
    SelectItemScreenContent(
        modifier = modifier,
        uiState = uiState,
        onItemClicked = { viewModel.onItemClicked(it, autofillAppState) },
        onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
        onEnterSearch = { viewModel.onEnterSearch() },
        onStopSearching = { viewModel.onStopSearching() },
        onCreateLoginClicked = onCreateLoginClicked,
        onClose = onClose
    )
}

@Composable
private fun OnItemSelectLaunchEffect(
    event: AutofillItemClickedEvent,
    onItemSelected: (AutofillMappings) -> Unit
) {
    if (event is AutofillItemClickedEvent.Clicked) {
        LaunchedEffect(Unit) { onItemSelected(event.autofillMappings) }
    }
}
