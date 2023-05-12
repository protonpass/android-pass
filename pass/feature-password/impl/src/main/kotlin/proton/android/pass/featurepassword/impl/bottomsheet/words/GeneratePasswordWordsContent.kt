package proton.android.pass.featurepassword.impl.bottomsheet.words

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import proton.android.pass.composecomponents.impl.buttons.ShowAdvancedOptionsButton
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.featurepassword.R
import proton.android.pass.featurepassword.impl.bottomsheet.GeneratePasswordContent
import proton.android.pass.featurepassword.impl.bottomsheet.GeneratePasswordEvent
import proton.android.pass.featurepassword.impl.bottomsheet.GeneratePasswordSelectorRow
import proton.android.pass.featurepassword.impl.bottomsheet.GeneratePasswordToggleRow
import proton.android.pass.featurepassword.impl.bottomsheet.GeneratePasswordTypeRow
import proton.android.pass.featurepassword.impl.extensions.toResourceString
import proton.android.pass.preferences.PasswordGenerationMode

@Composable
fun GeneratePasswordWordsContent(
    modifier: Modifier = Modifier,
    content: GeneratePasswordContent.WordsPassword,
    onEvent: (GeneratePasswordEvent) -> Unit
) {
    var showAdvancedOptions by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GeneratePasswordTypeRow(
            current = PasswordGenerationMode.Words,
            onClick = {
                onEvent(GeneratePasswordEvent.OnPasswordModeChangeClick)
            }
        )
        PassDivider()
        GeneratePasswordWordsCountRow(
            count = content.count,
            onCountChange = {
                onEvent(GeneratePasswordEvent.OnWordsCountChange(it))
            }
        )
        PassDivider()
        GeneratePasswordToggleRow(
            text = stringResource(R.string.bottomsheet_option_capitalise),
            value = content.capitalise,
            onChange = {
                onEvent(GeneratePasswordEvent.OnWordsCapitalizeChange(it))
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
                GeneratePasswordSelectorRow(
                    title = stringResource(R.string.word_separator),
                    value = content.wordSeparator.toResourceString(),
                    iconContentDescription = stringResource(R.string.password_words_separator_icon),
                    onClick = {
                        onEvent(GeneratePasswordEvent.OnWordsSeparatorClick)
                    }
                )
                PassDivider()
                GeneratePasswordToggleRow(
                    text = stringResource(R.string.bottomsheet_option_include_numbers),
                    value = content.includeNumbers,
                    onChange = {
                        onEvent(GeneratePasswordEvent.OnWordsIncludeNumbersChange(it))
                    }
                )
                PassDivider()
            }
        }
    }
}
