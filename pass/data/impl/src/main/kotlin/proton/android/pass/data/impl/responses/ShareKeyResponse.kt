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
data class GetShareKeysResponse(
    @SerialName("ShareKeys")
    val keys: ShareKeysListResponse
)

@Serializable
data class ShareKeysListResponse(
    @SerialName("Keys")
    val keys: List<ShareKeyResponse>,
    @SerialName("Total")
    val total: Long
)

@Serializable
data class ShareKeyResponse(
    @SerialName("KeyRotation")
    val keyRotation: Long,
    @SerialName("UserKeyID")
    val userKeyId: String,
    @SerialName("Key")
    val key: String,
    @SerialName("CreateTime")
    val createTime: Long
)
