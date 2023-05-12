package proton.android.pass.featurepassword.impl.bottomsheet.random

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.buttons.ShowAdvancedOptionsButton
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.featurepassword.R
import proton.android.pass.featurepassword.impl.bottomsheet.GeneratePasswordContent
import proton.android.pass.featurepassword.impl.bottomsheet.GeneratePasswordEvent
import proton.android.pass.featurepassword.impl.bottomsheet.GeneratePasswordToggleRow
import proton.android.pass.featurepassword.impl.bottomsheet.GeneratePasswordTypeRow
import proton.android.pass.preferences.PasswordGenerationMode

@Composable
fun GeneratePasswordRandomContent(
    modifier: Modifier = Modifier,
    content: GeneratePasswordContent.RandomPassword,
    onEvent: (GeneratePasswordEvent) -> Unit
) {
    var showAdvancedOptions by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GeneratePasswordTypeRow(
            current = PasswordGenerationMode.Random,
            onClick = {
                onEvent(GeneratePasswordEvent.OnPasswordModeChangeClick)
            }
        )
        PassDivider()
        GeneratePasswordRandomCountRow(
            length = content.length,
            onLengthChange = {
                onEvent(GeneratePasswordEvent.OnRandomLengthChange(it))
            }
        )
        PassDivider()
        GeneratePasswordToggleRow(
            text = stringResource(R.string.special_characters),
            value = content.hasSpecialCharacters,
            onChange = {
                onEvent(GeneratePasswordEvent.OnRandomUseSpecialCharactersChange(it))
            }
        )
        PassDivider()
        if (!showAdvancedOptions) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                ShowAdvancedOptionsButton(
                    currentValue = false,
                    onClick = { showAdvancedOptions = true }
                )
            }
        }

        AnimatedVisibility(visible = showAdvancedOptions) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                GeneratePasswordToggleRow(
                    text = stringResource(R.string.bottomsheet_option_capital_letters),
                    value = content.hasCapitalLetters,
                    onChange = {
                        onEvent(GeneratePasswordEvent.OnRandomUseCapitalLettersChange(it))
                    }
                )
                PassDivider()
                GeneratePasswordToggleRow(
                    text = stringResource(R.string.bottomsheet_option_include_numbers),
                    value = content.includeNumbers,
                    onChange = {
                        onEvent(GeneratePasswordEvent.OnRandomIncludeNumbersChange(it))
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun GeneratePasswordRandomContentPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            GeneratePasswordRandomContent(
                content = GeneratePasswordContent.RandomPassword(
                    length = 12,
                    hasSpecialCharacters = true,
                    hasCapitalLetters = false,
                    includeNumbers = true
                ),
                onEvent = {}
            )
        }
    }
}
