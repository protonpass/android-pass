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

package proton.android.pass.ui

import androidx.compose.runtime.Immutable
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.domain.inappmessages.InAppMessage
import proton.android.pass.inappupdates.api.InAppUpdateState
import proton.android.pass.network.api.NetworkStatus
import proton.android.pass.notifications.api.SnackbarMessage

@Immutable
data class AppUiState(
    val snackbarMessage: Option<SnackbarMessage>,
    val networkStatus: NetworkStatus,
    val inAppUpdateState: InAppUpdateState,
    val inAppMessage: Option<InAppMessage>
) {

    companion object {
        val Initial = AppUiState(
            snackbarMessage = None,
            networkStatus = NetworkStatus.Online,
            inAppUpdateState = InAppUpdateState.Idle,
            inAppMessage = None
        )
    }
}
