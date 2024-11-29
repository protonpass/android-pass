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

package proton.android.pass.featureitemcreate.impl.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallInverted
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.domain.Vault

@Composable
fun RowScope.VaultSwitcher(
    modifier: Modifier = Modifier,
    selectedVault: Vault,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .weight(weight = 1f, fill = false)
            .clip(CircleShape)
            .background(selectedVault.color.toColor(isBackground = true))
            .clickable { onClick() }
            .padding(horizontal = Spacing.medium, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            painter = painterResource(id = selectedVault.icon.toResource()),
            contentDescription = null,
            tint = selectedVault.color.toColor()
        )
        Text(
            modifier = Modifier.weight(weight = 1f, fill = false),
            text = selectedVault.name,
            style = ProtonTheme.typography.defaultSmallInverted,
            color = selectedVault.color.toColor(),
            overflow = TextOverflow.Ellipsis
        )
        Icon(
            painter = painterResource(R.drawable.ic_chevron_tiny_down),
            contentDescription = null,
            tint = selectedVault.color.toColor()
        )
    }
}
