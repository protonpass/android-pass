/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.vault.bottomsheet.folders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.dialogs.ConfirmWithLoadingDialog
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.composecomponents.impl.uievents.value
import proton.android.pass.features.vault.R

private const val TAG_FOLDER_NAME = "__FOLDER_NAME__"

@Composable
internal fun DeleteFolderDialogContent(
    modifier: Modifier = Modifier,
    state: DeleteFolderUiState,
    onFolderTextChange: (String) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    val bodyTextResource = stringResource(R.string.folder_delete_dialog_body)
    val bodyText = buildAnnotatedString {
        val textParts = bodyTextResource.split(TAG_FOLDER_NAME)
        if (textParts.size == 2) {
            append(textParts[0])
            append(AnnotatedString(state.folderName, SpanStyle(fontWeight = FontWeight.Bold)))
            append(textParts[1])
        } else {
            append(bodyTextResource)
        }
    }

    ConfirmWithLoadingDialog(
        modifier = modifier,
        show = true,
        isLoading = state.isLoading,
        isConfirmActionDestructive = true,
        isConfirmEnabled = state.isButtonEnabled.value(),
        title = stringResource(R.string.folder_delete_dialog_title),
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                Text.Body1Regular(
                    annotatedText = bodyText
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .roundedContainerNorm()
                        .padding(Spacing.medium)
                ) {
                    ProtonTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.folderText,
                        onChange = onFolderTextChange,
                        editable = !state.isLoading,
                        placeholder = {
                            ProtonTextFieldPlaceHolder(
                                text = stringResource(R.string.folder_delete_dialog_placeholder)
                            )
                        },
                        textStyle = ProtonTheme.typography.defaultNorm
                    )
                }
            }
        },
        confirmText = stringResource(R.string.folder_delete_dialog_delete_action),
        cancelText = stringResource(R.string.vault_delete_dialog_cancel_action),
        onDismiss = onDismiss,
        onConfirm = onDelete,
        onCancel = onCancel
    )
}

internal class DeleteFolderPreviewProvider : ThemePairPreviewProvider<DeleteFolderUiState>(
    DeleteFolderDialogPreviewProvider()
)

@[Preview Composable]
internal fun DeleteFolderDialogContentPreview(
    @PreviewParameter(DeleteFolderPreviewProvider::class) input: Pair<Boolean, DeleteFolderUiState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            DeleteFolderDialogContent(
                state = input.second,
                onFolderTextChange = {},
                onDelete = {},
                onCancel = {},
                onDismiss = {}
            )
        }
    }
}
