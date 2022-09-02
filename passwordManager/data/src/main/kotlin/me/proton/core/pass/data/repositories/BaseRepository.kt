package me.proton.core.pass.data.repositories

import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.UserAddress
import me.proton.core.user.domain.extension.primary
import me.proton.core.user.domain.repository.UserAddressRepository

abstract class BaseRepository constructor(
    open val userAddressRepository: UserAddressRepository
) {

    protected suspend fun <R> withUserAddress(userId: UserId, block: suspend (UserAddress) -> R): R {
        val userAddress = requireNotNull(userAddressRepository.getAddresses(userId).primary())
        return block(userAddress)
    }
}
