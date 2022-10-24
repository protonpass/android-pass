/*
 * Copyright (c) 2021 Proton Technologies AG
 * This file is part of Proton Technologies AG and ProtonCore.
 *
 * ProtonCore is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ProtonCore is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ProtonCore.  If not, see <https://www.gnu.org/licenses/>.
 */
package me.proton.pass.presentation.components.navigation.drawer

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonDimens.DefaultSpacing
import me.proton.core.compose.theme.ProtonDimens.SmallSpacing
import me.proton.core.compose.theme.ProtonTheme

@Composable
fun NavigationDrawerListItem(
    @DrawableRes icon: Int,
    @StringRes title: Int,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit
) = NavigationDrawerListItem(
    icon = painterResource(icon),
    title = stringResource(title),
    modifier = modifier,
    isSelected = isSelected,
    onClick = onClick
)

@Composable
fun NavigationDrawerListItem(
    icon: Painter,
    title: String,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        ProtonTheme.colors.interactionWeakPressed
    } else {
        ProtonTheme.colors.backgroundNorm
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ListItemHeight)
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .padding(horizontal = DefaultSpacing, vertical = SmallSpacing)
            .semantics(mergeDescendants = true) {
                contentDescription = title
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(painter = icon, contentDescription = null, tint = ProtonTheme.colors.iconWeak)
        Text(
            text = title,
            modifier = Modifier.padding(start = ListItemTextStartPadding),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private val ListItemHeight = 48.dp
private val ListItemTextStartPadding = 12.dp
