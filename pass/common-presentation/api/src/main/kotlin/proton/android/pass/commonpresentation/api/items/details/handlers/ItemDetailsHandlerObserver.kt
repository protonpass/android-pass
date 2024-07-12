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

import kotlinx.coroutines.flow.Flow
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldSection
import proton.android.pass.commonpresentation.api.items.details.domain.ItemDetailsFieldType
import proton.android.pass.commonuimodels.api.items.ItemDetailState
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents

abstract class ItemDetailsHandlerObserver<in ITEM_CONTENTS : ItemContents> {

    abstract fun observe(item: Item): Flow<ItemDetailState>

    abstract fun updateItemContents(
        itemContents: ITEM_CONTENTS,
        hiddenFieldType: ItemDetailsFieldType.Hidden,
        hiddenFieldSection: ItemDetailsFieldSection,
        hiddenState: HiddenState
    ): ItemContents

    protected fun toggleHiddenCustomField(
        customFieldsContent: List<CustomFieldContent>,
        hiddenFieldType: ItemDetailsFieldType.Hidden.CustomField,
        hiddenState: HiddenState
    ): List<CustomFieldContent> = customFieldsContent.mapIndexed { index, customFieldContent ->
        if (index == hiddenFieldType.index && customFieldContent is CustomFieldContent.Hidden) {
            customFieldContent.copy(value = hiddenState)
        } else {
            customFieldContent
        }
    }

}
