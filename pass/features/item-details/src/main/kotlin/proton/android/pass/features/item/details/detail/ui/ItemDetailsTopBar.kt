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

package proton.android.pass.features.item.details.detail.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.appbar.ProtonTopAppBar
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.buttons.CircleIconButton
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.buttons.PassSharingShareIcon
import proton.android.pass.composecomponents.impl.topbar.iconbutton.BackArrowCircleIconButton
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.features.item.details.R
import me.proton.core.presentation.R as CoreR
import proton.android.pass.composecomponents.impl.R as CompR

@Composable
internal fun ItemDetailsTopBar(
    modifier: Modifier = Modifier,
    itemCategory: ItemCategory,
    shareSharedCount: Int,
    onUpClick: () -> Unit,
    isEditEnabled: Boolean,
    onEditClick: () -> Unit,
    areOptionsEnabled: Boolean,
    onOptionsClick: () -> Unit,
    isShareEnabled: Boolean,
    onShareClick: () -> Unit,
    isLoading: Boolean
) {
    val itemColors = passItemColors(itemCategory = itemCategory)

    ProtonTopAppBar(
        modifier = modifier,
        backgroundColor = PassTheme.colors.itemDetailBackground,
        title = { },
        navigationIcon = {
            BackArrowCircleIconButton(
                modifier = Modifier.padding(
                    horizontal = Spacing.mediumSmall,
                    vertical = Spacing.extraSmall
                ),
                backgroundColor = itemColors.minorPrimary,
                color = itemColors.majorSecondary,
                onUpClick = onUpClick
            )
        },
        actions = {
            Row(
                modifier = modifier
                    .height(height = 48.dp)
                    .padding(
                        horizontal = Spacing.mediumSmall,
                        vertical = Spacing.extraSmall
                    ),
                horizontalArrangement = Arrangement.spacedBy(space = Spacing.extraSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ItemDetailEditButton(
                    isLoading = isLoading,
                    actionColor = itemColors.majorPrimary,
                    isEnabled = isEditEnabled,
                    onEditClick = onEditClick
                )

                ItemDetailShareButton(
                    itemCategory = itemCategory,
                    shareSharedCount = shareSharedCount,
                    isEnabled = isShareEnabled,
                    onShareClick = onShareClick
                )

                ItemDetailOptionsButton(
                    isVisible = !isLoading,
                    isEnabled = areOptionsEnabled,
                    iconBackgroundColor = itemColors.minorPrimary,
                    iconColor = itemColors.majorSecondary,
                    onOptionsClick = onOptionsClick
                )
            }
        }
    )
}

@Composable
private fun ItemDetailEditButton(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    isEnabled: Boolean,
    actionColor: Color,
    onEditClick: () -> Unit
) {
    val textInvertColor = PassTheme.colors.textInvert
    val textNormColor = PassTheme.colors.textNorm
    val (editButtonBackgroundColor, editButtonForegroundColor) = remember(isLoading, isEnabled) {
        if (!isLoading && isEnabled) {
            actionColor to textInvertColor
        } else {
            actionColor.copy(alpha = 0.1f) to textNormColor.copy(alpha = 0.2f)
        }
    }

    LoadingCircleButton(
        modifier = modifier,
        color = editButtonBackgroundColor,
        isLoading = isLoading,
        text = {
            Text(
                text = stringResource(id = CompR.string.action_edit),
                style = ProtonTheme.typography.defaultSmallNorm,
                color = editButtonForegroundColor
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(id = CoreR.drawable.ic_proton_pencil),
                contentDescription = null,
                tint = editButtonForegroundColor
            )
        },
        onClick = onEditClick
    )
}

@Composable
private fun ItemDetailShareButton(
    modifier: Modifier = Modifier,
    itemCategory: ItemCategory,
    shareSharedCount: Int,
    isEnabled: Boolean,
    onShareClick: () -> Unit
) {
    if (itemCategory != ItemCategory.Alias) {
        PassSharingShareIcon(
            modifier = modifier,
            itemCategory = itemCategory,
            shareSharedCount = shareSharedCount,
            isEnabled = isEnabled,
            onClick = onShareClick
        )
    }
}

@Composable
private fun ItemDetailOptionsButton(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
    isEnabled: Boolean,
    iconBackgroundColor: Color,
    iconColor: Color,
    onOptionsClick: () -> Unit
) {
    AnimatedVisibility(visible = isVisible) {
        CircleIconButton(
            modifier = modifier,
            drawableRes = CoreR.drawable.ic_proton_three_dots_vertical,
            size = 40,
            backgroundColor = iconBackgroundColor,
            tintColor = iconColor,
            iconContentDescription = stringResource(id = R.string.item_details_toolbar_content_description_menu_button),
            enabled = isEnabled,
            onClick = onOptionsClick
        )
    }
}
