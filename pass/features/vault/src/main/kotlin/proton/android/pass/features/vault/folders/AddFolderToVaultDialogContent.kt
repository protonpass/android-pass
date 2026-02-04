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

package proton.android.pass.features.vault.folders

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.dialogs.ConfirmWithLoadingDialog
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.uievents.value
import proton.android.pass.features.vault.R

@Composable
internal fun AddFolderToVaultDialogContent(
    modifier: Modifier = Modifier,
    state: AddFolderToVaultUiState,
    onVaultTextChange: (String) -> Unit,
    onCreate: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmWithLoadingDialog(
        modifier = modifier,
        show = true,
        isLoading = state.isLoading,
        isConfirmActionDestructive = true,
        isConfirmEnabled = state.isButtonEnabled.value(),
        title = stringResource(R.string.vault_add_folder_dialog_title),
        content = {
            Column(
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                AnimatedContent(
                    targetState = state.showSameFolderExist,
                    transitionSpec = {
                        fadeIn().togetherWith(fadeOut())
                    }
                ) { visible ->
                    if (visible) {
                        Text(text = stringResource(R.string.vault_add_foldersame_folder_exist))
                    } else {
                        Text(
                            text = "",
                            modifier = Modifier.height(0.dp)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .roundedContainerNorm()
                        .padding(Spacing.medium)
                ) {
                    ProtonTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.folderName,
                        onChange = onVaultTextChange,
                        editable = !state.isLoading,
                        placeholder = {
                            ProtonTextFieldPlaceHolder(
                                text = stringResource(R.string.vault_add_folder_dialog_placeholder)
                            )
                        },
                        textStyle = ProtonTheme.typography.defaultNorm
                    )
                }
            }
        },
        confirmText = stringResource(R.string.vault_add_folder_dialog_create_action),
        cancelText = stringResource(R.string.vault_delete_dialog_cancel_action),
        onDismiss = onDismiss,
        onConfirm = onCreate,
        onCancel = onCancel
    )
}


internal class AddFolderToVaultPreviewProvider : ThemePairPreviewProvider<AddFolderToVaultUiState>(
    AddFolderToVaultDialogPreviewProvider()
)

@[Preview Composable]
internal fun DeleteVaultDialogContentPreview(
    @PreviewParameter(AddFolderToVaultPreviewProvider::class)
    input: Pair<Boolean, AddFolderToVaultUiState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            AddFolderToVaultDialogContent(
                state = input.second,
                onVaultTextChange = {},
                onCreate = {},
                onCancel = {},
                onDismiss = {}
            )
        }
    }
}
