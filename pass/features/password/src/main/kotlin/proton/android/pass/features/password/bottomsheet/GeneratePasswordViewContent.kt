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

package proton.android.pass.features.password.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.subheadlineNorm
import proton.android.pass.common.api.PasswordStrength
import proton.android.pass.commonrust.api.passwords.PasswordConfig
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.toPasswordAnnotatedString
import proton.android.pass.composecomponents.impl.item.PassPasswordStrengthItem
import proton.android.pass.features.password.bottomsheet.random.GeneratePasswordRandomContent
import proton.android.pass.features.password.bottomsheet.words.GeneratePasswordWordsContent

@Composable
internal fun GeneratePasswordViewContent(
    modifier: Modifier = Modifier,
    state: GeneratePasswordUiState,
    onEvent: (GeneratePasswordEvent) -> Unit
) = with(state) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .defaultMinSize(minHeight = 110.dp)
                .wrapContentHeight(align = Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            Text(
                text = password.toPasswordAnnotatedString(
                    digitColor = PassTheme.colors.loginInteractionNormMajor2,
                    symbolColor = PassTheme.colors.aliasInteractionNormMajor2,
                    letterColor = PassTheme.colors.textNorm
                ),
                style = ProtonTheme.typography.subheadlineNorm.copy(fontFamily = FontFamily.Monospace)
            )

            PassPasswordStrengthItem(passwordStrength = passwordStrength)
        }

        passwordConfig?.let { config ->
            when (config) {
                is PasswordConfig.Memorable -> {
                    GeneratePasswordWordsContent(
                        config = config,
                        onEvent = onEvent
                    )
                }

                is PasswordConfig.Random -> {
                    GeneratePasswordRandomContent(
                        config = config,
                        onEvent = onEvent
                    )
                }
            }
        }
    }
}

@[Preview Composable]
internal fun GeneratePasswordViewContentThemePreview(
    @PreviewParameter(ThemePreviewProvider::class) isDarkMode: Boolean
) {
    PassTheme(isDark = isDarkMode) {
        Surface {
            GeneratePasswordViewContent(
                state = GeneratePasswordUiState(
                    password = "a1b!c_d3e#fg",
                    passwordStrength = PasswordStrength.Strong,
                    mode = GeneratePasswordMode.CopyAndClose,
                    passwordConfig = PasswordConfig.Random(
                        passwordLength = 12,
                        includeNumbers = true,
                        includeUppercase = false,
                        includeSymbols = true
                    )
                ),
                onEvent = {}
            )
        }
    }
}
