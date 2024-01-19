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

package proton.android.pass.featuresharing.impl.sharingsummary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import me.proton.core.compose.theme.defaultWeak
import me.proton.core.presentation.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.container.BoxedIcon
import proton.android.pass.featuresharing.impl.common.AddressPermissionUiState
import proton.android.pass.featuresharing.impl.extensions.toStringResource
import proton.android.pass.featuresharing.impl.sharingpermissions.SharingType

@Composable
fun AddressRowSection(
    modifier: Modifier = Modifier,
    address: AddressPermissionUiState
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        BoxedIcon(
            backgroundColor = PassTheme.colors.interactionNormMinor1,
            size = 40,
            shape = PassTheme.shapes.squircleMediumShape
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_proton_envelope),
                contentDescription = null,
                tint = PassTheme.colors.interactionNormMajor2
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = address.address,
                style = ProtonTheme.typography.defaultSmallNorm()
            )
            Text(
                text = stringResource(address.permission.toStringResource()),
                style = ProtonTheme.typography.defaultWeak,
                color = PassTheme.colors.textNorm
            )
        }
    }
}

@Preview
@Composable
fun AddressRowSectionPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            AddressRowSection(
                address = AddressPermissionUiState("my@test.email", SharingType.Write)
            )
        }
    }
}
