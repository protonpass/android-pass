/*
 * Copyright (c) 2023-2026 Proton AG
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

package proton.android.pass.autofill.ui.autofill.select

import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.datetime.Clock
import me.proton.core.domain.entity.UserId
import proton.android.pass.autofill.service.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.impl.dialogs.AssociateItemDialog
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType

@Composable
internal fun AssociateAutofillItemDialog(
    modifier: Modifier = Modifier,
    itemUiModel: ItemUiModel?,
    onAssociateAndAutofill: (ItemUiModel) -> Unit,
    onAutofill: (ItemUiModel) -> Unit,
    onDismiss: () -> Unit,
    onCancel: () -> Unit
) {
    itemUiModel ?: return onDismiss()

    AssociateItemDialog(
        modifier = modifier,
        title = stringResource(R.string.autofill_dialog_associate_title),
        message = stringResource(
            R.string.autofill_associate_web_app_name_dialog_title,
            itemUiModel.contents.title
        ),
        confirmLabel = stringResource(R.string.autofill_dialog_associate_and_autofill),
        secondaryLabel = stringResource(R.string.autofill_dialog_just_autofill),
        cancelLabel = stringResource(R.string.autofill_dialog_cancel),
        onConfirm = { onAssociateAndAutofill(itemUiModel) },
        onSecondary = { onAutofill(itemUiModel) },
        onCancel = {
            onCancel()
            onDismiss()
        },
        onDismiss = onDismiss
    )
}

@Preview
@Composable
fun AssociateAutofillItemDialogPreview(@PreviewParameter(ThemePreviewProvider::class) isDark: Boolean) {
    PassTheme(isDark = isDark) {
        Surface {
            AssociateAutofillItemDialog(
                itemUiModel = ItemUiModel(
                    id = ItemId(id = "ferri"),
                    userId = UserId(id = "user-id"),
                    shareId = ShareId(id = "rutrum"),
                    contents = ItemContents.Note(
                        title = "Willie Lowe",
                        note = "repudiandae",
                        customFields = emptyList()
                    ),
                    state = 6128,
                    createTime = Clock.System.now(),
                    modificationTime = Clock.System.now(),
                    lastAutofillTime = null,
                    isPinned = false,
                    revision = 1,
                    pinTime = Clock.System.now(),
                    shareCount = 0,
                    shareType = ShareType.Vault
                ),
                onAssociateAndAutofill = {},
                onAutofill = {},
                onDismiss = {},
                onCancel = {}
            )
        }
    }
}
