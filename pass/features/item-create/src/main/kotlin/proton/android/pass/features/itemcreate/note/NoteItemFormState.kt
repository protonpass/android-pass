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

package proton.android.pass.features.itemcreate.note

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import proton.android.pass.domain.ItemContents
import proton.android.pass.features.itemcreate.common.UICustomFieldContent

@Parcelize
@Immutable
data class NoteItemFormState(
    val title: String,
    val note: String,
    val customFields: List<UICustomFieldContent>
) : Parcelable {

    constructor(itemContents: ItemContents.Note) : this(
        title = itemContents.title,
        note = itemContents.note,
        customFields = itemContents.customFields.map(UICustomFieldContent.Companion::from)
    )

    fun validate(): Set<NoteItemValidationErrors> {
        val mutableSet = mutableSetOf<NoteItemValidationErrors>()
        if (title.isBlank()) mutableSet.add(NoteItemValidationErrors.BlankTitle)
        return mutableSet.toSet()
    }

    fun toItemContents(): ItemContents = ItemContents.Note(
        title = title,
        note = note,
        customFields = customFields.map(UICustomFieldContent::toCustomFieldContent)
    )

    companion object {
        val Empty = NoteItemFormState(
            title = "",
            note = "",
            customFields = emptyList()
        )
    }
}

sealed interface NoteItemValidationErrors {
    data object BlankTitle : NoteItemValidationErrors
}
