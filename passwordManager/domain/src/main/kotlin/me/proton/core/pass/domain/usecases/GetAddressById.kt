package me.proton.core.pass.domain.usecases

import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.UserAddressManager
import me.proton.core.user.domain.entity.AddressId
import javax.inject.Inject

class GetAddressById @Inject constructor(
    private val addressManager: UserAddressManager,
) {
    suspend operator fun invoke(userId: UserId, addressId: AddressId) =
        addressManager.getAddress(userId, addressId)
}
