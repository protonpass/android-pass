package proton.android.pass.featureitemcreate.impl.login.bottomsheet.password

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun GeneratePasswordBottomSheet(
    modifier: Modifier = Modifier,
    regeneratePassword: Boolean,
    onPasswordRegenerated: () -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val viewModel = hiltViewModel<GeneratePasswordViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(regeneratePassword) {
        if (regeneratePassword) {
            viewModel.regenerate()
            onPasswordRegenerated()
        }
    }

    GeneratePasswordBottomSheetContent(
        modifier = modifier,
        state = state,
        onLengthChange = { viewModel.onLengthChange(it) },
        onRegenerateClick = { viewModel.regenerate() },
        onHasSpecialCharactersChange = { viewModel.onHasSpecialCharactersChange(it) },
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}
