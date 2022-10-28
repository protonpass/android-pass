package me.proton.pass.test.domain

import me.proton.pass.domain.ItemType
import me.proton.pass.test.TestUtils

object TestItemType {
    fun login(
        username: String? = null,
        password: String? = null,
        websites: List<String> = emptyList()
    ): ItemType.Login =
        ItemType.Login(
            username = username ?: TestUtils.randomString(),
            password = password ?: TestUtils.randomString(),
            websites = websites
        )
}
