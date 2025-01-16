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

package proton.android.pass.features.sharing.invitesinfo

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.composecomponents.impl.dialogs.ConfirmDialog
import proton.android.pass.features.sharing.R
import proton.android.pass.features.sharing.SharingNavigation

@Composable
fun InvitesErrorDialog(onNavigateEvent: (SharingNavigation) -> Unit, modifier: Modifier = Modifier) {
    ConfirmDialog(
        modifier = modifier,
        title = stringResource(id = R.string.sharing_error_dialog_title),
        message = stringResource(id = R.string.sharing_error_dialog_message),
        cancelText = "",
        state = true,
        onConfirm = { },
        onDismiss = { onNavigateEvent(SharingNavigation.CloseScreen) }
    )
}
