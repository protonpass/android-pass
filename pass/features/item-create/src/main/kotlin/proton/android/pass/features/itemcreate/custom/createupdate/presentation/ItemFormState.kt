/*
 * Copyright (c) 2023-2024 Proton AG
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

package proton.android.pass.features.itemcreate.custom.createupdate.presentation

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import proton.android.pass.domain.ExtraSectionContent
import proton.android.pass.domain.ItemContents
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UIExtraSection

@Parcelize
@Immutable
data class ItemFormState(
    val title: String,
    val customFieldList: List<UICustomFieldContent>,
    val sectionList: List<UIExtraSection>
) : Parcelable {

    constructor(itemContents: ItemContents.Custom) : this(
        title = itemContents.title,
        customFieldList = itemContents.customFieldList.map(UICustomFieldContent.Companion::from),
        sectionList = itemContents.sectionContentList.map(::UIExtraSection)
    )

    fun validate(): Set<ItemValidationErrors> {
        val mutableSet = mutableSetOf<ItemValidationErrors>()
        if (title.isBlank()) mutableSet.add(ItemValidationErrors.BlankTitle)
        return mutableSet.toSet()
    }

    fun toItemContents(): ItemContents = ItemContents.Custom(
        title = title,
        note = "",
        customFieldList = customFieldList.map(UICustomFieldContent::toCustomFieldContent),
        sectionContentList = sectionList.map {
            ExtraSectionContent(
                title = it.title,
                customFieldList = it.customFields.map(UICustomFieldContent::toCustomFieldContent)
            )
        }
    )

    companion object {
        val EMPTY = ItemFormState(
            title = "",
            customFieldList = emptyList(),
            sectionList = emptyList()
        )
    }
}

sealed interface ItemValidationErrors {
    data object BlankTitle : ItemValidationErrors
}
