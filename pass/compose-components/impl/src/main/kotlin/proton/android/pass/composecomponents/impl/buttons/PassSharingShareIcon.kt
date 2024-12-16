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

package proton.android.pass.composecomponents.impl.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Radius
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.composecomponents.impl.utils.passItemColors
import proton.android.pass.domain.items.ItemCategory
import me.proton.core.presentation.R as CoreR

@Composable
fun PassSharingShareIcon(
    modifier: Modifier = Modifier,
    itemCategory: ItemCategory,
    shareSharedCount: Int,
    onClick: () -> Unit,
    isEnabled: Boolean = true
) {
    val backgroundAlpha = remember(isEnabled) { if (isEnabled) 1f else 0.6f }

    val contentAlpha = remember(isEnabled) { if (isEnabled) 1f else 0.2f }

    val itemColors = passItemColors(itemCategory)

    val showShareSharedCounter = remember(shareSharedCount) { shareSharedCount > 1 }

    Row(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = Radius.large))
            .background(color = itemColors.minorPrimary.copy(alpha = backgroundAlpha))
            .clickable { onClick() }
            .padding(all = Spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = Spacing.extraSmall)
    ) {
        Icon(
            painter = painterResource(id = CoreR.drawable.ic_proton_users_plus),
            tint = itemColors.majorSecondary.copy(alpha = contentAlpha),
            contentDescription = null
        )

        if (showShareSharedCounter) {
            Text.CaptionMedium(
                modifier = Modifier
                    .background(
                        color = itemColors.majorSecondary.copy(alpha = contentAlpha),
                        shape = CircleShape
                    )
                    .padding(
                        horizontal = Spacing.small,
                        vertical = Spacing.extraSmall
                    ),
                text = shareSharedCount.toString(),
                color = PassTheme.colors.textInvert
            )
        }
    }
}

@[Preview Composable]
internal fun PassShareItemIconPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            PassSharingShareIcon(
                itemCategory = ItemCategory.Login,
                shareSharedCount = 5,
                onClick = {}
            )
        }
    }
}
