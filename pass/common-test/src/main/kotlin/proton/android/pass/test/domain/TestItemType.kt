package proton.android.pass.test.domain

import proton.android.pass.test.TestUtils
import proton.pass.domain.ItemType

object TestItemType {
    fun login(
        username: String? = null,
        password: String? = null,
        websites: List<String> = emptyList()
    ): ItemType.Login =
        ItemType.Login(
            username = username ?: TestUtils.randomString(),
            password = password ?: TestUtils.randomString(),
            websites = websites,
            primaryTotp = TestUtils.randomString()
        )
}
