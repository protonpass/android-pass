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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import proton.android.pass.composecomponents.impl.item.icon.CreditCardIcon
import proton.android.pass.composecomponents.impl.item.icon.CustomItemIcon
import proton.android.pass.composecomponents.impl.item.icon.IdentityIcon
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.composecomponents.impl.item.icon.NoteIcon
import proton.android.pass.composecomponents.impl.loading.Loading
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.items.ItemCategory
import me.proton.core.presentation.compose.R as CoreR

@Composable
internal fun AttachmentSyncItemGroupRow(
    modifier: Modifier = Modifier,
    item: ItemSyncState,
    onRetryAttachment: (Attachment) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { isExpanded = !isExpanded }
                .padding(vertical = Spacing.extraSmall),
            horizontalArrangement = Arrangement.spacedBy(space = Spacing.small),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemCategoryIcon(itemCategory = item.itemCategory)

            Text.Body2Regular(
                modifier = Modifier.weight(1f),
                text = item.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            when {
                item.hasPending -> Loading(
                    modifier = Modifier.size(16.dp),
                    color = PassTheme.colors.textWeak,
                    strokeWidth = 1.5.dp
                )

                item.hasFailed -> Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(id = CoreR.drawable.ic_proton_cross_circle),
                    tint = PassTheme.colors.signalDanger,
                    contentDescription = null
                )

                item.allDone -> Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(id = CoreR.drawable.ic_proton_checkmark_circle),
                    tint = PassTheme.colors.signalSuccess,
                    contentDescription = null
                )
            }

            Icon(
                modifier = Modifier.size(14.dp),
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

        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(
                modifier = Modifier.padding(start = ITEM_CONTENT_INDENT)
            ) {
                item.attachments.forEach { attachmentRow ->
                    AttachmentSyncItemRow(
                        name = attachmentRow.name,
                        size = attachmentRow.size,
                        status = attachmentRow.status,
                        onRetry = { onRetryAttachment(attachmentRow.attachment) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ItemCategoryIcon(itemCategory: ItemCategory) {
    when (itemCategory) {
        ItemCategory.Login -> LoginIcon()
        ItemCategory.Alias -> AliasIcon()
        ItemCategory.Note -> NoteIcon()
        ItemCategory.CreditCard -> CreditCardIcon()
        ItemCategory.Identity -> IdentityIcon()
        ItemCategory.WifiNetwork,
        ItemCategory.SSHKey,
        ItemCategory.Custom -> CustomItemIcon()
        ItemCategory.Password,
        ItemCategory.Unknown -> Unit
    }
}

private val ITEM_CONTENT_INDENT = 48.dp

@Preview
@Composable
internal fun AttachmentSyncItemGroupRowPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            AttachmentSyncItemGroupRow(
                item = ItemSyncState(
                    itemId = proton.android.pass.domain.ItemId("item-1"),
                    shareId = proton.android.pass.domain.ShareId("share-1"),
                    name = "My Login Item",
                    itemCategory = ItemCategory.Login,
                    attachments = emptyList()
                ),
                onRetryAttachment = {}
            )
        }
    }
}
