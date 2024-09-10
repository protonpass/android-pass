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

package proton.android.pass.features.report.ui

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.features.report.navigation.ReportNavContentEvent

@Composable
fun ImageAttachedItem(
    modifier: Modifier = Modifier,
    image: Uri,
    onEvent: (ReportNavContentEvent) -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        val context = LocalContext.current
        val fileName = remember { getFileName(context, image) }

        Image(
            modifier = Modifier
                .clip(PassTheme.shapes.squircleMediumShape)
                .size(50.dp),
            painter = rememberAsyncImagePainter(image),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        Text.Body1Regular(modifier = Modifier.weight(1f), text = fileName)

        SmallCrossIconButton { onEvent(ReportNavContentEvent.OnImageRemoved(image)) }
    }
}

private fun getFileName(context: Context, uri: Uri): String {
    var name = "Unknown"
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                name = it.getString(nameIndex)
            }
        }
    }
    return name
}
