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

package proton.android.pass.securitycenter.impl.checkers

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.securitycenter.fakes.passwords.FakeSupports2fa
import proton.android.pass.test.domain.ItemTestFactory
import proton.android.pass.test.domain.ItemTypeTestFactory

class Missing2faCheckerTest {

    private lateinit var instance: MissingTfaCheckerImpl

    private lateinit var supports2fa: FakeSupports2fa
    private lateinit var encryptionContextProvider: FakeEncryptionContextProvider

    @Before
    fun setup() {
        supports2fa = FakeSupports2fa()
        encryptionContextProvider = FakeEncryptionContextProvider()
        instance = MissingTfaCheckerImpl(
            supports2fa = supports2fa,
            encryptionContextProvider = encryptionContextProvider
        )
    }

    @Test
    fun `can handle empty list`() = runTest {
        val res = instance.invoke(emptyList())
        assertThat(res.items).isEmpty()
    }

    @Test
    fun `can handle non-empty list without websites nor totps`() = runTest {
        val items = (0 until 2).map {
            ItemTestFactory.random(ItemTypeTestFactory.login(websites = emptyList()))
        }
        val res = instance.invoke(items)
        assertThat(res.items).isEmpty()
    }

    @Test
    fun `can handle list without totp with websites that dont support 2fa`() = runTest {
        val items = (0 until 2).map {
            ItemTestFactory.random(ItemTypeTestFactory.login(websites = listOf("some.domain")))
        }
        val res = instance.invoke(items)
        assertThat(res.items).isEmpty()
    }

    @Test
    fun `can handle list with totp without websites that dont support 2fa`() = runTest {
        val items = (0 until 2).map {
            val itemType = ItemTypeTestFactory.login(
                websites = listOf("some.domain"),
                primaryTotp = "some.totp"
            )
            ItemTestFactory.random(itemType)
        }
        val res = instance.invoke(items)
        assertThat(res.items).isEmpty()
    }

    @Test
    fun `given a list of items with totp and domains that support 2fa, none should be reported`() = runTest {
        val domain = "some.domain"
        supports2fa.setSupportsList(listOf(domain))
        val items = (0 until 2).map {
            val itemType = ItemTypeTestFactory.login(
                websites = listOf(domain),
                primaryTotp = "some.totp"
            )
            ItemTestFactory.random(itemType)
        }
        val res = instance.invoke(items)
        assertThat(res.items).isEmpty()
    }

    @Test
    fun `is able to detect missing 2fa by only taking into account the domain`() = runTest {
        val domain = "some.domain"
        supports2fa.setSupportsList(listOf(domain))
        val item = ItemTestFactory.random(
            ItemTypeTestFactory.login(websites = listOf("https://$domain"))
        )

        val res = instance.invoke(listOf(item))
        assertThat(res.items.size).isEqualTo(1)
        assertThat(res.items[0].id).isEqualTo(item.id)
    }

    @Test
    fun `given a list of items without totp and domains that support 2fa, they should be reported`() = runTest {
        val domain = "some.domain"
        supports2fa.setSupportsList(listOf(domain))

        val item1 = ItemTestFactory.random(
            ItemTypeTestFactory.login(websites = listOf(domain))
        )
        val item2 = ItemTestFactory.random(
            ItemTypeTestFactory.login(websites = listOf("random.domain", domain))
        )
        val item3 = ItemTestFactory.random(
            ItemTypeTestFactory.login(
                websites = listOf(domain),
                primaryTotp = "sometotp"
            )
        )
        val item4 = ItemTestFactory.random(
            ItemTypeTestFactory.login(websites = listOf("other.domain"))
        )

        val res = instance.invoke(listOf(item1, item2, item3, item4))
        assertThat(res.items.size).isEqualTo(2)
        assertThat(res.items[0].id).isEqualTo(item1.id)
        assertThat(res.items[1].id).isEqualTo(item2.id)
    }

}
