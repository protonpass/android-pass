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

package proton.android.pass.data.impl.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MoveItemsToFolderResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Items")
    val items: List<MoveItemRevisionApiModel>
)

@Serializable
data class MoveItemRevisionApiModel(
    @SerialName("ItemID")
    val itemId: String,
    @SerialName("Revision")
    val revision: Long,
    @SerialName("State")
    val state: Int,
    @SerialName("Flags")
    val flags: Int,
    @SerialName("FolderID")
    val folderId: String?,
    @SerialName("ModifyTime")
    val modifyTime: Long,
    @SerialName("RevisionTime")
    val revisionTime: Long
)

