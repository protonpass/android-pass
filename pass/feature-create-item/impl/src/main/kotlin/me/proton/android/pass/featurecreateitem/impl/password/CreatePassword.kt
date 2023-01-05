package me.proton.android.pass.featurecreateitem.impl.password

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun CreatePassword(
    modifier: Modifier = Modifier,
    onUpClick: () -> Unit
) {
    val viewModel: CreatePasswordViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    CreatePasswordContent(
        modifier = modifier,
        state = state,
        onUpClick = onUpClick,
        onLengthChange = { viewModel.onLengthChange(it) },
        onRegenerateClick = { viewModel.regenerate() },
        onHasSpecialCharactersChange = { viewModel.onHasSpecialCharactersChange(it) },
        onConfirm = { viewModel.onConfirm() }
    )
}

