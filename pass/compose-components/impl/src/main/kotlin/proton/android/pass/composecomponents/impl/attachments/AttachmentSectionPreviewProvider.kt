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

package proton.android.pass.composecomponents.impl.attachments

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.datetime.Instant
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.AttachmentId
import proton.android.pass.domain.attachments.AttachmentKey
import proton.android.pass.domain.attachments.AttachmentType

class AttachmentSectionPreviewProvider : PreviewParameterProvider<AttachmentSectionInput> {

    override val values: Sequence<AttachmentSectionInput>
        get() = sequence {
            for (isDetail in listOf(true, false)) {
                yield(
                    createAttachmentSectionInput(
                        files = listOf(
                            createFile(AttachmentId("1"), "file1", AttachmentType.Pdf),
                            createFile(AttachmentId("2"), "file2", AttachmentType.RasterImage)
                        ),
                        isDetail = isDetail
                    )
                )
                yield(
                    createAttachmentSectionInput(
                        files = listOf(
                            createFile(AttachmentId("1"), "file1", AttachmentType.Calendar),
                            createFile(AttachmentId("2"), "file2", AttachmentType.Audio)
                        ),
                        loadingFile = createFile(
                            AttachmentId("1"),
                            "file1",
                            AttachmentType.Calendar
                        ).some(),
                        isDetail = isDetail
                    )
                )
                yield(
                    createAttachmentSectionInput(
                        files = emptyList(),
                        isDetail = isDetail
                    )
                )
            }
        }

    @Suppress("MagicNumber")
    private fun createFile(
        id: AttachmentId,
        name: String,
        type: AttachmentType,
        size: Long = 1_572_864L, // 1.5 MB
        createTime: Instant = Instant.fromEpochSeconds(1_640_000_000L)
    ) = Attachment(
        id = id,
        name = name,
        type = type,
        size = size,
        createTime = createTime,
        mimeType = "",
        fileKey = AttachmentKey(""),
        itemKeyRotation = "",
        chunks = listOf()
    )

    private fun createAttachmentSectionInput(
        files: List<Attachment>,
        loadingFile: Option<Attachment> = None,
        isDetail: Boolean
    ) = AttachmentSectionInput(files = files, loadingFile = loadingFile, isDetail = isDetail)
}

data class AttachmentSectionInput(
    val files: List<Attachment>,
    val loadingFile: Option<Attachment>,
    val isDetail: Boolean
)
