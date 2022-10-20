package me.proton.core.pass.presentation.create.password

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@ExperimentalComposeUiApi
@Composable
fun CreatePassword(
    onUpClick: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val viewModel: CreatePasswordViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    CreatePasswordContent(
        state = state,
        onUpClick = onUpClick,
        onLengthChange = { viewModel.onLengthChange(it) },
        onRegenerateClick = { viewModel.regenerate() },
        onHasSpecialCharactersChange = { viewModel.onHasSpecialCharactersChange(it) },
        onConfirm = onConfirm
    )
}

