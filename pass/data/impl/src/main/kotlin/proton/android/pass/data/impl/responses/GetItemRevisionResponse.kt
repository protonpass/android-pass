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

package proton.android.pass.data.impl.responses

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GetItemRevisionResponse(
    @SerialName("Revisions") val revisions: Revisions,
)

@Serializable
data class Revisions(
    @SerialName("RevisionsData") val revisionsData: List<RevisionsData>,
    @SerialName("Total") val total: Int,
    @SerialName("LastToken") val lastToken: String? = null,
)

@Serializable
data class RevisionsData(
    @SerialName("ItemID") val itemID: String,
    @SerialName("Revision") val revision: Int,
    @SerialName("ContentFormatVersion") val contentFormatVersion: Int,
    @SerialName("KeyRotation") val keyRotation: String,
    @SerialName("Content") val content: String,
    @SerialName("ItemKey") val itemKey: String? = null,
    @SerialName("State") val state: Int,
    @SerialName("Pinned") val pinned: Boolean,
    @SerialName("AliasEmail") val aliasEmail: String? = null,
    @SerialName("CreateTime") val createTime: Int,
    @SerialName("ModifyTime") val modifyTime: Int,
    @SerialName("LastUseTime") val lastUseTime: Int? = null,
    @SerialName("RevisionTime") val revisionTime: Int,
)
