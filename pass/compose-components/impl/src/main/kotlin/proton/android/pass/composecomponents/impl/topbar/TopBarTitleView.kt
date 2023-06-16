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

package proton.android.pass.composecomponents.impl.topbar

import androidx.annotation.StringRes
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme

@Composable
fun TopBarTitleView(
    modifier: Modifier = Modifier,
    @StringRes title: Int
) {
    TopBarTitleView(
        modifier = modifier,
        title = stringResource(title)
    )
}

@Composable
fun TopBarTitleView(
    modifier: Modifier = Modifier,
    title: String
) {
    Text(
        modifier = modifier,
        text = title,
        fontWeight = FontWeight.W500,
        fontSize = 16.sp,
        color = ProtonTheme.colors.textNorm,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
