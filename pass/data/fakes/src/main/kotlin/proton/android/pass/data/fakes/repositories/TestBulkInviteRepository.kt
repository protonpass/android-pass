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

package proton.android.pass.data.fakes.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import proton.android.pass.data.api.repositories.AddressPermission
import proton.android.pass.data.api.repositories.BulkInviteRepository
import proton.android.pass.domain.ShareRole
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestBulkInviteRepository @Inject constructor() : BulkInviteRepository {

    private val addressesFlow: MutableStateFlow<MutableList<AddressPermission>> =
        MutableStateFlow(mutableListOf())

    override suspend fun storeAddresses(addresses: List<String>) {
        addressesFlow.update {
            addresses.map { AddressPermission(it, ShareRole.Read) }.toMutableList()
        }
    }

    override suspend fun setPermission(address: String, permission: ShareRole) {
        addressesFlow.update { state ->
            val index = state.indexOfFirst { it.address == address }
            state[index] = state[index].copy(permission = permission)
            state
        }
    }

    override suspend fun removeAddress(address: String) {
        addressesFlow.update { state ->
            state.removeIf { it.address == address }
            state
        }
    }

    override fun observeAddresses(): Flow<List<AddressPermission>> = addressesFlow

    override suspend fun clear() {
        addressesFlow.update { mutableListOf() }
    }
}
