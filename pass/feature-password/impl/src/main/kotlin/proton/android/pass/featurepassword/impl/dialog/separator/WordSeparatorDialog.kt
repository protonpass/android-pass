package proton.android.pass.featurepassword.impl.dialog.separator

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.featurepassword.impl.GeneratePasswordNavigation

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun WordSeparatorDialog(
    modifier: Modifier = Modifier,
    onNavigate: (GeneratePasswordNavigation) -> Unit,
    viewModel: WordSeparatorViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        if (state.event == WordSeparatorUiEvent.Close) {
            onNavigate(GeneratePasswordNavigation.CloseDialog)
        }
    }

    WordSeparatorDialogContent(
        modifier = modifier,
        state = state,
        onOptionSelected = viewModel::onChange,
        onConfirm = viewModel::onConfirm,
        onCancel = {
            onNavigate(GeneratePasswordNavigation.CloseDialog)
        },
        onDismiss = {
            onNavigate(GeneratePasswordNavigation.CloseDialog)
        }
    )
}

