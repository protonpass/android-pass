/*
 * Copyright (c) 2023-2026 Proton AG
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

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
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
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.toPasswordAnnotatedString
import proton.android.pass.commonuimodels.api.passwords.PasswordChecksUiState
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.buttons.CircleIconButton
import proton.android.pass.composecomponents.impl.item.PassPasswordChecksList
import proton.android.pass.composecomponents.impl.item.PassPasswordStrengthItem
import proton.android.pass.composecomponents.impl.labels.PassPasswordStrengthLabel
import proton.android.pass.features.itemcreate.common.PasswordInputLeadingIcon
import proton.android.pass.features.password.bottomsheet.random.GeneratePasswordRandomContent
import proton.android.pass.features.password.bottomsheet.words.GeneratePasswordWordsContent
import proton.android.pass.features.password.R as PasswordR

@Composable
internal fun GeneratePasswordViewContent(
    modifier: Modifier = Modifier,
    state: GeneratePasswordUiState,
    onEvent: (GeneratePasswordUiEvent) -> Unit
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
            Row(
                modifier = Modifier,
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.mediumSmall)
            ) {
                Row(
                    modifier = Modifier
                        .defaultMinSize(minHeight = 52.dp)
                        .weight(1f)
                        .border(
                            width = 1.dp,
                            color = PassTheme.colors.inputBorderStrong,
                            shape = RoundedCornerShape(size = Spacing.mediumSmall)
                        )
                        .padding(all = Spacing.mediumSmall),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = Spacing.mediumSmall)
                ) {
                    Text(
                        modifier = Modifier.weight(weight = 1f),
                        text = password.toPasswordAnnotatedString(
                            digitColor = PassTheme.colors.loginInteractionNormMajor2,
                            symbolColor = PassTheme.colors.aliasInteractionNormMajor2,
                            letterColor = PassTheme.colors.textNorm
                        ),
                        style = ProtonTheme.typography.subheadlineNorm.copy(fontFamily = FontFamily.Monospace)
                    )

                    PasswordInputLeadingIcon(
                        passwordStrength = passwordStrength
                    )
                }

                CircleIconButton(
                    modifier = Modifier.size(size = 40.dp),
                    backgroundColor = PassTheme.colors.loginInteractionNormMinor1,
                    onClick = { onEvent(GeneratePasswordUiEvent.OnRegeneratePasswordClick) }
                ) {
                    Icon(
                        painter = painterResource(
                            me.proton.core.presentation.compose.R.drawable.ic_proton_arrows_rotate
                        ),
                        contentDescription = stringResource(
                            PasswordR.string.regenerate_password_icon_content_description
                        ),
                        tint = PassTheme.colors.loginInteractionNormMajor2
                    )
                }
            }


            if (isPasswordChecksEnabled) {
                Spacer(modifier = Modifier.height(Spacing.extraSmall))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall)
                    ) {
                        PassPasswordStrengthLabel(
                            passwordStrength = passwordStrength,
                            labelPrefix = stringResource(R.string.password_strength_prefix)
                        )
                    }
                    PassPasswordChecksList(checks = passwordChecks)
                }
            }

            if (!isPasswordChecksEnabled) {
                PassPasswordStrengthItem(passwordStrength = passwordStrength)
            }
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
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val (isDarkMode, isPasswordChecksEnabled) = input

    PassTheme(isDark = isDarkMode) {
        Surface {
            GeneratePasswordViewContent(
                state = GeneratePasswordUiState(
                    password = "a1b!c_d3e#fg",
                    passwordStrength = PasswordStrength.Strong,
                    passwordChecks = PasswordChecksUiState.Initial.copy(
                        hasMinLength = true,
                        hasLowercase = true,
                        hasUppercase = true,
                        hasNoRepeatedCharacters = true,
                        hasNoCommonPassword = true
                    ),
                    isPasswordChecksEnabled = isPasswordChecksEnabled,
                    mode = GeneratePasswordMode.CopyAndClose,
                    event = GeneratePasswordEvent.Idle,
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
