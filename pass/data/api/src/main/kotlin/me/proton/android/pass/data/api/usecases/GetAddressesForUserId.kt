package me.proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.UserAddress

interface GetAddressesForUserId {
    suspend operator fun invoke(userId: UserId, refresh: Boolean = false): List<UserAddress>
}
