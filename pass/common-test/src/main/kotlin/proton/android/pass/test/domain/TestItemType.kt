package proton.android.pass.test.domain

import proton.android.pass.test.TestUtils
import proton.pass.domain.ItemType
import proton.pass.domain.entity.PackageInfo

object TestItemType {
    fun login(
        username: String? = null,
        password: String? = null,
        websites: List<String> = emptyList(),
        packageInfoSet: Set<PackageInfo> = emptySet(),
    ): ItemType.Login =
        ItemType.Login(
            username = username ?: TestUtils.randomString(),
            password = password ?: TestUtils.randomString(),
            websites = websites,
            packageInfoSet = packageInfoSet,
            primaryTotp = TestUtils.randomString()
        )
}
