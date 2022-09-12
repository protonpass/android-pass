package me.proton.core.pass.domain.usecases

import javax.inject.Inject
import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.repository.UserAddressRepository

class GetAddressesForUserId @Inject constructor(
    private val addressRepository: UserAddressRepository
) {
    suspend operator fun invoke(
        userId: UserId,
        refresh: Boolean = false
    ) = addressRepository.getAddresses(userId, refresh)
}
