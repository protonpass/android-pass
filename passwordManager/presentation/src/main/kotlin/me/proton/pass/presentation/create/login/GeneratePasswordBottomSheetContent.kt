package me.proton.pass.presentation.create.login

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetTitle
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetTitleButton
import me.proton.pass.presentation.components.previewproviders.CreatePasswordStatePreviewProvider
import me.proton.pass.presentation.create.password.CreatePasswordUiState
import me.proton.pass.presentation.create.password.CreatePasswordViewContent

@Composable
fun GeneratePasswordBottomSheetContent(
    modifier: Modifier = Modifier,
    state: CreatePasswordUiState,
    onLengthChange: (Int) -> Unit,
    onRegenerateClick: () -> Unit,
    onHasSpecialCharactersChange: (Boolean) -> Unit,
    onConfirm: (String) -> Unit
) {
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
            onLengthChange = onLengthChange,
            onRegenerateClick = onRegenerateClick,
            onSpecialCharactersChange = onHasSpecialCharactersChange
        )
    }
}

@Preview(showBackground = true)
@Composable
@Suppress("FunctionMaxLength")
fun Preview_GeneratePasswordBottomSheetContent(
    @PreviewParameter(CreatePasswordStatePreviewProvider::class) state: CreatePasswordUiState
) {
    ProtonTheme {
        GeneratePasswordBottomSheetContent(
            state = state,
            onLengthChange = {},
            onRegenerateClick = {},
            onHasSpecialCharactersChange = {},
            onConfirm = {}
        )
    }
}
