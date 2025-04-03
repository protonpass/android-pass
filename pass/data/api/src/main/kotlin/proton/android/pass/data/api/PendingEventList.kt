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

package proton.android.pass.data.api

data class PendingEventItemRevision(
    val itemId: String,
    val revision: Long,
    val contentFormatVersion: Int,
    val keyRotation: Long,
    val content: String,
    val key: String?,
    val state: Int,
    val aliasEmail: String?,
    val createTime: Long,
    val modifyTime: Long,
    val lastUseTime: Long?,
    val revisionTime: Long,
    val isPinned: Boolean,
    val pinTime: Long?,
    val flags: Int,
    val shareCount: Int
)

data class PendingEventList(
    val updatedItems: List<PendingEventItemRevision>,
    val deletedItemIds: List<String>
)
