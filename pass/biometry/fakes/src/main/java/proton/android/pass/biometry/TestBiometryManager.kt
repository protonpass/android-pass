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

package proton.android.pass.biometry

import android.content.Context
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import proton.android.pass.commonui.api.ClassHolder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestBiometryManager @Inject constructor() : BiometryManager {

    private var biometryStatus: BiometryStatus = BiometryStatus.CanAuthenticate

    private val resultFlow = MutableSharedFlow<BiometryResult>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    var hasBeenCalled = false
        private set

    fun setBiometryStatus(status: BiometryStatus) {
        biometryStatus = status
    }

    fun emitResult(result: BiometryResult) {
        resultFlow.tryEmit(result)
    }

    override fun getBiometryStatus(): BiometryStatus = biometryStatus

    override fun launch(contextHolder: ClassHolder<Context>): Flow<BiometryResult> {
        hasBeenCalled = true
        return resultFlow
    }

}
