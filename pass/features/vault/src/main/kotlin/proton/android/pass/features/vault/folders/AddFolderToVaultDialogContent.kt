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

package proton.android.pass.features.vault.folders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.RequestFocusLaunchedEffect
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
    val title = if (state.isEditMode) {
        stringResource(R.string.vault_edit_folder_dialog_title)
    } else {
        stringResource(R.string.vault_add_folder_dialog_title)
    }

    val confirmText = if (state.isEditMode) {
        stringResource(R.string.vault_edit_folder_dialog_save_action)
    } else {
        stringResource(R.string.vault_add_folder_dialog_create_action)
    }

    ConfirmWithLoadingDialog(
        modifier = modifier,
        show = true,
        isLoading = state.isLoading,
        isConfirmActionDestructive = false,
        isConfirmEnabled = state.isButtonEnabled.value(),
        title = title,
        content = {
            val focusRequester = remember { FocusRequester() }

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
                // Material2 AlertDialog uses baseline-based layout (AlertDialogBaselineLayout).
                // It positions the text slot by aligning its first text baseline at a fixed
                // distance from the title baseline. A non-text element (like a Box or Spacer)
                // has no baseline, so the layout would fall back to the TextField's baseline,
                // shifting the entire content block upward and eliminating the gap.
                // This zero-height Text anchors the baseline at y=0 so the layout places the
                // content block top at the correct distance from the title, and spacedBy then
                // adds the visual gap before the input field.
                Text(text = "", modifier = Modifier.height(0.dp))
                Box(
                    modifier = Modifier
                        .roundedContainerNorm()
                        .padding(Spacing.medium)
                ) {
                    ProtonTextField(
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        value = state.folderName,
                        onChange = onVaultTextChange,
                        editable = !state.isLoading,
                        placeholder = {
                            ProtonTextFieldPlaceHolder(
                                text = stringResource(R.string.vault_add_folder_dialog_placeholder)
                            )
                        },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        textStyle = ProtonTheme.typography.defaultNorm
                    )
                }
            }

            RequestFocusLaunchedEffect(focusRequester)
        },
        confirmText = confirmText,
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
