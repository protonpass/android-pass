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

package proton.android.pass.features.security.center.shared.ui.headers

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.applyIf
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun SecurityCenterListStickyHeader(
    modifier: Modifier = Modifier,
    isCollapsed: Boolean = false,
    onClick: (() -> Unit)? = null,
    label: @Composable () -> Unit
) {
    val isClickable = remember(onClick) {
        onClick != null
    }

    @DrawableRes val chevronResId = remember(isCollapsed) {
        if (isCollapsed) CompR.drawable.ic_chevron_tiny_right
        else CompR.drawable.ic_chevron_tiny_down
    }

    Row(
        modifier = modifier
            .applyIf(
                condition = isClickable,
                ifTrue = { clickable { onClick?.invoke() } }
            )
            .fillMaxWidth()
            .background(color = PassTheme.colors.backgroundStrong)
            .padding(
                vertical = Spacing.small,
                horizontal = Spacing.medium
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        if (isClickable) {
            Icon(
                modifier = Modifier.width(20.dp),
                painter = painterResource(id = chevronResId),
                contentDescription = null,
                tint = ProtonTheme.colors.iconWeak
            )
        }

        label()
    }
}
