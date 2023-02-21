package proton.android.pass.featurecreateitem.impl.login.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.PassDimens.bottomSheetPadding
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetTitle
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetTitleButton
import proton.android.pass.featurecreateitem.impl.R
import proton.android.pass.featurecreateitem.impl.password.CreatePasswordStatePreviewProvider
import proton.android.pass.featurecreateitem.impl.password.CreatePasswordUiState
import proton.android.pass.featurecreateitem.impl.password.CreatePasswordViewContent

@Composable
fun GeneratePasswordBottomSheetContent(
    modifier: Modifier = Modifier,
    state: CreatePasswordUiState,
    onLengthChange: (Int) -> Unit,
    onRegenerateClick: () -> Unit,
    onHasSpecialCharactersChange: (Boolean) -> Unit,
    onConfirm: (String) -> Unit
) {
    Column(
        modifier = modifier.bottomSheetPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BottomSheetTitle(
            title = stringResource(id = R.string.button_generate_password),
            button = BottomSheetTitleButton(
                title = stringResource(id = R.string.bottom_sheet_generate_password_confirm),
                onClick = { onConfirm(state.password) },
                enabled = true
            ),
            showDivider = false
        )
        CreatePasswordViewContent(
            state = state,
            onLengthChange = onLengthChange,
            onRegenerateClick = onRegenerateClick,
            onSpecialCharactersChange = onHasSpecialCharactersChange
        )
    }
}

@Preview
@Composable
@Suppress("FunctionMaxLength")
fun GeneratePasswordBottomSheetContentThemePreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            GeneratePasswordBottomSheetContent(
                state = CreatePasswordUiState(
                    password = "a1b!c_d3e#fg",
                    length = 12,
                    hasSpecialCharacters = true
                ),
                onLengthChange = {},
                onRegenerateClick = {},
                onHasSpecialCharactersChange = {},
                onConfirm = {}
            )
        }
    }
}

@Preview
@Composable
@Suppress("FunctionMaxLength")
fun GeneratePasswordBottomSheetContentPreview(
    @PreviewParameter(CreatePasswordStatePreviewProvider::class) state: CreatePasswordUiState
) {
    ProtonTheme {
        Surface {
            GeneratePasswordBottomSheetContent(
                state = state,
                onLengthChange = {},
                onRegenerateClick = {},
                onHasSpecialCharactersChange = {},
                onConfirm = {}
            )
        }
    }
}
