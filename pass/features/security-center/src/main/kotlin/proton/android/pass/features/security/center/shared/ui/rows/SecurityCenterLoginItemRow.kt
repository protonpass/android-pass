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

package proton.android.pass.features.security.center.shared.ui.rows

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.extension.toSmallResource
import proton.android.pass.composecomponents.impl.item.LoginRow
import proton.android.pass.domain.ShareIcon

@Composable
internal fun SecurityCenterLoginItemRow(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel,
    canLoadExternalImages: Boolean,
    shareIcon: ShareIcon? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(
                horizontal = Spacing.medium,
                vertical = Spacing.small
            )
    ) {
        LoginRow(
            item = itemUiModel,
            canLoadExternalImages = canLoadExternalImages,
            vaultIcon = shareIcon?.toSmallResource()
        )
    }

}
