package proton.android.pass.test.domain

import me.proton.core.domain.entity.UserId
import me.proton.core.user.domain.entity.User

object TestUser {

    fun create(
        email: String? = null,
        name: String? = null
    ): User = User(
        userId = UserId("12345"),
        email = email,
        name = name,
        displayName = null,
        currency = "",
        credit = 0,
        usedSpace = 0,
        maxSpace = 0,
        maxUpload = 0,
        role = null,
        private = false,
        services = 0,
        subscribed = 0,
        delinquent = null,
        keys = listOf()
    )
}
