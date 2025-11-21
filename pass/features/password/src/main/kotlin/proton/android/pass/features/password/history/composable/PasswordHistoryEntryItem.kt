/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.password.history.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.domain.PasswordHistoryEntryId
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.password.history.model.PasswordHistoryItemUiState
import me.proton.core.presentation.R as PresentationR

private const val PASSWORD_CONCEALED_LENGTH = 15

@Composable
fun PasswordHistoryItem(
    modifier: Modifier = Modifier,
    item: PasswordHistoryItemUiState,
    onPasswordClick: () -> Unit,
    onThreeDotsClick: () -> Unit,
    onChangeVisibility: (Boolean) -> Unit
) {
    val isPasswordVisible by remember(item.value) {
        mutableStateOf(item.value is UIHiddenState.Revealed)
    }

    val password by remember(item.value) {
        mutableStateOf(
            when (item.value) {
                is UIHiddenState.Concealed -> "•".repeat(PASSWORD_CONCEALED_LENGTH)
                is UIHiddenState.Revealed -> item.value.clearText
                is UIHiddenState.Empty -> ""
            }
        )
    }

    Row(
        modifier = modifier
            .roundedContainer(
                backgroundColor = Color.Transparent,
                borderColor = PassTheme.colors.textWeak
            )
            .padding(all = Spacing.medium),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            AnimatedContent(
                targetState = password,
                transitionSpec = {
                    fadeIn().togetherWith(fadeOut())
                }
            ) { pass ->
                Text.Body1Regular(
                    modifier = Modifier.clickable {
                        onPasswordClick()
                    },
                    text = pass,
                    color = PassTheme.colors.textWeak
                )
            }

            Spacer(modifier = Modifier.height(height = Spacing.small))

            Text.Body1Regular(
                text = item.date,
                color = PassTheme.colors.textWeak
            )
        }

        Row {
            IconButton(
                onClick = {
                    onChangeVisibility(!isPasswordVisible)
                }
            ) {
                Icon.Default(
                    id = if (isPasswordVisible) PresentationR.drawable.ic_proton_eye_slash
                    else PresentationR.drawable.ic_proton_eye,
                    tint = PassTheme.colors.passwordInteractionNormMajor2
                )
            }

            IconButton(
                onClick = {
                    onThreeDotsClick()
                }
            ) {
                Icon.Default(
                    id = PresentationR.drawable.ic_proton_three_dots_vertical,
                    tint = PassTheme.colors.passwordInteractionNormMajor2
                )
            }
        }
    }
}


@Composable
@Preview
internal fun PasswordHistoryItemShowPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            PasswordHistoryItem(
                modifier = Modifier.fillMaxWidth(),
                item = PasswordHistoryItemUiState(
                    value = UIHiddenState.Revealed(
                        encrypted = "toto",
                        clearText = "long long long long long long long long password"
                    ),
                    date = "01/01/2002",
                    passwordHistoryEntryId = PasswordHistoryEntryId(0)
                ),
                onThreeDotsClick = {},
                onChangeVisibility = {},
                onPasswordClick = {}
            )
        }
    }
}

@Composable
@Preview
internal fun PasswordHistoryItemHidePreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            PasswordHistoryItem(
                modifier = Modifier.fillMaxWidth(),
                item = PasswordHistoryItemUiState(
                    value = UIHiddenState.Concealed(
                        encrypted = "toto"
                    ),
                    date = "01/01/2003",
                    passwordHistoryEntryId = PasswordHistoryEntryId(0)
                ),
                onThreeDotsClick = {},
                onChangeVisibility = {},
                onPasswordClick = {}
            )
        }
    }
}
