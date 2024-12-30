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

package proton.android.pass.data.impl.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemRevisionResponse(
    @SerialName("Code") val code: Int,
    @SerialName("Item") val item: ItemRevisionApiModel
)

@Serializable
data class GetItemsResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Items")
    val items: ItemsListApiModel
)

@Serializable
data class ItemsListApiModel(
    @SerialName("Total")
    val total: Long,
    @SerialName("RevisionsData")
    val revisions: List<ItemRevisionApiModel>,
    @SerialName("LastToken")
    val lastToken: String?
)

@Serializable
data class ItemRevisionApiModel(
    @SerialName("ItemID")
    val itemId: String,
    @SerialName("Revision")
    val revision: Long,
    @SerialName("ContentFormatVersion")
    val contentFormatVersion: Int,
    @SerialName("KeyRotation")
    val keyRotation: Long,
    @SerialName("Content")
    val content: String,
    @SerialName("ItemKey")
    val itemKey: String?,
    @SerialName("State")
    val state: Int,
    @SerialName("AliasEmail")
    val aliasEmail: String?,
    @SerialName("CreateTime")
    val createTime: Long,
    @SerialName("ModifyTime")
    val modifyTime: Long,
    @SerialName("LastUseTime")
    val lastUseTime: Long?,
    @SerialName("RevisionTime")
    val revisionTime: Long,
    @SerialName("Pinned")
    val isPinned: Boolean,
    @SerialName("Flags")
    val flags: Int,
    @SerialName("ShareCount")
    val shareCount: Int
)
