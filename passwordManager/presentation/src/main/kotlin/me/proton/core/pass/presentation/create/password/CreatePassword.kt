package me.proton.core.pass.presentation.create.password

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.android.pass.ui.shared.ArrowBackIcon
import me.proton.android.pass.ui.shared.TopBarTitleView
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.pass.presentation.R

@ExperimentalComposeUiApi
@Composable
fun CreatePassword(
    onUpClick: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val viewModel: CreatePasswordViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            ProtonTopAppBar(
                title = { TopBarTitleView(R.string.title_create_password) },
                navigationIcon = { ArrowBackIcon(onUpClick = onUpClick) },
                actions = {}
            )
        }
    ) { padding ->

        Box(modifier = Modifier.padding(padding)) {
            CreatePasswordViewContent(
                state = state,
                onConfirm = onConfirm,
                onLengthChange = { viewModel.onLengthChange(it) },
                onRegenerateClick = { viewModel.regenerate() },
                onSpecialCharactersChange = { viewModel.onHasSpecialCharactersChange(it) }
            )
        }
    }
}

