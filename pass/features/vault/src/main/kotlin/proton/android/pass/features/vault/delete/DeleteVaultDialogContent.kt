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

package proton.android.pass.features.vault.delete

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
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
import proton.android.pass.composecomponents.impl.container.PassInfoWarningBanner
import proton.android.pass.composecomponents.impl.container.roundedContainerNorm
import proton.android.pass.composecomponents.impl.dialogs.ConfirmWithLoadingDialog
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.text.Text
import proton.android.pass.composecomponents.impl.uievents.value
import proton.android.pass.features.vault.R
import proton.android.pass.composecomponents.impl.R as CompR

private const val TAG_VAULT_NAME = "__VAULT_NAME__"

@Composable
internal fun DeleteVaultDialogContent(
    modifier: Modifier = Modifier,
    state: DeleteVaultUiState,
    onVaultTextChange: (String) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    val bodyTextResource = stringResource(R.string.vault_delete_dialog_body)
    val bodyText = buildAnnotatedString {
        val textParts = bodyTextResource.split(TAG_VAULT_NAME)
        if (textParts.size == 2) {
            append(textParts[0])
            append(AnnotatedString(state.vaultName, SpanStyle(fontWeight = FontWeight.Bold)))
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
        title = stringResource(R.string.vault_delete_dialog_title),
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                Text.Body1Regular(
                    annotatedText = bodyText
                )

                if (state.showSharedItemsWarning) {
                    PassInfoWarningBanner(
                        backgroundColor = PassTheme.colors.interactionNormMinor2,
                        text = stringResource(
                            id = R.string.vault_delete_dialog_shared_items_warning,
                            pluralStringResource(
                                id = CompR.plurals.shared_items_count,
                                count = state.sharedItemsCount,
                                state.sharedItemsCount
                            )
                        )
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .roundedContainerNorm()
                        .padding(Spacing.medium)
                ) {
                    ProtonTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.vaultText,
                        onChange = onVaultTextChange,
                        editable = !state.isLoading,
                        placeholder = {
                            ProtonTextFieldPlaceHolder(
                                text = stringResource(R.string.vault_delete_dialog_placeholder)
                            )
                        },
                        textStyle = ProtonTheme.typography.defaultNorm
                    )
                }
            }
        },
        confirmText = stringResource(R.string.vault_delete_dialog_delete_action),
        cancelText = stringResource(R.string.vault_delete_dialog_cancel_action),
        onDismiss = onDismiss,
        onConfirm = onDelete,
        onCancel = onCancel
    )
}

internal class DeleteVaultPreviewProvider : ThemePairPreviewProvider<DeleteVaultUiState>(
    DeleteVaultDialogPreviewProvider()
)

@[Preview Composable]
internal fun DeleteVaultDialogContentPreview(
    @PreviewParameter(DeleteVaultPreviewProvider::class) input: Pair<Boolean, DeleteVaultUiState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            DeleteVaultDialogContent(
                state = input.second,
                onVaultTextChange = {},
                onDelete = {},
                onCancel = {},
                onDismiss = {}
            )
        }
    }
}
