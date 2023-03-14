package proton.android.pass.featureitemcreate.impl.login.bottomsheet.password

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheetPadding
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetCancelConfirm
import proton.android.pass.composecomponents.impl.generatepassword.GeneratePasswordBottomSheetTitle
import proton.android.pass.composecomponents.impl.generatepassword.GeneratePasswordStatePreviewProvider
import proton.android.pass.composecomponents.impl.generatepassword.GeneratePasswordUiState
import proton.android.pass.composecomponents.impl.generatepassword.GeneratePasswordViewContent

@Composable
fun GeneratePasswordBottomSheetContent(
    modifier: Modifier = Modifier,
    state: GeneratePasswordUiState,
    onLengthChange: (Int) -> Unit,
    onRegenerateClick: () -> Unit,
    onHasSpecialCharactersChange: (Boolean) -> Unit,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = modifier.bottomSheetPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GeneratePasswordBottomSheetTitle(onRegenerate = { onRegenerateClick() })
        GeneratePasswordViewContent(
            state = state,
            onLengthChange = onLengthChange,
            onSpecialCharactersChange = onHasSpecialCharactersChange
        )
        BottomSheetCancelConfirm(onCancel = onDismiss, onConfirm = { onConfirm(state.password) })
    }
}

@Preview
@Composable
@Suppress("FunctionMaxLength")
fun GeneratePasswordBottomSheetContentThemePreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            GeneratePasswordBottomSheetContent(
                state = GeneratePasswordUiState(
                    password = "a1b!c_d3e#fg",
                    length = 12,
                    hasSpecialCharacters = true
                ),
                onLengthChange = {},
                onRegenerateClick = {},
                onHasSpecialCharactersChange = {},
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}

@Preview
@Composable
@Suppress("FunctionMaxLength")
fun GeneratePasswordBottomSheetContentPreview(
    @PreviewParameter(GeneratePasswordStatePreviewProvider::class) state: GeneratePasswordUiState
) {
    PassTheme {
        Surface {
            GeneratePasswordBottomSheetContent(
                state = state,
                onLengthChange = {},
                onRegenerateClick = {},
                onHasSpecialCharactersChange = {},
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}
