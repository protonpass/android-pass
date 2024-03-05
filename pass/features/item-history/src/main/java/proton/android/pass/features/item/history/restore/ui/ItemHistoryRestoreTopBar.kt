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

package proton.android.pass.features.item.history.restore.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.topbar.iconbutton.BackArrowCircleIconButton
import proton.android.pass.composecomponents.impl.utils.PassItemColors
import proton.android.pass.features.item.history.R
import me.proton.core.presentation.R as CoreR

@Composable
fun ItemHistoryRestoreTopBar(
    modifier: Modifier = Modifier,
    colors: PassItemColors,
    onUpClick: () -> Unit,
    onRestoreClick: () -> Unit,
) {
    ProtonTopAppBar(
        modifier = modifier,
        backgroundColor = PassTheme.colors.itemDetailBackground,
        title = {},
        navigationIcon = {
            BackArrowCircleIconButton(
                modifier = Modifier.padding(12.dp, Spacing.small),
                color = colors.majorSecondary,
                backgroundColor = colors.minorPrimary,
                onUpClick = onUpClick,
            )
        },
        actions = {
            LoadingCircleButton(
                modifier = Modifier.padding(12.dp, Spacing.small),
                isLoading = false,
                color = colors.majorPrimary,
                leadingIcon = {
                    Icon(
                        painter = painterResource(CoreR.drawable.ic_proton_clock_rotate_left),
                        contentDescription = null,
                        tint = PassTheme.colors.textInvert,
                    )
                },
                text = {
                    Text(
                        text = stringResource(id = R.string.item_history_restore_action),
                        fontWeight = FontWeight.W400,
                        fontSize = 14.sp,
                        color = PassTheme.colors.textInvert,
                        style = ProtonTheme.typography.defaultSmallNorm,
                        maxLines = 1,
                    )
                },
                onClick = onRestoreClick,
            )
        },
    )
}
