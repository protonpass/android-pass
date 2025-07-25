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
import proton.android.pass.commonuimodels.api.items.DetailEvent
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemDiffs
import proton.android.pass.domain.ItemSection
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.items.ItemCategory

interface ItemDetailsHandler {

    fun observeItemDetails(
        item: Item,
        source: ItemDetailsSource,
        savedStateEntries: Map<String, Any?>
    ): Flow<ItemDetailState>

    suspend fun onAttachmentOpen(contextHolder: ClassHolder<Context>, attachment: Attachment)

    suspend fun onItemDetailsFieldClicked(fieldType: ItemDetailsFieldType)

    @Suppress("LongParameterList")
    fun updateItemDetailsContent(
        revealedHiddenFields: Map<ItemSection, Set<ItemDetailsFieldType.HiddenCopyable>>,
        itemCategory: ItemCategory,
        itemContents: ItemContents
    ): ItemContents

    fun updateItemDetailsDiffs(
        itemCategory: ItemCategory,
        baseItemContents: ItemContents,
        otherItemContents: ItemContents,
        baseAttachments: List<Attachment>,
        otherAttachments: List<Attachment>
    ): ItemDiffs

    fun consumeEvent(event: DetailEvent)
}

enum class ItemDetailsSource {
    DETAIL,
    REVISION
}
