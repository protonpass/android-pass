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

package proton.android.pass.data.impl.usecases.attachments

import proton.android.pass.data.api.repositories.DraftAttachmentRepository
import proton.android.pass.data.api.usecases.attachments.ClearAttachments
import proton.android.pass.data.impl.repositories.FileKeyRepository
import proton.android.pass.files.api.CacheCleaner
import proton.android.pass.files.api.DirectoryType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClearAttachmentsImpl @Inject constructor(
    private val fileKeyRepository: FileKeyRepository,
    private val draftAttachmentRepository: DraftAttachmentRepository,
    private val cacheCleaner: CacheCleaner
) : ClearAttachments {
    override suspend fun invoke() {
        draftAttachmentRepository.clear()
        cacheCleaner.deleteDir(DirectoryType.CameraTemp)
        fileKeyRepository.clearAllMappings()
    }
}
