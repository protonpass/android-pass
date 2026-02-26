/*
 * Copyright (c) 2025-2026 Proton AG
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
data class GetFoldersResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Folders")
    val folders: FoldersListApiModel
)

@Serializable
data class FoldersListApiModel(
    @SerialName("Total")
    val total: Long,
    @SerialName("Folders")
    val folders: List<FolderApiModel>,
    @SerialName("LastToken")
    val lastToken: String?,
    @SerialName("Code")
    val code: Int
)

@Serializable
data class FolderApiModel(
    @SerialName("VaultID")
    val vaultId: String,
    @SerialName("FolderID")
    val folderId: String,
    @SerialName("ParentFolderID")
    val parentFolderId: String?,
    @SerialName("KeyRotation")
    val keyRotation: Long,
    @SerialName("FolderKey")
    val folderKey: String,
    @SerialName("ContentFormatVersion")
    val contentFormatVersion: Int,
    @SerialName("Content")
    val content: String
)

@Serializable
data class FolderGetResponse(
    @SerialName("Code")
    val code: Int,
    @SerialName("Folder")
    val folder: FolderApiModel
)
