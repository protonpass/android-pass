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

package proton.android.pass.data.impl.usecases.passkeys

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Test
import proton.android.pass.account.fakes.TestKeyStoreCrypto
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.data.api.usecases.passkeys.PasskeySelection
import proton.android.pass.data.fakes.usecases.TestObserveItemsWithPasskeys
import proton.android.pass.data.fakes.usecases.shares.FakeObserveAutofillShares
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.Passkey
import proton.android.pass.domain.PasskeyId
import proton.android.pass.test.domain.TestItem
import proton.android.pass.test.domain.TestItemType
import kotlin.time.Duration.Companion.hours

class GetPasskeysForDomainImplTest {

    private lateinit var instance: GetPasskeysForDomainImpl

    private lateinit var observeItemsWithPasskeys: TestObserveItemsWithPasskeys
    private lateinit var encryptionContextProvider: TestEncryptionContextProvider

    @Before
    fun setup() {
        observeItemsWithPasskeys = TestObserveItemsWithPasskeys()
        encryptionContextProvider = TestEncryptionContextProvider()

        instance = GetPasskeysForDomainImpl(
            observeItemsWithPasskeys = observeItemsWithPasskeys,
            observeAutofillShares = FakeObserveAutofillShares(),
            encryptionContextProvider = encryptionContextProvider
        )
    }

    @Test
    fun `return empty list with empty items`() = runTest {
        observeItemsWithPasskeys.emit(emptyList())

        val res = instance.invoke("domain.test")
        assertThat(res).isEmpty()
    }

    @Test
    fun `returns all matching passkeys for domain`() = runTest {
        val itemId1 = ItemId("item1")
        val itemId2 = ItemId("item1")
        val passkeyId1 = PasskeyId("id1")
        val passkeyId2 = PasskeyId("id2")
        val passkeyId3 = PasskeyId("id3")
        val domain = "domain.test"
        val items = listOf(
            TestItem.create(
                keyStoreCrypto = TestKeyStoreCrypto,
                itemId = itemId1,
                itemType = TestItemType.login(
                    passkeys = listOf(
                        testPasskey(passkeyId1, domain),
                        testPasskey(passkeyId2, domain),
                        testPasskey(PasskeyId("other"), "other.domain")
                    )
                )
            ),
            TestItem.create(
                keyStoreCrypto = TestKeyStoreCrypto,
                itemId = itemId2,
                itemType = TestItemType.login(
                    passkeys = listOf(
                        testPasskey(passkeyId3, domain),
                        testPasskey(PasskeyId("otherone"), "other.domains")
                    )
                )
            )
        )
        observeItemsWithPasskeys.emit(items)

        val res = instance.invoke(domain)
        assertThat(res.size).isEqualTo(3)

        val withId1 = res.first { it.passkey.id == passkeyId1 }
        assertThat(withId1.itemId).isEqualTo(itemId1)
        assertThat(withId1.passkey.id).isEqualTo(passkeyId1)

        val withId2 = res.first { it.passkey.id == passkeyId2 }
        assertThat(withId2.itemId).isEqualTo(itemId1)
        assertThat(withId2.passkey.id).isEqualTo(passkeyId2)

        val withId3 = res.first { it.passkey.id == passkeyId3 }
        assertThat(withId3.itemId).isEqualTo(itemId2)
        assertThat(withId3.passkey.id).isEqualTo(passkeyId3)
    }

    @Test
    fun `returns only allowed passkeys for domain`() = runTest {
        val itemId1 = ItemId("item1")
        val itemId2 = ItemId("item1")
        val passkeyId1 = PasskeyId("id1")
        val passkeyId2 = PasskeyId("id2")
        val passkeyId3 = PasskeyId("id3")
        val domain = "domain.test"
        val items = listOf(
            TestItem.create(
                keyStoreCrypto = TestKeyStoreCrypto,
                itemId = itemId1,
                itemType = TestItemType.login(
                    passkeys = listOf(
                        testPasskey(passkeyId1, domain),
                        testPasskey(passkeyId2, domain),
                        testPasskey(PasskeyId("other"), "other.domain")
                    )
                )
            ),
            TestItem.create(
                keyStoreCrypto = TestKeyStoreCrypto,
                itemId = itemId2,
                itemType = TestItemType.login(
                    passkeys = listOf(
                        testPasskey(passkeyId3, domain),
                        testPasskey(PasskeyId("otherone"), "other.domains")
                    )
                )
            )
        )
        observeItemsWithPasskeys.emit(items)

        val selection = PasskeySelection.Allowed(listOf(passkeyId1, passkeyId3))
        val res = instance.invoke(domain, selection)
        assertThat(res.size).isEqualTo(2)

        val withId1 = res.first { it.passkey.id == passkeyId1 }
        assertThat(withId1.itemId).isEqualTo(itemId1)
        assertThat(withId1.passkey.id).isEqualTo(passkeyId1)

        val withId3 = res.first { it.passkey.id == passkeyId3 }
        assertThat(withId3.itemId).isEqualTo(itemId2)
        assertThat(withId3.passkey.id).isEqualTo(passkeyId3)
    }

    @Test
    fun `returns empty list if allowed passkeys are not for the domain`() = runTest {
        val itemId1 = ItemId("item1")
        val itemId2 = ItemId("item2")
        val passkeyId1 = PasskeyId("id1")
        val passkeyId2 = PasskeyId("id2")
        val passkeyId3 = PasskeyId("id3")
        val domain = "domain.test"
        val items = listOf(
            TestItem.create(
                keyStoreCrypto = TestKeyStoreCrypto,
                itemId = itemId1,
                itemType = TestItemType.login(
                    passkeys = listOf(
                        testPasskey(passkeyId1, "other.domain"),
                        testPasskey(passkeyId2, domain),
                        testPasskey(PasskeyId("other"), "other.domain")
                    )
                )
            ),
            TestItem.create(
                keyStoreCrypto = TestKeyStoreCrypto,
                itemId = itemId2,
                itemType = TestItemType.login(
                    passkeys = listOf(
                        testPasskey(passkeyId3, "other.domain"),
                        testPasskey(PasskeyId("otherone"), "other.domains")
                    )
                )
            )
        )
        observeItemsWithPasskeys.emit(items)

        val selection = PasskeySelection.Allowed(listOf(passkeyId1, passkeyId3))
        val res = instance.invoke(domain, selection)
        assertThat(res).isEmpty()
    }

    @Test
    fun `returns passkeys sorted by creation date desc`() = runTest {
        val itemId1 = ItemId("item1")
        val itemId2 = ItemId("item2")
        val passkeyId1 = PasskeyId("id1")
        val passkeyId2 = PasskeyId("id2")
        val passkeyId3 = PasskeyId("id3")
        val domain = "domain.test"

        val createTime1 = Clock.System.now().minus(1.hours)
        val createTime2 = Clock.System.now().minus(2.hours)
        val createTime3 = Clock.System.now().minus(3.hours)

        val items = listOf(
            TestItem.create(
                keyStoreCrypto = TestKeyStoreCrypto,
                itemId = itemId1,
                itemType = TestItemType.login(
                    passkeys = listOf(
                        testPasskey(passkeyId1, domain, createTime2),
                        testPasskey(passkeyId2, domain, createTime3)
                    )
                )
            ),
            TestItem.create(
                keyStoreCrypto = TestKeyStoreCrypto,
                itemId = itemId2,
                itemType = TestItemType.login(
                    passkeys = listOf(
                        testPasskey(passkeyId3, domain, createTime1)
                    )
                )
            )
        )
        observeItemsWithPasskeys.emit(items)


        val res = instance.invoke(domain, PasskeySelection.All)
        assertThat(res.size).isEqualTo(3)

        assertThat(res[0].itemId).isEqualTo(itemId2)
        assertThat(res[0].passkey.id).isEqualTo(passkeyId3)
        assertThat(res[1].itemId).isEqualTo(itemId1)
        assertThat(res[1].passkey.id).isEqualTo(passkeyId1)
        assertThat(res[2].itemId).isEqualTo(itemId1)
        assertThat(res[2].passkey.id).isEqualTo(passkeyId2)
    }

    private fun testPasskey(
        id: PasskeyId,
        domain: String,
        createTime: Instant = Clock.System.now()
    ) = Passkey(
        id = id,
        domain = domain,
        rpId = "",
        rpName = "",
        userName = "",
        userDisplayName = "",
        userId = byteArrayOf(),
        note = "",
        createTime = createTime,
        contents = byteArrayOf(),
        userHandle = null,
        credentialId = id.value.toByteArray(),
        creationData = null
    )
}
