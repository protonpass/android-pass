package proton.android.pass.featurevault.impl.delete

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.ProtonAlertDialogText
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.dialogs.ConfirmWithLoadingDialog
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldPlaceHolder
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.value
import proton.android.pass.feature.vault.impl.R

@Composable
fun DeleteVaultDialogContent(
    modifier: Modifier = Modifier,
    state: DeleteVaultUiState,
    onVaultTextChange: (String) -> Unit,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmWithLoadingDialog(
        modifier = modifier,
        show = true,
        isLoading = state.isLoadingState.value(),
        isConfirmActionDestructive = true,
        isConfirmEnabled = state.isButtonEnabled.value(),
        title = stringResource(R.string.vault_delete_dialog_title),
        content = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ProtonAlertDialogText(
                    text = stringResource(
                        id = R.string.vault_delete_dialog_body,
                        formatArgs = arrayOf(state.vaultName)
                    )
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .roundedContainer(ProtonTheme.colors.separatorNorm)
                        .padding(16.dp),
                ) {
                    ProtonTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = state.vaultText,
                        onChange = onVaultTextChange,
                        editable = state.isLoadingState == IsLoadingState.NotLoading,
                        placeholder = {
                            ProtonTextFieldPlaceHolder(
                                text = stringResource(R.string.vault_delete_dialog_placeholder)
                            )
                        },
                        textStyle = ProtonTheme.typography.default
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

class DeleteVaultPreviewProvider :
    ThemePairPreviewProvider<DeleteVaultUiState>(DeleteVaultDialogPreviewProvider())

@Preview
@Composable
fun DeleteVaultDialogContentPreview(
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
