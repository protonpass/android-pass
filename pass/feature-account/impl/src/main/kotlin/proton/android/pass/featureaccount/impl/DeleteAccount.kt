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

package proton.android.pass.featureaccount.impl

import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.setting.ColorSettingOption

@Composable
fun DeleteAccount(modifier: Modifier = Modifier, onDeleteAccountClick: () -> Unit) {
    ColorSettingOption(
        modifier = modifier.roundedContainerNorm(),
        text = stringResource(R.string.account_delete_account),
        textColor = PassTheme.colors.passwordInteractionNormMajor2,
        iconBgColor = PassTheme.colors.passwordInteractionNormMinor1,
        icon = {
            Icon(
                painter = painterResource(me.proton.core.presentation.compose.R.drawable.ic_proton_trash_cross),
                contentDescription = stringResource(R.string.account_delete_account_icon_content_description),
                tint = PassTheme.colors.passwordInteractionNormMajor2
            )
        },
        onClick = onDeleteAccountClick
    )
}

@Preview
@Composable
fun DeleteAccountPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            DeleteAccount {}
        }
    }
}
