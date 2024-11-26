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
import proton.android.pass.commonui.api.FileType
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.image.Image
import me.proton.core.presentation.R as CoreR

@Composable
fun AttachmentImage(modifier: Modifier = Modifier, fileType: FileType) {
    val imageId = remember(fileType) {
        when (fileType) {
            FileType.Audio -> CoreR.drawable.ic_proton_file_type_audio_24
            FileType.Code -> R.drawable.ic_file_type_code
            FileType.Compressed -> CoreR.drawable.ic_proton_file_type_zip_24
            FileType.Executable -> R.drawable.ic_file_type_executable
            FileType.Font -> R.drawable.ic_file_type_font
            FileType.Link -> R.drawable.ic_file_type_link
            FileType.Locked -> R.drawable.ic_file_type_locked
            FileType.Movie -> R.drawable.ic_file_type_movie
            FileType.Music -> R.drawable.ic_file_type_music
            FileType.Photo -> R.drawable.ic_file_type_photo
            FileType.RasterImage -> CoreR.drawable.ic_proton_file_type_image_24
            FileType.Text -> CoreR.drawable.ic_proton_file_type_text_24
            FileType.ThreeDImage -> R.drawable.ic_file_type_3d
            FileType.VectorImage -> R.drawable.ic_file_type_vector
            FileType.Video -> CoreR.drawable.ic_proton_file_type_video_24
            FileType.Unknown -> CoreR.drawable.ic_proton_file_type_unknown_24
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
