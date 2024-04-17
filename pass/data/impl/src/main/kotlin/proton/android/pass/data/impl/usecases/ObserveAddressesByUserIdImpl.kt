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

package proton.android.pass.data.impl.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.repository.UserAddressRepository
import proton.android.pass.data.api.usecases.ObserveAddressesByUserId
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import javax.inject.Inject

class ObserveAddressesByUserIdImpl @Inject constructor(
    private val observeCurrentUser: ObserveCurrentUser,
    private val addressRepository: UserAddressRepository
) : ObserveAddressesByUserId {

    override fun invoke(userId: UserId?): Flow<List<UserAddress>> = if (userId == null) {
        observeCurrentUser().flatMapLatest { addressRepository.observeAddresses(it.userId) }
    } else {
        addressRepository.observeAddresses(userId)
    }
}

