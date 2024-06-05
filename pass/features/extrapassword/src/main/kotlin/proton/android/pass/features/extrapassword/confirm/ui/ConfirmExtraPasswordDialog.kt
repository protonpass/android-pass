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

package proton.android.pass.features.extrapassword.confirm.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import proton.android.pass.composecomponents.impl.dialogs.ConfirmWithLoadingDialog
import proton.android.pass.features.extrapassword.ExtraPasswordNavigation
import proton.android.pass.features.extrapassword.R
import proton.android.pass.features.extrapassword.confirm.presentation.ConfirmExtraPasswordContentEvent
import proton.android.pass.features.extrapassword.confirm.presentation.ConfirmExtraPasswordViewModel
import proton.android.pass.composecomponents.impl.R as compR

@Composable
fun ConfirmExtraPasswordDialog(
    modifier: Modifier = Modifier,
    onNavigate: (ExtraPasswordNavigation) -> Unit,
    viewmodel: ConfirmExtraPasswordViewModel = hiltViewModel()
) {

    val state by viewmodel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.event) {
        when (state.event) {
            ConfirmExtraPasswordContentEvent.Idle -> {}
            ConfirmExtraPasswordContentEvent.Success -> onNavigate(ExtraPasswordNavigation.FinishedConfiguring)
        }
        viewmodel.onEventConsumed(state.event)
    }

    ConfirmWithLoadingDialog(
        show = true,
        isLoading = state.isLoading.value(),
        isConfirmActionDestructive = false,
        title = stringResource(R.string.confirm_extra_password_title),
        message = stringResource(R.string.confirm_extra_password_body),
        confirmText = stringResource(id = compR.string.action_confirm),
        cancelText = stringResource(id = compR.string.action_cancel),
        onDismiss = { onNavigate(ExtraPasswordNavigation.FinishedConfiguring) },
        onConfirm = { viewmodel.submit() },
        onCancel = { onNavigate(ExtraPasswordNavigation.FinishedConfiguring) }
    )
}
