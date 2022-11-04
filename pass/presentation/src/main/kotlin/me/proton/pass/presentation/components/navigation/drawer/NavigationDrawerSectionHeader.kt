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

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonDimens.DefaultSpacing
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.headline

@Composable
fun NavigationDrawerSectionHeader(
    @StringRes title: Int,
    modifier: Modifier = Modifier
) = NavigationDrawerSectionHeader(stringResource(title), modifier)

@Composable
fun NavigationDrawerSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .semantics(mergeDescendants = true) {
                contentDescription = title
                heading()
            }
            .padding(top = DefaultSpacing)
            .fillMaxWidth()
    ) {

        Divider()

        Text(
            text = title,
            modifier = Modifier.padding(
                vertical = DefaultSpacing,
                horizontal = SectionHeaderHorizontalPadding
            ),
            style = ProtonTheme.typography.headline,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private val SectionHeaderHorizontalPadding = 20.dp
