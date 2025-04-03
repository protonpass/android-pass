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

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.data.api.usecases.ItemActions
import proton.android.pass.data.api.usecases.capabilities.CanShareShareStatus
import proton.android.pass.domain.items.ItemCategory

class ItemDetailTopBarPreviewProvider : PreviewParameterProvider<ItemDetailTopBarPreview> {
    override val values: Sequence<ItemDetailTopBarPreview>
        get() = sequence {
            for (isLoading in listOf(true, false)) {
                val entries = ItemCategory.entries.filter {
                    it != ItemCategory.Unknown && it != ItemCategory.Password
                }
                for (itemCategory in entries) {
                    yield(
                        ItemDetailTopBarPreview(
                            isLoading = isLoading,
                            itemCategory = itemCategory,
                            actions = ItemActions(
                                canShare = CanShareShareStatus.CanShare(1),
                                canEdit = ItemActions.CanEditActionState.Enabled,
                                canMoveToOtherVault = ItemActions.CanMoveToOtherVaultState.Enabled,
                                canMoveToTrash = true,
                                canRestoreFromTrash = false,
                                canDelete = false,
                                canUseOptions = true
                            )
                        )
                    )
                }
            }
            yield(
                ItemDetailTopBarPreview(
                    isLoading = false,
                    itemCategory = ItemCategory.Login,
                    actions = ItemActions.Disabled
                )
            )
        }
}

data class ItemDetailTopBarPreview(
    val isLoading: Boolean,
    val itemCategory: ItemCategory,
    val actions: ItemActions
)
