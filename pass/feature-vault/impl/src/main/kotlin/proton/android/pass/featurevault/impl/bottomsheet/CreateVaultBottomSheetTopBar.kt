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

package proton.android.pass.featurevault.impl.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonui.api.body3Norm
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.buttons.UpgradeButton
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.feature.vault.impl.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun CreateVaultBottomSheetTopBar(
    modifier: Modifier = Modifier,
    buttonText: String,
    isLoading: Boolean,
    isButtonEnabled: Boolean,
    showUpgradeButton: Boolean,
    onCloseClick: () -> Unit,
    onCreateClick: () -> Unit,
    onUpgradeClick: () -> Unit
) {

    val keyboardController = LocalSoftwareKeyboardController.current
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Circle(
            backgroundColor = PassTheme.colors.loginInteractionNormMinor1,
            onClick = onCloseClick
        ) {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_cross),
                contentDescription = stringResource(R.string.bottomsheet_close_icon_content_description),
                tint = PassTheme.colors.loginInteractionNormMajor2
            )
        }

        val buttonColor = if (isButtonEnabled) {
            PassTheme.colors.loginInteractionNormMajor1
        } else {
            PassTheme.colors.loginInteractionNormMinor2
        }

        if (showUpgradeButton) {
            UpgradeButton(
                onUpgradeClick = {
                    keyboardController?.hide()
                    onUpgradeClick()
                }
            )
        } else {
            LoadingCircleButton(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                color = buttonColor,
                isLoading = isLoading,
                buttonEnabled = isButtonEnabled,
                text = {
                    Text(
                        text = buttonText,
                        color = PassTheme.colors.textInvert,
                        style = PassTheme.typography.body3Norm()
                    )
                },
                onClick = {
                    keyboardController?.hide()
                    onCreateClick()
                }
            )
        }
    }
}

class ThemeCreateVaultTopBarPreviewProvider :
    ThemePairPreviewProvider<TopBarInput>(CreateVaultBottomSheetTopBarPreviewProvider())

@Preview
@Composable
fun CreateVaultBottomSheetTopBarPreview(
    @PreviewParameter(ThemeCreateVaultTopBarPreviewProvider::class) input: Pair<Boolean, TopBarInput>
) {
    PassTheme(isDark = input.first) {
        Surface {
            CreateVaultBottomSheetTopBar(
                buttonText = stringResource(R.string.bottomsheet_create_vault_button),
                isLoading = input.second.isLoading,
                isButtonEnabled = true,
                showUpgradeButton = input.second.showUpgrade,
                onCloseClick = {},
                onCreateClick = {},
                onUpgradeClick = {}
            )
        }
    }
}
