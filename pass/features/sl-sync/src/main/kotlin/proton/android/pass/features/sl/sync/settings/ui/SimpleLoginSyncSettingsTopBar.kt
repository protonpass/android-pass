/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.sl.sync.settings.ui

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTopBarBackButtonType
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.topbar.PassExtendedTopBar
import proton.android.pass.features.sl.sync.R
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun SimpleLoginSyncSettingsTopBar(
    modifier: Modifier = Modifier,
    onUpClick: () -> Unit,
    onConfirmClick: () -> Unit,
    isConfirmEnabled: Boolean
) {
    val buttonColorDefault = PassTheme.colors.interactionNormMajor1

    val buttonColor = remember(isConfirmEnabled) {
        if (isConfirmEnabled) {
            buttonColorDefault
        } else {
            buttonColorDefault.copy(alpha = 0.1f)
        }
    }

    PassExtendedTopBar(
        modifier = modifier,
        backButton = PassTopBarBackButtonType.Cross,
        title = stringResource(id = R.string.simple_login_sync_settings_title),
        onUpClick = onUpClick,
        actions = {
            LoadingCircleButton(
                isLoading = false,
                color = buttonColor,
                text = {
                    Text(
                        text = stringResource(id = CompR.string.action_confirm),
                        fontWeight = FontWeight.W400,
                        fontSize = 14.sp,
                        color = PassTheme.colors.textInvert,
                        style = ProtonTheme.typography.defaultSmallNorm,
                        maxLines = 1
                    )
                },
                onClick = onConfirmClick,
                buttonEnabled = isConfirmEnabled,
                showClickEffect = isConfirmEnabled
            )
        }
    )
}
