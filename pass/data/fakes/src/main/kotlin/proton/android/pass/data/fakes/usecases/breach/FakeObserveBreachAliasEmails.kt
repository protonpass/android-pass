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

package proton.android.pass.data.fakes.usecases.breach

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import proton.android.pass.data.api.usecases.breach.ObserveBreachAliasEmails
import proton.android.pass.domain.breach.AliasData
import proton.android.pass.domain.breach.AliasKeyId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeObserveBreachAliasEmails @Inject constructor() : ObserveBreachAliasEmails {

    private val flow: MutableStateFlow<Map<AliasKeyId, AliasData>> = MutableStateFlow(emptyMap())

    fun emit(data: Map<AliasKeyId, AliasData>) {
        flow.update { data }
    }

    override fun invoke(): Flow<Map<AliasKeyId, AliasData>> = flow

}
