/*
 * Copyright (c) 2024 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.autofill.ui.autofill.select

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.autofill.ui.autofill.AutofillAppViewModel.Companion.shouldAskForAssociation
import proton.android.pass.crypto.fakes.context.FakeEncryptionContext
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.entity.AppName
import proton.android.pass.domain.entity.PackageInfo
import proton.android.pass.domain.entity.PackageName

class ShouldAskForAssociationTest {

    @Test
    fun `should not ask for association if package name is already present`() {
        val res = shouldAskForAssociation(
            item = itemContent(packageName = appPackageName),
            packageName = appPackageName,
            webDomain = null
        )
        assertThat(res).isFalse()
    }

    @Test
    fun `should not ask for association if url is already present`() {
        val res = shouldAskForAssociation(
            item = itemContent(url = "some.url"),
            packageName = appPackageName,
            webDomain = "some.url"
        )
        assertThat(res).isFalse()
    }

    @Test
    fun `should ask for association if package name is not browser and url is empty`() {
        val res = shouldAskForAssociation(
            item = itemContent(),
            packageName = appPackageName,
            webDomain = null
        )
        assertThat(res).isTrue()
    }

    @Test
    fun `should ask for association if package name is not browser and url is not empty`() {
        val res = shouldAskForAssociation(
            item = itemContent(),
            packageName = appPackageName,
            webDomain = "some.domain"
        )
        assertThat(res).isTrue()
    }

    @Test
    fun `should ask for association if package name is browser and url is not empty`() {
        val res = shouldAskForAssociation(
            item = itemContent(),
            packageName = browserPackageName,
            webDomain = "some.domain"
        )
        assertThat(res).isTrue()
    }

    private fun itemContent(url: String? = null, packageName: PackageName? = null): ItemContents.Login =
        ItemContents.Login(
            title = "title",
            note = "note",
            itemEmail = "user@email.com",
            itemUsername = "username",
            password = HiddenState.Empty(FakeEncryptionContext.encrypt("")),
            urls = mutableListOf<String>().apply { url?.let { add(it) } },
            customFields = emptyList(),
            packageInfoSet = mutableSetOf<PackageInfo>().apply {
                packageName?.let {
                    add(PackageInfo(it, AppName("app name")))
                }
            },
            primaryTotp = HiddenState.Empty(FakeEncryptionContext.encrypt("")),
            passkeys = emptyList()
        )

    companion object {
        private val browserPackageName = PackageName("com.android.chrome")
        private val appPackageName = PackageName("some.other.app")
    }

}
