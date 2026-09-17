/*
 * Copyright (c) 2026 Proton AG
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

package proton.android.pass.data.impl.usecases

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.fakes.usecases.FakeObservePinnedItems
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId

class ObservePinnedItemCountImplTest {

    private lateinit var observePinnedItems: FakeObservePinnedItems
    private lateinit var encryptionContextProvider: FakeEncryptionContextProvider
    private lateinit var instance: ObservePinnedItemCountImpl

    @Before
    fun setUp() {
        observePinnedItems = FakeObservePinnedItems()
        encryptionContextProvider = FakeEncryptionContextProvider()

        instance = ObservePinnedItemCountImpl(
            observePinnedItems = observePinnedItems,
            encryptionContextProvider = encryptionContextProvider
        )
    }

    @Test
    fun `counts pinned items by category regardless of vault or folder`() = runTest {
        val shareId = ShareId("other-share")
        val login = FakeObservePinnedItems.createLogin(
            shareId = shareId,
            itemId = ItemId("login-1")
        )
        val loginWithMFA = FakeObservePinnedItems.createLogin(
            shareId = ShareId("yet-another-share"),
            itemId = ItemId("login-2"),
            primaryTotp = "otpauth://totp/test"
        )
        val alias = FakeObservePinnedItems.createAlias(
            shareId = shareId,
            itemId = ItemId("alias-1")
        )
        val note = FakeObservePinnedItems.createNote(
            shareId = shareId,
            itemId = ItemId("note-1")
        )
        val creditCard = FakeObservePinnedItems.createCreditCard(
            shareId = shareId,
            itemId = ItemId("cc-1")
        )

        observePinnedItems.emitValue(listOf(login, loginWithMFA, alias, note, creditCard))

        instance().test {
            val summary = awaitItem()
            assertThat(summary.login).isEqualTo(2)
            assertThat(summary.loginWithMFA).isEqualTo(1)
            assertThat(summary.alias).isEqualTo(1)
            assertThat(summary.note).isEqualTo(1)
            assertThat(summary.creditCard).isEqualTo(1)
            assertThat(summary.identities).isEqualTo(0)
            assertThat(summary.custom).isEqualTo(0)
            assertThat(summary.sharedWithMe).isEqualTo(0)
            assertThat(summary.sharedByMe).isEqualTo(0)
            assertThat(summary.trashed).isEqualTo(0)
        }
    }

    @Test
    fun `emits empty summary when there are no pinned items`() = runTest {
        observePinnedItems.emitValue(emptyList())

        instance().test {
            val summary = awaitItem()
            assertThat(summary.total).isEqualTo(0)
        }
    }
}
