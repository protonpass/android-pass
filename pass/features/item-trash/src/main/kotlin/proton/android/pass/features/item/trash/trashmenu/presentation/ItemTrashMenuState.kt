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

package proton.android.pass.features.item.trash.trashmenu.presentation

import androidx.compose.runtime.Stable
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemAction
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.items.ItemCategory

@Stable
internal data class ItemTrashMenuState(
    internal val action: BottomSheetItemAction,
    internal val event: ItemTrashMenuEvent,
    internal val canLoadExternalImages: Boolean,
    private val itemUiModelOption: Option<ItemUiModel>
) {

    internal val itemTitle: String = when (itemUiModelOption) {
        None -> ""
        is Some -> itemUiModelOption.value.contents.title
    }

    internal val itemSubtitle: String = when (itemUiModelOption) {
        None -> ""
        is Some -> when (val itemContents = itemUiModelOption.value.contents) {
            is ItemContents.Alias -> itemContents.aliasEmail
            is ItemContents.Login -> itemContents.displayValue
            is ItemContents.Note -> itemContents.note.replace("\n", " ")
            is ItemContents.CreditCard,
            is ItemContents.Identity,
            is ItemContents.Custom,
            is ItemContents.Unknown -> ""
        }
    }

    internal val itemCategory: ItemCategory = when (itemUiModelOption) {
        None -> ItemCategory.Unknown
        is Some -> itemUiModelOption.value.category
    }

    internal val itemWebsite: String = when (itemUiModelOption) {
        None -> ""
        is Some -> when (val itemContents = itemUiModelOption.value.contents) {
            is ItemContents.Login -> itemContents.websiteUrl.orEmpty()
            is ItemContents.Alias,
            is ItemContents.Note,
            is ItemContents.CreditCard,
            is ItemContents.Identity,
            is ItemContents.Custom,
            is ItemContents.Unknown -> ""
        }
    }

    internal val itemPackageName: String = when (itemUiModelOption) {
        None -> ""
        is Some -> when (val itemContents = itemUiModelOption.value.contents) {
            is ItemContents.Login -> itemContents.packageName.orEmpty()
            is ItemContents.Alias,
            is ItemContents.Note,
            is ItemContents.CreditCard,
            is ItemContents.Identity,
            is ItemContents.Custom,
            is ItemContents.Unknown -> ""
        }
    }

    internal val canLeaveItem: Boolean = when (itemUiModelOption) {
        None -> false
        is Some -> itemUiModelOption.value.isSharedWithMe
    }

    internal companion object {

        internal val Initial: ItemTrashMenuState = ItemTrashMenuState(
            action = BottomSheetItemAction.None,
            event = ItemTrashMenuEvent.Idle,
            canLoadExternalImages = false,
            itemUiModelOption = None
        )

    }

}
