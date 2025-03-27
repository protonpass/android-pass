/*
 * Copyright (c) 2023 Proton AG
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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.datetime.Clock
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import me.proton.core.compose.theme.headlineNorm
import me.proton.core.domain.entity.UserId
import proton.android.pass.autofill.service.R
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.dialogs.DialogButton
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

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(backgroundColor = PassTheme.colors.backgroundNorm) {
            Column(
                modifier = modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(R.string.autofill_dialog_associate_title),
                    style = ProtonTheme.typography.headlineNorm
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = stringResource(
                        R.string.autofill_associate_web_app_name_dialog_title,
                        itemUiModel.contents.title
                    ),
                    style = ProtonTheme.typography.defaultNorm
                )
                DialogButton(
                    modifier = Modifier.align(Alignment.End),
                    text = stringResource(R.string.autofill_dialog_associate_and_autofill),
                    onClick = { onAssociateAndAutofill(itemUiModel) }
                )
                DialogButton(
                    modifier = Modifier.align(Alignment.End),
                    text = stringResource(R.string.autofill_dialog_just_autofill),
                    onClick = { onAutofill(itemUiModel) }
                )
                DialogButton(
                    modifier = Modifier.align(Alignment.End),
                    text = stringResource(R.string.autofill_dialog_cancel),
                    onClick = {
                        onCancel()
                        onDismiss()
                    }
                )
            }
        }
    }
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
                        note = "repudiandae"
                    ),
                    state = 6128,
                    createTime = Clock.System.now(),
                    modificationTime = Clock.System.now(),
                    lastAutofillTime = null,
                    isPinned = false,
                    revision = 1,
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
