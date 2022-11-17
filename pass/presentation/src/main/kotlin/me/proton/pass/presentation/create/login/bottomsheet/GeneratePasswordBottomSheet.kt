package me.proton.pass.presentation.create.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.pass.presentation.create.password.CreatePasswordViewModel

@Composable
fun GeneratePasswordBottomSheet(
    modifier: Modifier = Modifier,
    regeneratePassword: Boolean,
    onPasswordRegenerated: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val viewModel = hiltViewModel<CreatePasswordViewModel>()
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
        onConfirm = onConfirm
    )
}
