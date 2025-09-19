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

package proton.android.pass.data.fakes.usecases.vaults

import kotlinx.coroutines.flow.Flow
import proton.android.pass.common.api.FlowUtils
import proton.android.pass.data.api.usecases.vaults.ObserveVaultsGroupedByVisibility
import proton.android.pass.domain.VaultWithItemCount
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeObserveVaultsGroupedByVisibility @Inject constructor() :
    ObserveVaultsGroupedByVisibility {

    private val flow =
        FlowUtils.testFlow<Pair<List<VaultWithItemCount>, List<VaultWithItemCount>>>()

    fun emit(item: Pair<List<VaultWithItemCount>, List<VaultWithItemCount>>) {
        flow.tryEmit(item)
    }

    fun emitDefault() {
        flow.tryEmit(Pair(emptyList(), emptyList()))
    }

    override fun invoke(): Flow<Pair<List<VaultWithItemCount>, List<VaultWithItemCount>>> = flow

}
