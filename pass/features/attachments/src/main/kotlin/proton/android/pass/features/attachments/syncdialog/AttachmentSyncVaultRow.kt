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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.extension.toColor
import proton.android.pass.composecomponents.impl.extension.toResource
import proton.android.pass.composecomponents.impl.icon.VaultIcon
import proton.android.pass.composecomponents.impl.loading.Loading
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.features.attachments.R
import me.proton.core.presentation.compose.R as CoreR

@Composable
internal fun AttachmentSyncVaultRow(
    modifier: Modifier = Modifier,
    vault: VaultSyncState,
    onRetryAttachment: (Attachment) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = Spacing.small),
            horizontalArrangement = Arrangement.spacedBy(space = Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            VaultIcon(
                backgroundColor = vault.color.toColor(isBackground = true),
                icon = vault.icon.toResource(),
                iconColor = vault.color.toColor()
            )

            Column(modifier = Modifier.weight(1f)) {
                Text.Body2Regular(
                    text = vault.totalSizeBytes
                        ?.let { "${vault.name} (${formatFileSize(it)})" }
                        ?: vault.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text.Body3Weak(
                    text = stringResource(
                        R.string.attachment_sync_vault_files_count,
                        vault.downloadedCount,
                        vault.totalCount
                    )
                )
            }

            when {
                vault.hasPending -> Loading(
                    modifier = Modifier.size(20.dp),
                    color = PassTheme.colors.interactionNormMajor1,
                    strokeWidth = 2.dp
                )

                vault.hasFailed -> Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(id = CoreR.drawable.ic_proton_cross_circle),
                    tint = PassTheme.colors.signalDanger,
                    contentDescription = null
                )

                vault.allDone -> Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(id = CoreR.drawable.ic_proton_checkmark_circle),
                    tint = PassTheme.colors.signalSuccess,
                    contentDescription = null
                )
            }

            Icon(
                modifier = Modifier.size(16.dp),
                painter = painterResource(
                    id = if (isExpanded) {
                        CoreR.drawable.ic_proton_chevron_up
                    } else {
                        CoreR.drawable.ic_proton_chevron_down
                    }
                ),
                tint = PassTheme.colors.textWeak,
                contentDescription = null
            )
        }

        // Display Items (Logins, Alias...)
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(start = 40.dp)
            ) {
                vault.items.forEach { item ->
                    AttachmentSyncItemGroupRow(
                        item = item,
                        onRetryAttachment = onRetryAttachment
                    )
                }
            }
        }
    }
}

@Preview
@Composable
internal fun AttachmentSyncVaultRowPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            AttachmentSyncVaultRow(
                vault = VaultSyncState(
                    shareId = proton.android.pass.domain.ShareId("share-1"),
                    name = "Personal Vault",
                    color = ShareColor.Color1,
                    icon = ShareIcon.Icon1,
                    items = emptyList()
                ),
                onRetryAttachment = {}
            )
        }
    }
}
