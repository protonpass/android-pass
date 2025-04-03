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

package proton.android.pass.commonuimodels.fakes

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType

object TestItemUiModel {

    fun create(
        id: String = "item-id",
        title: String = "item-title",
        note: String = "item-note",
        itemContents: ItemContents = ItemContents.Note(title, note),
        createTime: Instant = Clock.System.now(),
        modificationTime: Instant = Clock.System.now(),
        lastAutofillTime: Instant? = null,
        isPinned: Boolean = false,
        pinTime: Instant? = null,
        revision: Long = 0,
        shareCount: Int = 0,
        shareType: ShareType = ShareType.Vault
    ): ItemUiModel = ItemUiModel(
        id = ItemId(id = id),
        userId = UserId("user-id"),
        shareId = ShareId(id = "share-id"),
        contents = itemContents,
        createTime = createTime,
        state = 0,
        modificationTime = modificationTime,
        lastAutofillTime = lastAutofillTime,
        isPinned = isPinned,
        pinTime = pinTime,
        revision = revision,
        shareCount = shareCount,
        shareType = shareType
    )

}
