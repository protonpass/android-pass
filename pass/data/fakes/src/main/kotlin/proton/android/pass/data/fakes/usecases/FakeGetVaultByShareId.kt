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

package proton.android.pass.data.fakes.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onSubscription
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.FlowUtils
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.usecases.GetVaultByShareId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.Vault
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeGetVaultByShareId @Inject constructor() : GetVaultByShareId {

    private val flow: MutableSharedFlow<Vault> = FlowUtils.testFlow()
    private var exceptionOption: Option<Exception> = None

    fun emitValue(value: Vault) {
        flow.tryEmit(value)
    }

    fun sendException(exception: Exception) {
        exceptionOption = exception.toOption()
    }

    override fun invoke(userId: UserId?, shareId: ShareId): Flow<Vault> = flow.onSubscription {
        when (val exception = exceptionOption) {
            None -> {}
            is Some -> throw exception.value
        }
    }
}
