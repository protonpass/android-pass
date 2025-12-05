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

package proton.android.pass.notifications.fakes

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.notifications.api.SnackbarMessage
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeSnackbarDispatcher @Inject constructor() : SnackbarDispatcher {
    private val snackbarState = MutableSharedFlow<Option<SnackbarMessage>>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override val snackbarMessage: Flow<Option<SnackbarMessage>> = snackbarState

    override suspend fun invoke(snackbarMessage: SnackbarMessage) {
        snackbarState.tryEmit(snackbarMessage.some())
    }

    override suspend fun snackbarMessageDelivered() {
        snackbarState.tryEmit(None)
    }

    override fun reset() {
        snackbarState.tryEmit(None)
    }
}
