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
import proton.android.pass.commonrust.api.FileType
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.image.Image

@Composable
fun AttachmentImage(modifier: Modifier = Modifier, fileType: FileType) {
    val imageId = remember(fileType) {
        when (fileType) {
            FileType.Audio -> R.drawable.ic_file_type_audio
            FileType.Photo -> R.drawable.ic_file_type_photo
            FileType.RasterImage -> R.drawable.ic_file_type_raster
            FileType.VectorImage -> R.drawable.ic_file_type_vector
            FileType.Text -> R.drawable.ic_file_type_text
            FileType.Video -> R.drawable.ic_file_type_video
            FileType.Key -> R.drawable.ic_file_type_key
            FileType.Calendar -> R.drawable.ic_file_type_calendar
            FileType.Pdf -> R.drawable.ic_file_type_pdf
            FileType.Word -> R.drawable.ic_file_type_word
            FileType.PowerPoint -> R.drawable.ic_file_type_powerpoint
            FileType.Excel -> R.drawable.ic_file_type_excel
            FileType.Document -> R.drawable.ic_file_type_text_alt
            FileType.Unknown -> R.drawable.ic_file_type_unknown
        }
    }
    Image.Default(
        modifier = modifier,
        id = imageId
    )
}

class FileTypePreviewProvider : PreviewParameterProvider<FileType> {
    override val values: Sequence<FileType>
        get() = FileType.entries.asSequence()
}

@Preview
@Composable
fun AttachmentImagePreview(@PreviewParameter(FileTypePreviewProvider::class) filetype: FileType) {
    PassTheme(isDark = true) {
        Surface {
            AttachmentImage(fileType = filetype)
        }
    }
}
