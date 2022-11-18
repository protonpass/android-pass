package me.proton.android.pass.data.impl.usecases

import me.proton.android.pass.data.api.usecases.GetAddressById
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserAddressManager
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress
import javax.inject.Inject

class GetAddressByIdImpl @Inject constructor(
    private val addressManager: UserAddressManager
) : GetAddressById {

    override suspend fun invoke(userId: UserId, addressId: AddressId): UserAddress? =
        addressManager.getAddress(userId, addressId)
}

