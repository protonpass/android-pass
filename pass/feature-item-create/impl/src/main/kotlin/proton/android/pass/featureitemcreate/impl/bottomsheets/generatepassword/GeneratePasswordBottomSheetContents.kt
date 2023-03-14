package proton.android.pass.featureitemcreate.impl.bottomsheets.generatepassword

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonui.api.bottomSheetPadding
import proton.android.pass.composecomponents.impl.buttons.CircleButton
import proton.android.pass.composecomponents.impl.generatepassword.GeneratePasswordBottomSheetTitle
import proton.android.pass.composecomponents.impl.generatepassword.GeneratePasswordStatePreviewProvider
import proton.android.pass.composecomponents.impl.generatepassword.GeneratePasswordUiState
import proton.android.pass.composecomponents.impl.generatepassword.GeneratePasswordViewContent
import proton.android.pass.featureitemcreate.impl.R

@Composable
fun GeneratePasswordBottomSheetContent(
    modifier: Modifier = Modifier,
    state: GeneratePasswordUiState,
    onLengthChange: (Int) -> Unit,
    onHasSpecialCharactersChange: (Boolean) -> Unit,
    onRegenerateClick: () -> Unit,
    onConfirm: (String) -> Unit
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
        CircleButton(
            modifier = Modifier.fillMaxWidth(),
            color = PassTheme.colors.accentPurpleOpaque,
            onClick = { onConfirm(state.password) }
        ) {
            Text(
                text = stringResource(R.string.generate_password_copy),
                style = PassTypography.body3RegularInverted
            )
        }
    }
}

class ThemeAndCreatePasswordUiStateProvider :
    ThemePairPreviewProvider<GeneratePasswordUiState>(GeneratePasswordStatePreviewProvider())

@Preview
@Composable
fun GenPasswordBottomSheetContentPreview(
    @PreviewParameter(ThemeAndCreatePasswordUiStateProvider::class)
    input: Pair<Boolean, GeneratePasswordUiState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            GeneratePasswordBottomSheetContent(
                state = input.second,
                onLengthChange = {},
                onHasSpecialCharactersChange = {},
                onRegenerateClick = {},
                onConfirm = {}
            )
        }
    }
}
