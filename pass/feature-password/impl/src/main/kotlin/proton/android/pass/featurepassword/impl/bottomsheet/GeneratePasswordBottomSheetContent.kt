package proton.android.pass.featurepassword.impl.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ButtonDefaults
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
import proton.android.pass.commonui.api.bottomSheet
import proton.android.pass.composecomponents.impl.buttons.CircleButton
import proton.android.pass.featurepassword.R

@Composable
fun GeneratePasswordBottomSheetContent(
    modifier: Modifier = Modifier,
    state: GeneratePasswordUiState,
    onEvent: (GeneratePasswordEvent) -> Unit,
    buttonSection: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .bottomSheet(horizontalPadding = PassTheme.dimens.bottomsheetHorizontalPadding)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        GeneratePasswordBottomSheetTitle(onRegenerate = {
            onEvent(GeneratePasswordEvent.OnRegeneratePasswordClick)
        })
        GeneratePasswordViewContent(
            state = state,
            onEvent = onEvent
        )
        buttonSection()
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
                onEvent = {},
                buttonSection = {
                    CircleButton(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(14.dp),
                        color = PassTheme.colors.loginInteractionNormMajor1,
                        elevation = ButtonDefaults.elevation(0.dp),
                        onClick = { }
                    ) {
                        Text(
                            text = stringResource(R.string.generate_password_copy),
                            style = PassTypography.body3RegularInverted,
                            color = PassTheme.colors.textInvert
                        )
                    }
                }
            )
        }
    }
}

