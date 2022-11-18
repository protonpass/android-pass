package me.proton.android.pass.data.impl.usecases

import me.proton.android.pass.data.api.usecases.GetAddressesForUserId
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.repository.UserAddressRepository
import javax.inject.Inject

class GetAddressesForUserIdImpl @Inject constructor(
    private val addressRepository: UserAddressRepository
) : GetAddressesForUserId {

    override suspend fun invoke(userId: UserId, refresh: Boolean): List<UserAddress> =
        addressRepository.getAddresses(userId, refresh)
}

