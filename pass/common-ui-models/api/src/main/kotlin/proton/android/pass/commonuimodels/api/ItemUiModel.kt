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

package proton.android.pass.commonuimodels.api

import androidx.compose.runtime.Stable
import kotlinx.datetime.Instant
import me.proton.core.domain.entity.UserId
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.items.ItemCategory

@Stable
data class ItemUiModel(
    val id: ItemId,
    val shareId: ShareId,
    val userId: UserId,
    val contents: ItemContents,
    val state: Int,
    val createTime: Instant,
    val modificationTime: Instant,
    val lastAutofillTime: Instant?,
    val isPinned: Boolean,
    val category: ItemCategory = ItemCategory.Unknown,
    val revision: Long
) {

    val key = "${shareId.id}-${id.id}"

    fun isInTrash() = state == ItemState.Trashed.value

}
