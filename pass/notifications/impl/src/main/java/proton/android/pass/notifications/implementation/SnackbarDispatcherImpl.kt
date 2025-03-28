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

package proton.android.pass.notifications.implementation

import android.os.Build
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.notifications.api.SnackbarMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SnackbarDispatcherImpl @Inject constructor() : SnackbarDispatcher {
    private val mutex = Mutex()

    private val snackbarState = MutableStateFlow<Option<SnackbarMessage>>(None)

    override val snackbarMessage: Flow<Option<SnackbarMessage>> = snackbarState

    override suspend fun invoke(snackbarMessage: SnackbarMessage) {
        if (!shouldDisplay(snackbarMessage)) return
        mutex.withLock {
            snackbarState.update { snackbarMessage.some() }
        }
    }

    override suspend fun snackbarMessageDelivered() {
        mutex.withLock {
            snackbarState.update { None }
        }
    }

    override fun reset() {
        snackbarState.update { None }
    }

    private fun shouldDisplay(snackbarMessage: SnackbarMessage): Boolean {
        if (snackbarMessage !is SnackbarMessage.StructuredMessage) return true
        return !(snackbarMessage.isClipboard && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
    }

}
