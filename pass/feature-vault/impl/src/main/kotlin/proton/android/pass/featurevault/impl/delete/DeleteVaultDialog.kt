package proton.android.pass.featurevault.impl.delete

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun DeleteVaultDialog(
    modifier: Modifier = Modifier,
    onClose: () -> Unit,
    viewModel: DeleteVaultViewModel = hiltViewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.onStart()
    }

    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.event) {
        if (state.event == DeleteVaultEvent.Deleted) {
            onClose()
        }
    }

    DeleteVaultDialogContent(
        modifier = modifier,
        state = state,
        onVaultTextChange = { viewModel.onTextChange(it) },
        onDelete = { viewModel.onDelete() },
        onCancel = onClose,
        onDismiss = onClose
    )
}
