package proton.android.pass.autofill.ui.bottomsheet.itemoptions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun AutofillItemOptionsBottomSheet(
    modifier: Modifier = Modifier,
    onNavigate: (AutofillItemOptionsNavigation) -> Unit,
    viewModel: AutofillItemOptionsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        if (state.event == AutofillItemOptionsEvent.Close) {
            onNavigate(AutofillItemOptionsNavigation.Close)
        }
    }

    AutofillItemOptionsBottomSheetContent(
        modifier = modifier,
        isLoading = state.isLoading.value(),
        onTrash = {
            viewModel.onTrash()
        }
    )
}
