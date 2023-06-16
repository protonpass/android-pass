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

package proton.android.pass.composecomponents.impl.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.R

@Composable
fun ActionableItemRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    vaultIcon: Int? = null,
    highlight: String = "",
    showMenuIcon: Boolean,
    canLoadExternalImages: Boolean,
    onItemClick: (ItemUiModel) -> Unit = {},
    onItemMenuClick: (ItemUiModel) -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick(item) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ItemRowContents(
            modifier = Modifier.weight(1f),
            item = item,
            highlight = highlight,
            vaultIcon = vaultIcon,
            canLoadExternalImages = canLoadExternalImages
        )
        if (showMenuIcon) {
            IconButton(
                onClick = { onItemMenuClick(item) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_three_dots_vertical_24),
                    contentDescription = stringResource(id = R.string.action_content_description_menu),
                    tint = PassTheme.colors.textHint
                )
            }
        }
    }
}

class ThemeAndItemUiModelProvider :
    ThemePairPreviewProvider<ItemUiModel>(ItemUiModelPreviewProvider())

@Preview
@Composable
fun ActionableItemRowPreviewWithMenuIcon(
    @PreviewParameter(ThemeAndItemUiModelProvider::class) input: Pair<Boolean, ItemUiModel>
) {
    PassTheme(isDark = input.first) {
        Surface {
            ActionableItemRow(
                item = input.second,
                showMenuIcon = true,
                vaultIcon = null,
                canLoadExternalImages = false
            )
        }
    }
}

@Preview
@Composable
fun ActionableItemRowPreviewWithoutMenuIcon(
    @PreviewParameter(ThemeAndItemUiModelProvider::class) input: Pair<Boolean, ItemUiModel>
) {
    PassTheme(isDark = input.first) {
        Surface {
            ActionableItemRow(
                item = input.second,
                showMenuIcon = false,
                vaultIcon = null,
                canLoadExternalImages = false
            )
        }
    }
}

@Preview
@Composable
fun ActionableItemRowPreviewWithVaultIcon(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            ActionableItemRow(
                item = ItemUiModelPreviewProvider().values.first(),
                showMenuIcon = false,
                vaultIcon = R.drawable.ic_bookmark_small,
                canLoadExternalImages = false
            )
        }
    }
}
