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

package proton.android.pass.data.impl.responses.attachments

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.proton.core.crypto.common.keystore.EncryptedString

@Serializable
data class GetAllFilesResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Files")
    val filesData: FilesDataResponse
)

@Serializable
data class FilesDataResponse(
    @SerialName("Files")
    val files: List<FileDetailsResponse>,
    @SerialName("Total")
    val total: Int,
    @SerialName("LastID")
    val lastId: String?
)

@Serializable
data class FileDetailsResponse(
    @SerialName("FileID")
    val fileId: String,
    @SerialName("Size")
    val size: Long,
    @SerialName("Metadata")
    val metadata: EncryptedString,
    @SerialName("FileKey")
    val fileKey: String,
    @SerialName("ItemKeyRotation")
    val itemKeyRotation: String,
    @SerialName("Chunks")
    val chunks: List<String>,
    @SerialName("CreateTime")
    val createTime: Long
)
