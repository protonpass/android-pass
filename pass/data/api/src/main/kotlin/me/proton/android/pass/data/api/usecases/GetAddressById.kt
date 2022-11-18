package me.proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.AddressId
import me.proton.core.user.domain.entity.UserAddress

interface GetAddressById {
    suspend operator fun invoke(userId: UserId, addressId: AddressId): UserAddress?
}
