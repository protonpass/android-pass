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

package proton.android.pass.featureitemdetail.impl

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.container.Circle
import proton.android.pass.composecomponents.impl.topbar.iconbutton.BackArrowCircleIconButton
import me.proton.core.presentation.R as CoreR

@ExperimentalComposeUiApi
@Composable
internal fun ItemDetailTopBar(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    isInTrash: Boolean,
    showActions: Boolean,
    iconColor: Color,
    iconBackgroundColor: Color,
    actionColor: Color,
    onUpClick: () -> Unit,
    onEditClick: () -> Unit,
    onOptionsClick: () -> Unit,
    onShareClick: () -> Unit
) {
    ProtonTopAppBar(
        modifier = modifier,
        backgroundColor = PassTheme.colors.itemDetailBackground,
        title = { },
        navigationIcon = {
            BackArrowCircleIconButton(
                modifier = Modifier.padding(12.dp, 4.dp),
                backgroundColor = iconBackgroundColor,
                color = iconColor,
                onUpClick = onUpClick
            )
        },
        actions = {
            if (showActions) {
                ItemTopBarActions(
                    isInTrash = isInTrash,
                    isLoading = isLoading,
                    actionColor = actionColor,
                    iconColor = iconColor,
                    iconBackgroundColor = iconBackgroundColor,
                    onEditClick = onEditClick,
                    onOptionsClick = onOptionsClick,
                    onShareClick = onShareClick
                )
            }
        }
    )
}

@Composable
private fun ItemTopBarActions(
    modifier: Modifier = Modifier,
    isInTrash: Boolean,
    isLoading: Boolean,
    actionColor: Color,
    iconColor: Color,
    iconBackgroundColor: Color,
    onEditClick: () -> Unit,
    onOptionsClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (!isInTrash) {
            LoadingCircleButton(
                color = actionColor,
                isLoading = isLoading,
                text = {
                    Text(
                        text = stringResource(R.string.top_bar_edit_button_text),
                        style = ProtonTheme.typography.defaultSmallNorm,
                        color = PassTheme.colors.textInvert
                    )
                },
                leadingIcon = {
                    Icon(
                        painter = painterResource(CoreR.drawable.ic_proton_pencil),
                        contentDescription = null,
                        tint = PassTheme.colors.textInvert
                    )
                },
                onClick = { onEditClick() }
            )

            Circle(
                backgroundColor = iconBackgroundColor,
                onClick = onShareClick
            ) {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_proton_users_plus),
                    contentDescription = stringResource(R.string.share_button_content_description),
                    tint = iconColor
                )

            }
        }
        AnimatedVisibility(visible = !isLoading) {
            Circle(
                backgroundColor = iconBackgroundColor,
                onClick = { onOptionsClick() }
            ) {
                Icon(
                    painter = painterResource(CoreR.drawable.ic_proton_three_dots_vertical),
                    contentDescription = stringResource(R.string.open_menu_icon_content_description),
                    tint = iconColor
                )
            }
        }
    }
}

class ThemeAndAccentColorProvider :
    ThemePairPreviewProvider<ItemDetailTopBarPreview>(ItemDetailTopBarPreviewProvider())

@OptIn(ExperimentalComposeUiApi::class)
@Preview
@Composable
fun ItemDetailTopBarPreview(
    @PreviewParameter(ThemeAndAccentColorProvider::class) input: Pair<Boolean, ItemDetailTopBarPreview>
) {
    PassTheme(isDark = input.first) {
        Surface {
            ItemDetailTopBar(
                isLoading = input.second.isLoading,
                isInTrash = false,
                actionColor = input.second.color,
                iconBackgroundColor = input.second.closeBackgroundColor,
                iconColor = input.second.color,
                showActions = input.second.showActions,
                onUpClick = {},
                onEditClick = {},
                onOptionsClick = {},
                onShareClick = {}
            )
        }
    }
}

