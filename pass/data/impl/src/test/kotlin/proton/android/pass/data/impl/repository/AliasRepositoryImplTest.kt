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

package proton.android.pass.data.impl.repository

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.crypto.fakes.context.FakeEncryptionContextProvider
import proton.android.pass.data.fakes.repositories.FakeSearchIndexRepository
import proton.android.pass.data.impl.fakes.FakeLocalItemDataSource
import proton.android.pass.data.impl.fakes.FakeRemoteAliasDataSource
import proton.android.pass.data.impl.fakes.mother.ItemEntityTestFactory
import proton.android.pass.data.impl.repositories.AliasRepositoryImpl
import proton.android.pass.data.impl.responses.AliasResponse
import proton.android.pass.data.impl.responses.AliasStatsResponse
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId

class AliasRepositoryImplTest {

    private lateinit var remoteDataSource: FakeRemoteAliasDataSource
    private lateinit var localItemDataSource: FakeLocalItemDataSource
    private lateinit var searchIndexRepository: FakeSearchIndexRepository
    private lateinit var encryptionContextProvider: FakeEncryptionContextProvider
    private lateinit var instance: AliasRepositoryImpl

    @Before
    fun setup() {
        remoteDataSource = FakeRemoteAliasDataSource()
        localItemDataSource = FakeLocalItemDataSource()
        searchIndexRepository = FakeSearchIndexRepository()
        encryptionContextProvider = FakeEncryptionContextProvider()
        instance = AliasRepositoryImpl(
            remoteDataSource = remoteDataSource,
            localItemDataSource = localItemDataSource,
            searchIndexRepository = searchIndexRepository,
            encryptionContextProvider = encryptionContextProvider
        )
    }

    @Test
    fun `refreshAliasSlNotesForItems stores and indexes the SL note`() = runTest {
        localItemDataSource.upsertItem(
            ItemEntityTestFactory.create(
                id = ALIAS_ITEM_ID,
                shareId = SHARE_ID,
                aliasEmail = ALIAS_EMAIL
            )
        )
        remoteDataSource.setBulkAliasDetails(listOf(aliasResponse(ALIAS_EMAIL, SL_NOTE)))

        instance.refreshAliasSlNotesForItems(
            userId = USER_ID,
            items = listOf(ShareId(SHARE_ID) to ItemId(ALIAS_ITEM_ID))
        )

        val update = localItemDataSource.getSlNoteUpdates().single()
        assertThat(update.first).isEqualTo(ShareId(SHARE_ID))
        assertThat(update.second).isEqualTo(ItemId(ALIAS_ITEM_ID))
        assertThat(encryptionContextProvider.withEncryptionContext { decrypt(update.third!!) })
            .isEqualTo(SL_NOTE)

        assertThat(searchIndexRepository.isItemIndexed(ShareId(SHARE_ID), ItemId(ALIAS_ITEM_ID)))
            .isTrue()
    }

    @Test
    fun `refreshAliasSlNotesForItems ignores items that are not aliases`() = runTest {
        localItemDataSource.upsertItem(
            ItemEntityTestFactory.create(
                id = LOGIN_ITEM_ID,
                shareId = SHARE_ID,
                aliasEmail = null
            )
        )

        instance.refreshAliasSlNotesForItems(
            userId = USER_ID,
            items = listOf(ShareId(SHARE_ID) to ItemId(LOGIN_ITEM_ID))
        )

        assertThat(remoteDataSource.getFetchBulkAliasDetailsMemory()).isEmpty()
        assertThat(localItemDataSource.getSlNoteUpdates()).isEmpty()
    }

    @Test
    fun `refreshAliasSlNotesForItems only asks for the given items`() = runTest {
        localItemDataSource.upsertItem(
            ItemEntityTestFactory.create(
                id = ALIAS_ITEM_ID,
                shareId = SHARE_ID,
                aliasEmail = ALIAS_EMAIL
            )
        )
        localItemDataSource.upsertItem(
            ItemEntityTestFactory.create(
                id = OTHER_ALIAS_ITEM_ID,
                shareId = SHARE_ID,
                aliasEmail = OTHER_ALIAS_EMAIL
            )
        )
        remoteDataSource.setBulkAliasDetails(listOf(aliasResponse(ALIAS_EMAIL, SL_NOTE)))

        instance.refreshAliasSlNotesForItems(
            userId = USER_ID,
            items = listOf(ShareId(SHARE_ID) to ItemId(ALIAS_ITEM_ID))
        )

        assertThat(remoteDataSource.getFetchBulkAliasDetailsMemory()).containsExactly(
            ShareId(SHARE_ID) to listOf(ItemId(ALIAS_ITEM_ID))
        )
    }

    private fun aliasResponse(email: String, note: String?) = AliasResponse(
        email = email,
        modify = true,
        mailboxes = emptyList(),
        availableMailboxes = emptyList(),
        stats = AliasStatsResponse(forwardedEmails = 0, repliedEmails = 0, blockedEmails = 0),
        name = null,
        displayName = "",
        note = note
    )

    private companion object {
        val USER_ID = UserId("user-id")
        const val SHARE_ID = "share-id"
        const val ALIAS_ITEM_ID = "alias-item-id"
        const val OTHER_ALIAS_ITEM_ID = "other-alias-item-id"
        const val LOGIN_ITEM_ID = "login-item-id"
        const val ALIAS_EMAIL = "alias@passmail.com"
        const val OTHER_ALIAS_EMAIL = "other@passmail.com"
        const val SL_NOTE = "shopping newsletters"
    }
}
