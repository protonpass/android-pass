/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.item.history.restore.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.datetime.Instant
import proton.android.pass.composecomponents.impl.dialogs.ConfirmWithLoadingDialog
import proton.android.pass.composecomponents.impl.utils.passFormattedDateText
import proton.android.pass.features.item.history.R
import me.proton.core.presentation.R as CoreR

@Composable
internal fun ItemHistoryRestoreConfirmationDialog(
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isVisible: Boolean,
    isLoading: Boolean,
    revisionTime: Long
) {
    ConfirmWithLoadingDialog(
        modifier = modifier,
        show = isVisible,
        isLoading = isLoading,
        isConfirmActionDestructive = false,
        title = stringResource(R.string.item_history_restore_confirmation_dialog_title),
        message = stringResource(
            id = R.string.item_history_restore_confirmation_dialog_message,
            passFormattedDateText(endInstant = Instant.fromEpochSeconds(revisionTime))
        ),
        confirmText = stringResource(id = R.string.item_history_restore_action),
        cancelText = stringResource(id = CoreR.string.presentation_alert_cancel),
        onDismiss = onDismiss,
        onCancel = onDismiss,
        onConfirm = onConfirm
    )
}
