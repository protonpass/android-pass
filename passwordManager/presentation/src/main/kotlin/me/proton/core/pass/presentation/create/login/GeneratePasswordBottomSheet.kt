package me.proton.core.pass.presentation.create.login

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.presentation.R
import me.proton.core.pass.presentation.components.common.bottomsheet.BottomSheetTitle
import me.proton.core.pass.presentation.components.common.bottomsheet.BottomSheetTitleButton
import me.proton.core.pass.presentation.create.password.CreatePasswordViewContent
import me.proton.core.pass.presentation.create.password.CreatePasswordViewModel

@Composable
fun GeneratePasswordBottomSheet(
    modifier: Modifier = Modifier,
    onConfirm: (String) -> Unit
) {
    val viewModel = hiltViewModel<CreatePasswordViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    Column(modifier = modifier) {
        BottomSheetTitle(
            title = R.string.button_generate_password,
            button = BottomSheetTitleButton(
                R.string.generate_password_confirm,
                onClick = { onConfirm(state.password) },
                enabled = true
            )
        )
        CreatePasswordViewContent(
            state = state,
            onLengthChange = { viewModel.onLengthChange(it) },
            onRegenerateClick = { viewModel.regenerate() },
            onSpecialCharactersChange = { viewModel.onHasSpecialCharactersChange(it) }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Preview_GeneratePasswordBottomSheet() {
    ProtonTheme {
        GeneratePasswordBottomSheet(onConfirm = {})
    }
}
