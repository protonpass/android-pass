/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.data.fakes.repositories

import proton.android.pass.data.api.repositories.ItemRevision

object ItemRevisionTestFactory {

    fun create(
        itemId: String = "item-id",
        revision: Long = 1L,
        contentFormatVersion: Int = 1,
        keyRotation: Long = 1L,
        content: String = "content",
        itemKey: String? = null,
        state: Int = 1,
        aliasEmail: String? = null,
        createTime: Long = 0L,
        modifyTime: Long = 0L,
        lastUseTime: Long? = null,
        revisionTime: Long = 0L,
        isPinned: Boolean = false,
        pinTime: Long? = null,
        flags: Int = 0,
        shareCount: Int = 0,
        folderId: String? = null
    ): ItemRevision = ItemRevision(
        itemId = itemId,
        revision = revision,
        contentFormatVersion = contentFormatVersion,
        keyRotation = keyRotation,
        content = content,
        itemKey = itemKey,
        state = state,
        aliasEmail = aliasEmail,
        createTime = createTime,
        modifyTime = modifyTime,
        lastUseTime = lastUseTime,
        revisionTime = revisionTime,
        isPinned = isPinned,
        pinTime = pinTime,
        flags = flags,
        shareCount = shareCount,
        folderId = folderId
    )
}
