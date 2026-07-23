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
import proton.android.pass.data.impl.fakes.AliasItemsPageRequest
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
    fun `refreshBulkAliasSlNotes does not create an observable query for each share`() = runTest {
        val firstShareId = ShareId(SHARE_ID)
        val secondShareId = ShareId(OTHER_SHARE_ID)
        localItemDataSource.upsertItem(
            ItemEntityTestFactory.create(
                id = ALIAS_ITEM_ID,
                shareId = firstShareId.id,
                aliasEmail = ALIAS_EMAIL
            )
        )
        repeat(100) { index ->
            localItemDataSource.upsertItem(
                ItemEntityTestFactory.create(
                    id = "paged-alias-$index",
                    shareId = firstShareId.id,
                    aliasEmail = "paged-alias-$index@example.test"
                )
            )
        }
        localItemDataSource.upsertItem(
            ItemEntityTestFactory.create(
                id = OTHER_ALIAS_ITEM_ID,
                shareId = secondShareId.id,
                aliasEmail = OTHER_ALIAS_EMAIL
            )
        )

        instance.refreshBulkAliasSlNotes(USER_ID, listOf(firstShareId, secondShareId))

        assertThat(localItemDataSource.getObserveItemsShareIdsMemory()).isEmpty()
        assertThat(localItemDataSource.getActiveAliasItemsPageRequests()).containsExactly(
            AliasItemsPageRequest(firstShareId, afterRowId = 0, limit = 100),
            AliasItemsPageRequest(firstShareId, afterRowId = 100, limit = 100),
            AliasItemsPageRequest(secondShareId, afterRowId = 0, limit = 100)
        ).inOrder()
    }

    @Test
    fun `refreshBulkAliasSlNotes coalesces updates from shares into one bounded database batch`() = runTest {
        val firstShareId = ShareId(SHARE_ID)
        val secondShareId = ShareId(OTHER_SHARE_ID)
        val aliases = buildList {
            listOf(firstShareId, secondShareId).forEachIndexed { shareIndex, shareId ->
                repeat(50) { itemIndex ->
                    val itemId = ItemId("alias-$shareIndex-$itemIndex")
                    val email = "alias-$shareIndex-$itemIndex@example.test"
                    localItemDataSource.upsertItem(
                        ItemEntityTestFactory.create(
                            id = itemId.id,
                            shareId = shareId.id,
                            aliasEmail = email
                        )
                    )
                    add(itemId to aliasResponse(email, "note-$shareIndex-$itemIndex"))
                }
            }
        }
        remoteDataSource.setBulkAliasDetailsByItemId(aliases.toMap())

        instance.refreshBulkAliasSlNotes(USER_ID, listOf(firstShareId, secondShareId))

        val batches = localItemDataSource.getSlNoteUpdateBatches()
        assertThat(batches).hasSize(1)
        assertThat(batches.single()).hasSize(100)
    }

    @Test
    fun `refreshAliasSlNotesForItems batches and indexes SL notes within a remote response chunk`() = runTest {
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
        remoteDataSource.setBulkAliasDetails(
            listOf(
                aliasResponse(ALIAS_EMAIL, SL_NOTE),
                aliasResponse(OTHER_ALIAS_EMAIL, OTHER_SL_NOTE)
            )
        )

        instance.refreshAliasSlNotesForItems(
            userId = USER_ID,
            items = listOf(
                ShareId(SHARE_ID) to ItemId(ALIAS_ITEM_ID),
                ShareId(SHARE_ID) to ItemId(OTHER_ALIAS_ITEM_ID)
            )
        )

        val updates = localItemDataSource.getSlNoteUpdateBatches().single()
        assertThat(updates.map { it.shareId to it.itemId }).containsExactly(
            ShareId(SHARE_ID) to ItemId(ALIAS_ITEM_ID),
            ShareId(SHARE_ID) to ItemId(OTHER_ALIAS_ITEM_ID)
        )
        assertThat(
            updates.associate { update ->
                update.itemId to requireNotNull(update.encryptedNote)
            }.mapValues { (_, encryptedNote) ->
                encryptionContextProvider.withEncryptionContext { decrypt(encryptedNote) }
            }
        ).containsExactly(
            ItemId(ALIAS_ITEM_ID), SL_NOTE,
            ItemId(OTHER_ALIAS_ITEM_ID), OTHER_SL_NOTE
        )
        assertThat(searchIndexRepository.isItemIndexed(ShareId(SHARE_ID), ItemId(ALIAS_ITEM_ID))).isTrue()
        assertThat(searchIndexRepository.isItemIndexed(ShareId(SHARE_ID), ItemId(OTHER_ALIAS_ITEM_ID))).isTrue()
    }

    @Test
    fun `refreshAliasSlNotesForItems preserves the 100 item batch boundary`() = runTest {
        val aliases = (1..101).map { index ->
            val itemId = ItemId("alias-item-$index")
            val email = "alias-$index@passmail.com"
            localItemDataSource.upsertItem(
                ItemEntityTestFactory.create(
                    id = itemId.id,
                    shareId = SHARE_ID,
                    aliasEmail = email
                )
            )
            itemId to aliasResponse(email, "note-$index")
        }
        remoteDataSource.setBulkAliasDetailsByItemId(aliases.toMap())

        instance.refreshAliasSlNotesForItems(
            userId = USER_ID,
            items = aliases.map { (itemId, _) -> ShareId(SHARE_ID) to itemId }
        )

        val batches = localItemDataSource.getSlNoteUpdateBatches()
        assertThat(batches).hasSize(2)
        assertThat(batches[0].map { it.itemId })
            .containsExactlyElementsIn(aliases.take(100).map { it.first })
            .inOrder()
        assertThat(batches[1].map { it.itemId })
            .containsExactlyElementsIn(aliases.takeLast(1).map { it.first })
            .inOrder()
        assertThat(
            aliases.all { (itemId, _) ->
                searchIndexRepository.isItemIndexed(ShareId(SHARE_ID), itemId)
            }
        ).isTrue()
    }

    @Test
    fun `refreshAliasSlNotesForItems clears a null remote SL note through the batch`() = runTest {
        localItemDataSource.upsertItem(
            ItemEntityTestFactory.create(
                id = ALIAS_ITEM_ID,
                shareId = SHARE_ID,
                aliasEmail = ALIAS_EMAIL
            )
        )
        remoteDataSource.setBulkAliasDetails(listOf(aliasResponse(ALIAS_EMAIL, null)))

        instance.refreshAliasSlNotesForItems(
            userId = USER_ID,
            items = listOf(ShareId(SHARE_ID) to ItemId(ALIAS_ITEM_ID))
        )

        assertThat(localItemDataSource.getSlNoteUpdateBatches().single().single().encryptedNote).isNull()
    }

    @Test
    fun `refreshAliasSlNotesForItems indexes only locally committed SL note updates`() = runTest {
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
        remoteDataSource.setBulkAliasDetails(
            listOf(
                aliasResponse(ALIAS_EMAIL, SL_NOTE),
                aliasResponse(OTHER_ALIAS_EMAIL, OTHER_SL_NOTE)
            )
        )
        localItemDataSource.setCommittedSlNoteUpdateIds(
            listOf(ShareId(SHARE_ID) to ItemId(ALIAS_ITEM_ID))
        )

        instance.refreshAliasSlNotesForItems(
            userId = USER_ID,
            items = listOf(
                ShareId(SHARE_ID) to ItemId(ALIAS_ITEM_ID),
                ShareId(SHARE_ID) to ItemId(OTHER_ALIAS_ITEM_ID)
            )
        )

        assertThat(searchIndexRepository.isItemIndexed(ShareId(SHARE_ID), ItemId(ALIAS_ITEM_ID))).isTrue()
        assertThat(searchIndexRepository.isItemIndexed(ShareId(SHARE_ID), ItemId(OTHER_ALIAS_ITEM_ID))).isFalse()
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
        const val OTHER_SHARE_ID = "other-share-id"
        const val ALIAS_ITEM_ID = "alias-item-id"
        const val OTHER_ALIAS_ITEM_ID = "other-alias-item-id"
        const val LOGIN_ITEM_ID = "login-item-id"
        const val ALIAS_EMAIL = "alias@passmail.com"
        const val OTHER_ALIAS_EMAIL = "other@passmail.com"
        const val SL_NOTE = "shopping newsletters"
        const val OTHER_SL_NOTE = "marketing emails"
    }
}
