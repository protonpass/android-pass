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

package proton.android.pass.features.itemdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.buttons.CircleIconButton
import proton.android.pass.composecomponents.impl.buttons.LoadingCircleButton
import proton.android.pass.composecomponents.impl.buttons.PassSharingShareIcon
import proton.android.pass.composecomponents.impl.topbar.iconbutton.BackArrowCircleIconButton
import proton.android.pass.data.api.usecases.ItemActions
import proton.android.pass.domain.items.ItemCategory
import me.proton.core.presentation.R as CoreR

@ExperimentalComposeUiApi
@Composable
internal fun ItemDetailTopBar(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    actions: ItemActions,
    iconColor: Color,
    iconBackgroundColor: Color,
    actionColor: Color,
    itemCategory: ItemCategory,
    shareSharedCount: Int,
    isItemSharingEnabled: Boolean,
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
                modifier = Modifier.padding(Spacing.mediumSmall, Spacing.extraSmall),
                backgroundColor = iconBackgroundColor,
                color = iconColor,
                onUpClick = onUpClick
            )
        },
        actions = {
            ItemTopBarActions(
                actions = actions,
                isLoading = isLoading,
                actionColor = actionColor,
                iconColor = iconColor,
                iconBackgroundColor = iconBackgroundColor,
                itemCategory = itemCategory,
                shareSharedCount = shareSharedCount,
                isItemSharingEnabled = isItemSharingEnabled,
                onEditClick = onEditClick,
                onOptionsClick = onOptionsClick,
                onShareClick = onShareClick
            )
        }
    )
}

@Composable
private fun ItemTopBarActions(
    modifier: Modifier = Modifier,
    actions: ItemActions,
    isLoading: Boolean,
    actionColor: Color,
    iconColor: Color,
    iconBackgroundColor: Color,
    itemCategory: ItemCategory,
    shareSharedCount: Int,
    isItemSharingEnabled: Boolean,
    onEditClick: () -> Unit,
    onOptionsClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(48.dp)
            .padding(horizontal = Spacing.mediumSmall, vertical = Spacing.extraSmall),
        horizontalArrangement = Arrangement.spacedBy(Spacing.extraSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ItemDetailEditButton(
            isLoading = isLoading,
            actionColor = actionColor,
            actions = actions,
            onEditClick = onEditClick
        )

        if (isItemSharingEnabled) {
            if (itemCategory != ItemCategory.Alias) {
                PassSharingShareIcon(
                    shareSharedCount = shareSharedCount,
                    itemCategory = itemCategory,
                    isEnabled = actions.canShare.value,
                    onClick = onShareClick
                )
            }
        } else {
            ItemDetailShareButton(
                isEnabled = actions.canShare.value,
                iconBackgroundColor = iconBackgroundColor,
                iconColor = iconColor,
                onShareClick = onShareClick
            )
        }

        ItemDetailOptionsButton(
            isVisible = !isLoading,
            iconBackgroundColor = iconBackgroundColor,
            iconColor = iconColor,
            onOptionsClick = onOptionsClick
        )
    }
}

@Composable
private fun ItemDetailEditButton(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    actions: ItemActions,
    actionColor: Color,
    onEditClick: () -> Unit
) {
    val textInvertColor = PassTheme.colors.textInvert
    val textNormColor = PassTheme.colors.textNorm
    val (
        editButtonBackgroundColor,
        editButtonForegroundColor,
        editButtonEnabled
    ) = remember(isLoading, actions.canEdit) {
        val enabled = !isLoading && actions.canEdit is ItemActions.CanEditActionState.Enabled
        if (enabled) {
            Triple(actionColor, textInvertColor, true)
        } else {
            Triple(actionColor.copy(alpha = 0.1f), textNormColor.copy(alpha = 0.2f), false)
        }
    }
    LoadingCircleButton(
        modifier = modifier,
        color = editButtonBackgroundColor,
        isLoading = isLoading,
        showClickEffect = editButtonEnabled,
        text = {
            Text(
                text = stringResource(R.string.top_bar_edit_button_text),
                style = ProtonTheme.typography.defaultSmallNorm,
                color = editButtonForegroundColor
            )
        },
        leadingIcon = {
            Icon(
                painter = painterResource(CoreR.drawable.ic_proton_pencil),
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
    isEnabled: Boolean,
    iconBackgroundColor: Color,
    iconColor: Color,
    onShareClick: () -> Unit
) {
    CircleIconButton(
        modifier = modifier,
        drawableRes = CoreR.drawable.ic_proton_users_plus,
        size = 40,
        backgroundColor = iconBackgroundColor,
        tintColor = iconColor,
        iconContentDescription = stringResource(id = R.string.share_button_content_description),
        enabled = isEnabled,
        onClick = onShareClick,
        onDisabledClick = onShareClick
    )
}

@Composable
private fun ItemDetailOptionsButton(
    modifier: Modifier = Modifier,
    isVisible: Boolean,
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
            iconContentDescription = stringResource(id = R.string.open_menu_icon_content_description),
            onClick = onOptionsClick
        )
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
                actionColor = input.second.color,
                iconBackgroundColor = input.second.closeBackgroundColor,
                iconColor = input.second.color,
                onUpClick = {},
                onEditClick = {},
                onOptionsClick = {},
                onShareClick = {},
                actions = input.second.actions,
                itemCategory = ItemCategory.Login,
                shareSharedCount = 0,
                isItemSharingEnabled = false
            )
        }
    }
}

