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

package proton.android.pass.commonpresentation.api.items.details.handlers

import android.content.Context
import kotlinx.coroutines.flow.Flow
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemCustomFieldSection
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.items.ItemCategory

interface ItemDetailsHandler {

    fun observeItemDetails(item: Item, source: ItemDetailsSource): Flow<ItemDetailState>

    suspend fun onAttachmentOpen(contextHolder: ClassHolder<Context>, attachment: Attachment)

    suspend fun onItemDetailsFieldClicked(text: String, plainFieldType: ItemDetailsFieldType.Plain)

    suspend fun onItemDetailsHiddenFieldClicked(hiddenState: HiddenState, hiddenFieldType: ItemDetailsFieldType.Hidden)

    @Suppress("LongParameterList")
    fun updateItemDetailsContent(
        isVisible: Boolean,
        hiddenState: HiddenState,
        hiddenFieldType: ItemDetailsFieldType.Hidden,
        hiddenFieldSection: ItemCustomFieldSection,
        itemCategory: ItemCategory,
        itemContents: ItemContents
    ): ItemContents

    fun updateItemDetailsDiffs(
        itemCategory: ItemCategory,
        baseItemContents: ItemContents,
        otherItemContents: ItemContents
    ): ItemDiffs

}

enum class ItemDetailsSource {
    DETAIL,
    REVISION
}
