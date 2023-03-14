package proton.android.pass.featureitemcreate.impl.bottomsheets.generatepassword

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun GeneratePasswordBottomSheet(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    val viewModel = hiltViewModel<GeneratePasswordViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    GeneratePasswordBottomSheetContent(
        modifier = modifier,
        state = state,
        onLengthChange = { viewModel.onLengthChange(it) },
        onRegenerateClick = { viewModel.regenerate() },
        onHasSpecialCharactersChange = { viewModel.onHasSpecialCharactersChange(it) },
        onConfirm = {
            viewModel.onConfirm()
            onDismiss()
        }
    )
}
