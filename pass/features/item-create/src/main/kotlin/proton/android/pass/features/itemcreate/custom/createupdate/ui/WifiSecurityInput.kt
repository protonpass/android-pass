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

package proton.android.pass.features.itemcreate.custom.createupdate.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.form.ChevronDownIcon
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.icon.Icon
import proton.android.pass.composecomponents.impl.stringhelpers.getWifiSecurityTypeText
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.domain.WifiSecurityType
import proton.android.pass.features.itemcreate.R
import me.proton.core.presentation.R as CoreR

@Composable
internal fun WifiSecurityInput(
    modifier: Modifier = Modifier,
    wifiSecurityType: WifiSecurityType,
    isEditAllowed: Boolean,
    onClick: (WifiSecurityType) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                enabled = isEditAllowed,
                onClick = { onClick(wifiSecurityType) }
            )
            .padding(
                vertical = Spacing.medium,
                horizontal = Spacing.mediumSmall
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = Spacing.mediumSmall)
    ) {
        Icon.Default(
            id = CoreR.drawable.ic_proton_lock,
            tint = ProtonTheme.colors.iconWeak
        )

        Column(
            modifier = Modifier.weight(1f)
        ) {
            ProtonTextFieldLabel(text = stringResource(R.string.wifi_security_type_field_label))

            Text.Body1Regular(
                text = getWifiSecurityTypeText(wifiSecurityType),
                color = ProtonTheme.colors.textNorm
            )
        }

        if (isEditAllowed) {
            ChevronDownIcon()
        }
    }
}

@Preview
@Composable
internal fun WifiSecurityInputPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            WifiSecurityInput(
                wifiSecurityType = WifiSecurityType.Unknown,
                isEditAllowed = true,
                onClick = {}
            )
        }
    }
}
