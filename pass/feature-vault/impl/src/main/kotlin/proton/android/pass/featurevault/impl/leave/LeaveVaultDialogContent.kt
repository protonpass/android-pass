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

package proton.android.pass.featurevault.impl.leave

import androidx.compose.material.Surface
import androidx.compose.material.Text
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
import me.proton.core.compose.theme.defaultWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.dialogs.ConfirmWithLoadingDialog
import proton.android.pass.composecomponents.impl.uievents.value
import proton.android.pass.featurevault.impl.R

private const val TAG_VAULT_NAME = "__VAULT_NAME__"

@Composable
fun LeaveVaultDialogContent(
    modifier: Modifier = Modifier,
    state: LeaveVaultUiState,
    onDelete: () -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    val bodyTextResource = stringResource(R.string.vault_leave_dialog_body)
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
        isLoading = state.isLoadingState.value(),
        isConfirmActionDestructive = true,
        title = stringResource(R.string.vault_leave_dialog_title),
        content = {
            Text(
                text = bodyText,
                style = ProtonTheme.typography.defaultWeak,
            )
        },
        confirmText = stringResource(R.string.vault_leave_dialog_leave_action),
        cancelText = stringResource(R.string.vault_leave_dialog_cancel_action),
        onDismiss = onDismiss,
        onConfirm = onDelete,
        onCancel = onCancel
    )
}

class LeaveVaultPreviewProvider :
    ThemePairPreviewProvider<LeaveVaultUiState>(LeaveVaultDialogPreviewProvider())

@Preview
@Composable
fun DeleteVaultDialogContentPreview(
    @PreviewParameter(LeaveVaultPreviewProvider::class) input: Pair<Boolean, LeaveVaultUiState>
) {
    PassTheme(isDark = input.first) {
        Surface {
            LeaveVaultDialogContent(
                state = input.second,
                onDelete = {},
                onCancel = {},
                onDismiss = {}
            )
        }
    }
}
