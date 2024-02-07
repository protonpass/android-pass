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
import proton.android.pass.data.api.repositories.ItemRevision

@Serializable
data class ItemRevisionResponse(
    @SerialName("Code") val code: Int,
    @SerialName("Item") val item: ItemRevision,
)

@Serializable
data class GetItemsResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Items")
    val items: ItemsList
)

@Serializable
data class ItemsList(
    @SerialName("Total")
    val total: Long,
    @SerialName("RevisionsData")
    val revisions: List<ItemRevisionImpl>,
    @SerialName("LastToken")
    val lastToken: String?
)

@Serializable
data class ItemRevisionImpl(
    @SerialName("ItemID")
    override val itemId: String,
    @SerialName("Revision")
    override val revision: Long,
    @SerialName("ContentFormatVersion")
    override val contentFormatVersion: Int,
    @SerialName("KeyRotation")
    override val keyRotation: Long,
    @SerialName("Content")
    override val content: String,
    @SerialName("ItemKey")
    override val itemKey: String?,
    @SerialName("State")
    override val state: Int,
    @SerialName("AliasEmail")
    override val aliasEmail: String?,
    @SerialName("CreateTime")
    override val createTime: Long,
    @SerialName("ModifyTime")
    override val modifyTime: Long,
    @SerialName("LastUseTime")
    override val lastUseTime: Long?,
    @SerialName("RevisionTime")
    override val revisionTime: Long,
    @SerialName("Pinned")
    override val isPinned: Boolean,
) : ItemRevision
