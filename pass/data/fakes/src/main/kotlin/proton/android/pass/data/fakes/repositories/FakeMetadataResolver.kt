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

package proton.android.pass.data.fakes.repositories

import kotlinx.datetime.Instant
import proton.android.pass.data.api.repositories.MetadataResolver
import proton.android.pass.domain.attachments.AttachmentType
import proton.android.pass.domain.attachments.FileMetadata
import java.net.URI
import javax.inject.Inject

class FakeMetadataResolver @Inject constructor() : MetadataResolver {

    override suspend fun extractMetadata(uri: URI): FileMetadata = FileMetadata(
        uri = uri,
        name = "",
        size = 0,
        mimeType = "",
        attachmentType = AttachmentType.Audio,
        createTime = Instant.DISTANT_PAST
    )

    override suspend fun extractName(uri: URI): String = uri.toString()
}
