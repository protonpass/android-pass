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
package proton.android.pass.presentation.navigation.drawer

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonDimens.DefaultSpacing
import me.proton.core.compose.theme.ProtonDimens.SmallSpacing
import me.proton.core.compose.theme.ProtonTheme

@Composable
fun NavigationDrawerListItem(
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int,
    title: String,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    closeDrawerAction: () -> Unit,
    startContent: @Composable () -> Unit = {},
    endContent: @Composable () -> Unit = {}
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
            .clickable(onClick = {
                onClick()
                closeDrawerAction()
            })
            .background(backgroundColor)
            .padding(horizontal = DefaultSpacing, vertical = SmallSpacing)
            .semantics(mergeDescendants = true) {
                contentDescription = title
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        startContent()
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = ProtonTheme.colors.iconWeak
        )
        Text(
            text = title,
            modifier = Modifier.padding(start = ListItemTextStartPadding),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.weight(1f))
        endContent()
    }
}

private val ListItemHeight = 48.dp
private val ListItemTextStartPadding = 12.dp
