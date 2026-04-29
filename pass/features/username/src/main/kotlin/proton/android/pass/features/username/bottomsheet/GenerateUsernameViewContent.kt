/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.features.username.bottomsheet

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Checkbox
import androidx.compose.material.CheckboxDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import me.proton.core.compose.theme.defaultStrongNorm
import me.proton.core.compose.theme.subheadlineNorm
import proton.android.pass.commonrust.api.passwords.PasswordWordSeparator
import proton.android.pass.commonrust.api.usernames.UsernameConfig
import proton.android.pass.commonrust.api.usernames.UsernameWordTypes
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.buttons.ShowAdvancedOptionsButton
import proton.android.pass.composecomponents.impl.container.AnimatedVisibilityWithOnComplete
import proton.android.pass.composecomponents.impl.container.rememberAnimatedVisibilityState
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.composecomponents.impl.R as CompR
import proton.android.pass.features.username.R

@Composable
internal fun GenerateUsernameViewContent(
    modifier: Modifier = Modifier,
    state: GenerateUsernameUiState,
    forceShowAdvancedOptions: Boolean = false,
    onEvent: (GenerateUsernameUiEvent) -> Unit
) {
    var showAdvancedOptions by rememberSaveable { mutableStateOf(forceShowAdvancedOptions) }
    val advancedToggleState = rememberAnimatedVisibilityState(initialState = true)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 110.dp)
                .wrapContentHeight(align = Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            Text(
                text = state.username,
                color = PassTheme.colors.textNorm,
                style = ProtonTheme.typography.subheadlineNorm.copy(fontFamily = FontFamily.Monospace)
            )
        }

        GenerateUsernameSliderRow(
            text = pluralStringResource(
                CompR.plurals.word_count,
                state.config.wordsCount,
                state.config.wordsCount
            ),
            value = state.config.wordsCount,
            minValue = UsernameConfig.USERNAME_MIN_WORDS,
            maxValue = UsernameConfig.USERNAME_MAX_WORDS,
            onValueChange = { onEvent(GenerateUsernameUiEvent.OnWordCountChange(it)) }
        )

        PassDivider()

        GenerateUsernameSelectorRow(
            title = stringResource(CompR.string.word_separator),
            selectedValue = state.config.wordSeparator.label(),
            onClick = {
                onEvent(GenerateUsernameUiEvent.OnSeparatorChange(state.config.wordSeparator))
            }
        )

        PassDivider()

        AnimatedVisibilityWithOnComplete(
            visibilityState = advancedToggleState,
            onComplete = { showAdvancedOptions = true }
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                ShowAdvancedOptionsButton(
                    currentValue = false,
                    onClick = { advancedToggleState.toggle() }
                )
            }
        }

        AnimatedVisibility(visible = showAdvancedOptions) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                GenerateUsernameToggleRow(
                    text = stringResource(R.string.generate_username_label_include_numbers),
                    value = state.config.includeNumbers,
                    isEnabled = true,
                    onChange = { onEvent(GenerateUsernameUiEvent.OnIncludeNumbersChange(it)) }
                )

                PassDivider()

                GenerateUsernameToggleRow(
                    text = stringResource(CompR.string.bottomsheet_option_capitalise),
                    value = state.config.capitalise,
                    isEnabled = true,
                    onChange = { onEvent(GenerateUsernameUiEvent.OnCapitaliseChange(it)) }
                )

                PassDivider()

                GenerateUsernameToggleRow(
                    text = stringResource(R.string.generate_username_label_leetspeak),
                    value = state.config.leetspeak,
                    isEnabled = true,
                    onChange = { onEvent(GenerateUsernameUiEvent.OnLeetspeakChange(it)) }
                )

                PassDivider()

                WordTypesSection(
                    wordTypes = state.config.wordTypes,
                    onChange = { onEvent(GenerateUsernameUiEvent.OnWordTypesChange(it)) }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WordTypesSection(wordTypes: UsernameWordTypes, onChange: (UsernameWordTypes) -> Unit) {
    val enabledCount = listOf(wordTypes.adjectives, wordTypes.nouns, wordTypes.verbs).count { it }
    val canDisable = enabledCount > 1

    Text(
        text = stringResource(R.string.generate_username_label_word_types),
        color = PassTheme.colors.textNorm,
        style = ProtonTheme.typography.defaultStrongNorm
    )

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        WordTypeChip(
            label = stringResource(R.string.generate_username_label_adjectives),
            checked = wordTypes.adjectives,
            enabled = canDisable || !wordTypes.adjectives,
            onCheckedChange = { onChange(wordTypes.copy(adjectives = it)) }
        )
        WordTypeChip(
            label = stringResource(R.string.generate_username_label_nouns),
            checked = wordTypes.nouns,
            enabled = canDisable || !wordTypes.nouns,
            onCheckedChange = { onChange(wordTypes.copy(nouns = it)) }
        )
        WordTypeChip(
            label = stringResource(R.string.generate_username_label_verbs),
            checked = wordTypes.verbs,
            enabled = canDisable || !wordTypes.verbs,
            onCheckedChange = { onChange(wordTypes.copy(verbs = it)) }
        )
    }
}

@Composable
private fun WordTypeChip(
    label: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.clickable(enabled = enabled) { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            enabled = enabled,
            colors = CheckboxDefaults.colors(
                checkedColor = PassTheme.colors.loginInteractionNormMajor1,
                checkmarkColor = PassTheme.colors.textInvert
            ),
            onCheckedChange = onCheckedChange
        )
        Text(
            text = label,
            color = PassTheme.colors.textNorm,
            style = ProtonTheme.typography.defaultSmallNorm
        )
    }
}

@Composable
private fun PasswordWordSeparator.label(): String = stringResource(
    when (this) {
        PasswordWordSeparator.Hyphen -> CompR.string.bottomsheet_option_word_separator_hyphens
        PasswordWordSeparator.Space -> CompR.string.bottomsheet_option_word_separator_spaces
        PasswordWordSeparator.Period -> CompR.string.bottomsheet_option_word_separator_periods
        PasswordWordSeparator.Comma -> CompR.string.bottomsheet_option_word_separator_commas
        PasswordWordSeparator.Underscore -> CompR.string.bottomsheet_option_word_separator_underscores
        PasswordWordSeparator.Numbers -> CompR.string.bottomsheet_option_word_separator_numbers
        PasswordWordSeparator.NumbersAndSymbols ->
            CompR.string.bottomsheet_option_word_separator_numbers_and_symbols
    }
)

@[Preview Composable]
internal fun GenerateUsernameViewPreview(@PreviewParameter(ThemePreviewProvider::class) isDarkMode: Boolean) {
    PassTheme(isDark = isDarkMode) {
        Surface {
            GenerateUsernameViewContent(
                state = GenerateUsernameUiState.initial(GenerateUsernameMode.CopyAndClose).copy(
                    username = "happy-cat"
                ),
                onEvent = {}
            )
        }
    }
}

@[Preview Composable]
internal fun GenerateUsernameViewExtPreview(@PreviewParameter(ThemePreviewProvider::class) isDarkMode: Boolean) {
    PassTheme(isDark = isDarkMode) {
        Surface {
            GenerateUsernameViewContent(
                state = GenerateUsernameUiState.initial(GenerateUsernameMode.CopyAndClose).copy(
                    username = "happy-cat"
                ),
                forceShowAdvancedOptions = true,
                onEvent = {}
            )
        }
    }
}
