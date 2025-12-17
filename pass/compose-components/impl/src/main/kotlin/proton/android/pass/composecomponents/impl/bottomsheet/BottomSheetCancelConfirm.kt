/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.composecomponents.impl.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.commonui.api.body3Inverted
import proton.android.pass.commonui.api.body3Weak
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.buttons.UpgradeButton

@Composable
fun BottomSheetCancelConfirm(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    showUpgrade: Boolean = false,
    confirmText: String = stringResource(R.string.bottomsheet_confirm_button),
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    onUpgradeClick: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
    ) {
        // Even if we don't display the loading in the Cancel button, we reuse the same component
        // so it has the exact same height
        LoadingCircleButton(
            modifier = Modifier.weight(1f),
            isLoading = false,
            color = PassTheme.colors.textDisabled,
            onClick = { if (!isLoading) onCancel() },
            buttonHeight = 26.dp,
            text = {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.bottomsheet_cancel_button),
                    style = PassTheme.typography.body3Weak(),
                    textAlign = TextAlign.Center
                )
            }
        )
        if (showUpgrade) {
            UpgradeButton(
                modifier = Modifier
                    .weight(1f)
                    .height(45.dp),
                backgroundColor = PassTheme.colors.loginInteractionNormMajor1,
                contentColor = PassTheme.colors.textInvert,
                onUpgradeClick = onUpgradeClick
            )
        } else {
            LoadingCircleButton(
                modifier = Modifier.weight(1f),
                isLoading = isLoading,
                color = PassTheme.colors.loginInteractionNormMajor1,
                onClick = onConfirm,
                buttonHeight = 26.dp,
                text = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = confirmText,
                        style = PassTheme.typography.body3Inverted(),
                        textAlign = TextAlign.Center,
                        color = PassTheme.colors.textInvert
                    )
                }
            )
        }
    }

}

@Preview
@Composable
fun BottomSheetCancelConfirmPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            BottomSheetCancelConfirm(
                isLoading = input.second,
                onCancel = {},
                onConfirm = {}
            )
        }
    }
}
