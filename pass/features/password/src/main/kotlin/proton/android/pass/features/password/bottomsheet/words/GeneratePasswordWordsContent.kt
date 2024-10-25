/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.password.bottomsheet.words

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
import proton.android.pass.commonrust.api.passwords.PasswordConfig
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.ShowAdvancedOptionsButton
import proton.android.pass.composecomponents.impl.container.AnimatedVisibilityWithOnComplete
import proton.android.pass.composecomponents.impl.container.rememberAnimatedVisibilityState
import proton.android.pass.composecomponents.impl.form.PassDivider
import proton.android.pass.features.password.R
import proton.android.pass.features.password.bottomsheet.GeneratePasswordEvent
import proton.android.pass.features.password.bottomsheet.GeneratePasswordSelectorRow
import proton.android.pass.features.password.bottomsheet.GeneratePasswordToggleRow
import proton.android.pass.features.password.bottomsheet.GeneratePasswordTypeRow
import proton.android.pass.features.password.extensions.toResourceString
import proton.android.pass.preferences.PasswordGenerationMode

@Composable
internal fun GeneratePasswordWordsContent(
    modifier: Modifier = Modifier,
    config: PasswordConfig.Memorable,
    onEvent: (GeneratePasswordEvent) -> Unit
) = with(config) {
    var showAdvancedOptions by rememberSaveable { mutableStateOf(false) }
    val state = rememberAnimatedVisibilityState(initialState = true)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        GeneratePasswordTypeRow(
            current = PasswordGenerationMode.Words,
            onClick = {
                onEvent(GeneratePasswordEvent.OnPasswordModeChangeClick)
            }
        )

        PassDivider()

        GeneratePasswordWordsCountRow(
            count = wordsCount,
            minCount = minWordsCount,
            maxCount = maxWordsCount,
            onCountChange = {
                onEvent(GeneratePasswordEvent.OnWordsCountChange(it))
            }
        )

        PassDivider()

        GeneratePasswordToggleRow(
            text = stringResource(R.string.bottomsheet_option_capitalise),
            value = capitalizeWords,
            isEnabled = canToggleCapitalise,
            onChange = { newCapitalizeWords ->
                GeneratePasswordEvent.OnPasswordConfigChanged(
                    config = config.copy(capitalizeWords = newCapitalizeWords)
                ).also(onEvent)
            }
        )

        PassDivider()

        AnimatedVisibilityWithOnComplete(
            visibilityState = state,
            onComplete = { showAdvancedOptions = true }
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                ShowAdvancedOptionsButton(
                    currentValue = false,
                    onClick = { state.toggle() }
                )
            }
        }

        AnimatedVisibility(visible = showAdvancedOptions) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
            ) {
                wordSeparator?.let { separator ->
                    GeneratePasswordSelectorRow(
                        title = stringResource(R.string.word_separator),
                        value = separator.toResourceString(),
                        iconContentDescription = stringResource(R.string.password_words_separator_icon),
                        onClick = {
                            onEvent(GeneratePasswordEvent.OnWordsSeparatorClick)
                        }
                    )

                    PassDivider()
                }

                GeneratePasswordToggleRow(
                    text = stringResource(R.string.bottomsheet_option_include_numbers),
                    value = includeNumbers,
                    isEnabled = canToggleNumbers,
                    onChange = {
                        onEvent(GeneratePasswordEvent.OnWordsIncludeNumbersChange(it))
                    }
                )

                PassDivider()
            }
        }
    }
}
