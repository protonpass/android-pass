/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.features.attachments.syncdialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.loading.Loading
import proton.android.pass.domain.attachments.AttachmentDownloadStatus
import proton.android.pass.features.attachments.R
import java.util.Locale
import me.proton.core.presentation.compose.R as CoreR

@Composable
internal fun AttachmentSyncItemRow(
    modifier: Modifier = Modifier,
    name: String,
    size: Long,
    status: AttachmentDownloadStatus,
    onRetry: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.extraSmall),
        horizontalArrangement = Arrangement.spacedBy(space = Spacing.small),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f, fill = false)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text.Body2Regular(
                    modifier = Modifier.weight(1f, fill = false),
                    text = name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                when (status) {
                    AttachmentDownloadStatus.Downloaded -> {
                        Icon(
                            modifier = Modifier.size(16.dp),
                            painter = painterResource(
                                id = CoreR.drawable.ic_proton_checkmark_circle
                            ),
                            tint = PassTheme.colors.signalSuccess,
                            contentDescription = null
                        )
                    }

                    AttachmentDownloadStatus.Failed -> {
                        IconButton(onClick = onRetry) {
                            Icon(
                                painter = painterResource(
                                    id = CoreR.drawable.ic_proton_arrow_rotate_right
                                ),
                                tint = PassTheme.colors.signalDanger,
                                contentDescription = stringResource(
                                    id = R.string.attachment_sync_dialog_retry_content_description
                                )
                            )
                        }
                    }

                    AttachmentDownloadStatus.Pending,
                    AttachmentDownloadStatus.Downloading -> {
                        Loading(
                            modifier = Modifier.size(16.dp),
                            color = PassTheme.colors.textWeak,
                            strokeWidth = 1.5.dp
                        )
                    }

                    AttachmentDownloadStatus.Idle,
                    AttachmentDownloadStatus.Paused -> {}
                }
            }

            Text.Body3Weak(
                text = formatFileSize(size)
            )
        }
    }
}

@Suppress("MagicNumber")
internal fun formatFileSize(sizeInBytes: Long): String {
    val kb = 1024.0
    val mb = kb * 1024
    val gb = mb * 1024
    return when {
        sizeInBytes >= gb -> String.format(Locale.US, "%.1f GB", sizeInBytes / gb)
        sizeInBytes >= mb -> String.format(Locale.US, "%.1f MB", sizeInBytes / mb)
        sizeInBytes >= kb -> String.format(Locale.US, "%.1f KB", sizeInBytes / kb)
        else -> "$sizeInBytes B"
    }
}

@Preview
@Composable
internal fun AttachmentSyncItemRowPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            AttachmentSyncItemRow(
                name = "document.pdf",
                size = 1_048_576,
                status = AttachmentDownloadStatus.Failed,
                onRetry = {}
            )
        }
    }
}
