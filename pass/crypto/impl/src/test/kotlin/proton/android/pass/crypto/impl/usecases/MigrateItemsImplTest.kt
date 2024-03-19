/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.crypto.impl.usecases

import com.google.common.truth.Truth.assertThat
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import org.junit.Before
import org.junit.Test
import proton.android.pass.crypto.api.Base64
import proton.android.pass.crypto.api.EncryptionKey
import proton.android.pass.crypto.api.usecases.EncryptedMigrateContent
import proton.android.pass.crypto.api.usecases.ItemMigrationContent
import proton.android.pass.crypto.api.usecases.ItemMigrationHistoryContent
import proton.android.pass.crypto.api.usecases.ItemMigrationPayload
import proton.android.pass.crypto.api.usecases.MigrateItem
import proton.android.pass.crypto.fakes.context.TestEncryptionContextProvider
import proton.android.pass.crypto.fakes.utils.TestUtils
import proton.android.pass.domain.ItemContents
import proton_pass_item_v1.ItemV1
import kotlin.random.Random

internal class MigrateItemsImplTest {

    private val encryptionContextProvider = TestEncryptionContextProvider()
    private val createItem = CreateItemImpl(encryptionContextProvider)

    private lateinit var migrateItem: MigrateItem

    @Before
    internal fun setUp() {
        migrateItem = MigrateItemImpl(encryptionContextProvider)
    }

    @Test
    internal fun `WHEN migrating an item THEN item content should be migrated`() {
        val sourceShareKey = TestUtils.createShareKey().first
        val itemContents = ItemContents.Note(
            title = proton.android.pass.test.TestUtils.randomString(),
            note = proton.android.pass.test.TestUtils.randomString()
        )
        val item = createItem.create(sourceShareKey, itemContents)
        val (destinationShareKey, decryptedDestinationShareKey) = TestUtils.createShareKey()
        val payload = ItemMigrationPayloadMother.create(
            itemContent = ItemMigrationContentMother.create(
                encryptedItemContents = EncryptedByteArray(Base64.decodeBase64(item.request.content)),
                contentFormatVersion = item.request.contentFormatVersion
            )
        )

        val encryptedMigrateItemBody = migrateItem.migrate(destinationShareKey, payload)

        assertThat(encryptedMigrateItemBody.item.contentFormatVersion).isEqualTo(item.request.contentFormatVersion)
        assertThat(encryptedMigrateItemBody.item.keyRotation).isEqualTo(destinationShareKey.rotation)
        assertThat(encryptedMigrateItemBody.history).isEmpty()

        val decryptedContent = decryptEncryptedItemContent(
            encryptionKey = decryptedDestinationShareKey,
            encryptedMigrateContent = encryptedMigrateItemBody.item
        )
        val expectedItem = ItemV1.Item.parseFrom(decryptedContent)
        assertThat(expectedItem.metadata.name).isEqualTo(itemContents.title)
        assertThat(expectedItem.metadata.note).isEqualTo(itemContents.note)
    }

    @Test
    internal fun `WHEN migrating an item THEN item history should be migrated`() {
        val sourceShareKey = TestUtils.createShareKey().first
        val itemContents = ItemContents.Note(
            title = proton.android.pass.test.TestUtils.randomString(),
            note = proton.android.pass.test.TestUtils.randomString()
        )
        val item = createItem.create(sourceShareKey, itemContents)
        val itemRevision = Random.nextLong()
        val (destinationShareKey, decryptedDestinationShareKey) = TestUtils.createShareKey()
        val payload = ItemMigrationPayloadMother.create(
            historyContents = listOf(
                ItemMigrationHistoryContentMother.create(
                    revision = itemRevision,
                    itemContent = ItemMigrationContentMother.create(
                        encryptedItemContents = EncryptedByteArray(Base64.decodeBase64(item.request.content)),
                        contentFormatVersion = item.request.contentFormatVersion
                    )
                )
            )
        )

        val encryptedMigrateItemBody = migrateItem.migrate(destinationShareKey, payload)
        assertThat(encryptedMigrateItemBody.history.size).isEqualTo(1)

        val encryptedMigrateHistoryItemBody = encryptedMigrateItemBody.history.first()
        val decryptedContent = decryptEncryptedItemContent(
            encryptionKey = decryptedDestinationShareKey,
            encryptedMigrateContent = encryptedMigrateHistoryItemBody.content
        )
        assertThat(encryptedMigrateHistoryItemBody.revision).isEqualTo(itemRevision)
        val expectedItem = ItemV1.Item.parseFrom(decryptedContent)
        assertThat(expectedItem.metadata.name).isEqualTo(itemContents.title)
        assertThat(expectedItem.metadata.note).isEqualTo(itemContents.note)
    }

    private fun decryptEncryptedItemContent(
        encryptionKey: EncryptionKey,
        encryptedMigrateContent: EncryptedMigrateContent
    ) = encryptionContextProvider.withEncryptionContext(encryptionKey) {
        EncryptionKey(decrypt(EncryptedByteArray(Base64.decodeBase64(encryptedMigrateContent.itemKey))))
    }.let { decryptedItemKey ->
        encryptionContextProvider.withEncryptionContext(decryptedItemKey) {
            decrypt(EncryptedByteArray(Base64.decodeBase64(encryptedMigrateContent.content)))
        }
    }
}

private object ItemMigrationPayloadMother {

    fun create(
        itemContent: ItemMigrationContent = ItemMigrationContentMother.create(),
        historyContents: List<ItemMigrationHistoryContent> = emptyList()
    ): ItemMigrationPayload = ItemMigrationPayload(
        itemContent = itemContent,
        historyContents = historyContents
    )

}

private object ItemMigrationContentMother {

    fun create(
        encryptedItemContents: EncryptedByteArray = EncryptedByteArray(byteArrayOf()),
        contentFormatVersion: Int = Random.nextInt()
    ): ItemMigrationContent = ItemMigrationContent(
        encryptedItemContents = encryptedItemContents,
        contentFormatVersion = contentFormatVersion
    )

}

private object ItemMigrationHistoryContentMother {

    fun create(
        revision: Long = Random.nextLong(),
        itemContent: ItemMigrationContent = ItemMigrationContentMother.create()
    ): ItemMigrationHistoryContent = ItemMigrationHistoryContent(
        revision = revision,
        itemContent = itemContent
    )

}
