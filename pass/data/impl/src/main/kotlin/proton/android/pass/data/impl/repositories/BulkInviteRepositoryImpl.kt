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

package proton.android.pass.data.impl.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import proton.android.pass.data.api.repositories.AddressPermission
import proton.android.pass.data.api.repositories.BulkInviteRepository
import proton.android.pass.domain.ShareRole
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BulkInviteRepositoryImpl @Inject constructor() : BulkInviteRepository {

    private val addressesFlow: MutableStateFlow<List<AddressPermission>> =
        MutableStateFlow(emptyList())

    private val invalidAddressesFlow = MutableStateFlow<Set<String>>(emptySet())

    override fun storeAddresses(addresses: List<String>) {
        val uniqueAddresses = addresses.distinct()
        addressesFlow.update {
            uniqueAddresses.map { AddressPermission(it, ShareRole.Read) }
        }
    }

    override fun setPermission(address: String, permission: ShareRole) {
        addressesFlow.update { state ->
            val newList = state.toMutableList()
            val index = newList.indexOfFirst { it.address == address }
            newList[index] = newList[index].copy(shareRole = permission)
            newList
        }
    }

    override fun setAllPermissions(permission: ShareRole) {
        addressesFlow.update { state ->
            state.map { it.copy(shareRole = permission) }
        }
    }

    override fun removeAddress(address: String) {
        addressesFlow.update { state ->
            val newList = state.toMutableList()
            newList.removeIf { it.address == address }
            newList
        }
    }

    override fun observeAddresses(): Flow<List<AddressPermission>> = addressesFlow

    override fun updateInvalidAddresses(addresses: List<String>) {
        invalidAddressesFlow.update { addresses.toSet() }
    }

    override fun observeInvalidAddresses(): Flow<Set<String>> = invalidAddressesFlow

    override fun clearInvalidAddresses() {
        invalidAddressesFlow.update { emptySet() }
    }

    override fun clear() {
        addressesFlow.update { emptyList() }
    }

}
