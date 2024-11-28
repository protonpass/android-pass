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

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.image.Image
import proton.android.pass.domain.attachments.AttachmentType

@Composable
fun AttachmentImage(modifier: Modifier = Modifier, attachmentType: AttachmentType) {
    val imageId = remember(attachmentType) {
        when (attachmentType) {
            AttachmentType.Audio -> R.drawable.ic_file_type_audio
            AttachmentType.Photo -> R.drawable.ic_file_type_photo
            AttachmentType.RasterImage -> R.drawable.ic_file_type_raster
            AttachmentType.VectorImage -> R.drawable.ic_file_type_vector
            AttachmentType.Text -> R.drawable.ic_file_type_text
            AttachmentType.Video -> R.drawable.ic_file_type_video
            AttachmentType.Key -> R.drawable.ic_file_type_key
            AttachmentType.Calendar -> R.drawable.ic_file_type_calendar
            AttachmentType.Pdf -> R.drawable.ic_file_type_pdf
            AttachmentType.Word -> R.drawable.ic_file_type_word
            AttachmentType.PowerPoint -> R.drawable.ic_file_type_powerpoint
            AttachmentType.Excel -> R.drawable.ic_file_type_excel
            AttachmentType.Document -> R.drawable.ic_file_type_text_alt
            AttachmentType.Unknown -> R.drawable.ic_file_type_unknown
        }
    }
    Image.Default(
        modifier = modifier,
        id = imageId
    )
}

class FileTypePreviewProvider : PreviewParameterProvider<AttachmentType> {
    override val values: Sequence<AttachmentType>
        get() = AttachmentType.entries.asSequence()
}

@Preview
@Composable
fun AttachmentImagePreview(@PreviewParameter(FileTypePreviewProvider::class) attachmentType: AttachmentType) {
    PassTheme(isDark = true) {
        Surface {
            AttachmentImage(attachmentType = attachmentType)
        }
    }
}
