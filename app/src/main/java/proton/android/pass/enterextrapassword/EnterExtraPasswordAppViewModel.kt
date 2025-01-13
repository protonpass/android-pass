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

package proton.android.pass.enterextrapassword

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.network.api.NetworkMonitor
import proton.android.pass.network.api.NetworkStatus
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.notifications.api.SnackbarMessage
import javax.inject.Inject

@Stable
data class EnterExtraPasswordAppState(
    val snackbarMessage: Option<SnackbarMessage>,
    val networkStatus: NetworkStatus
) {
    companion object {
        val Initial = EnterExtraPasswordAppState(
            snackbarMessage = None,
            networkStatus = NetworkStatus.Online
        )
    }
}

@HiltViewModel
class EnterExtraPasswordAppViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    networkMonitor: NetworkMonitor
) : ViewModel() {

    private val networkStatus: Flow<NetworkStatus> = networkMonitor
        .connectivity
        .distinctUntilChanged()

    val appUiState: StateFlow<EnterExtraPasswordAppState> = combine(
        snackbarDispatcher.snackbarMessage,
        networkStatus
    ) { snackbarMessage, networkStatus ->
        EnterExtraPasswordAppState(
            snackbarMessage = snackbarMessage,
            networkStatus = networkStatus
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = EnterExtraPasswordAppState.Initial
    )

    fun onSnackbarMessageDelivered() {
        viewModelScope.launch {
            snackbarDispatcher.snackbarMessageDelivered()
        }
    }
}
