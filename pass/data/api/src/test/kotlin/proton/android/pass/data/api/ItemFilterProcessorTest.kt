/*
 * Copyright (c) 2024-2026 Proton AG
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

package proton.android.pass.data.api

import kotlinx.datetime.Clock
import me.proton.core.crypto.common.keystore.EncryptedByteArray
import me.proton.core.domain.entity.UserId
import org.junit.Test
import proton.android.pass.common.api.None
import proton.android.pass.data.api.usecases.ItemData
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemFlags
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.Share
import proton.android.pass.domain.ShareColor
import proton.android.pass.domain.ShareFlags
import proton.android.pass.domain.ShareIcon
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.SharePermission
import proton.android.pass.domain.ShareRole
import proton.android.pass.domain.ShareType
import proton.android.pass.domain.VaultId
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ItemFilterProcessorTest {

    private fun createVaultShare(
        id: String,
        vaultId: String,
        isOwner: Boolean = false,
        shareRole: ShareRole = ShareRole.Read
    ): Share.Vault = Share.Vault(
        id = ShareId(id),
        userId = UserId("user-1"),
        targetId = "target-1",
        permission = SharePermission(0),
        vaultId = VaultId(vaultId),
        groupId = null,
        groupEmail = null,
        expirationTime = null,
        createTime = Date(),
        shareRole = shareRole,
        isOwner = isOwner,
        memberCount = 1,
        shared = false,
        maxMembers = 10,
        pendingInvites = 0,
        newUserInvitesReady = 0,
        canAutofill = true,
        name = "Test Vault",
        color = ShareColor.Color1,
        icon = ShareIcon.Icon1,
        shareFlags = ShareFlags(0)
    )

    private fun createItemShare(
        id: String,
        vaultId: String,
        isOwner: Boolean = false,
        shareRole: ShareRole = ShareRole.Read
    ): Share.Item = Share.Item(
        id = ShareId(id),
        userId = UserId("user-1"),
        targetId = "target-1",
        permission = SharePermission(0),
        vaultId = VaultId(vaultId),
        groupId = null,
        groupEmail = null,
        expirationTime = null,
        createTime = Date(),
        shareRole = shareRole,
        isOwner = isOwner,
        memberCount = 1,
        shared = false,
        maxMembers = 10,
        pendingInvites = 0,
        newUserInvitesReady = 0,
        canAutofill = true,
        shareFlags = ShareFlags(0)
    )

    private fun createItem(
        itemId: String,
        shareId: ShareId,
        itemUuid: String = "uuid-$itemId"
    ): Item {
        val now = Clock.System.now()
        return Item(
            id = ItemId(itemId),
            userId = UserId("user-1"),
            itemUuid = itemUuid,
            revision = 1,
            shareId = shareId,
            itemType = ItemType.Password,
            title = "title-$itemId",
            note = "note",
            content = EncryptedByteArray(byteArrayOf()),
            state = 0,
            packageInfoSet = emptySet(),
            createTime = now,
            modificationTime = now,
            lastAutofillTime = None,
            isPinned = false,
            pinTime = None,
            itemFlags = ItemFlags(0),
            shareCount = 0,
            shareType = ShareType.Vault
        )
    }

    @Test
    fun `empty input returns empty list`() {
        val result = ItemFilterProcessor.removeDuplicates(emptyArray<Pair<List<Share>, List<ItemData>>>())
        assertTrue(result.isEmpty())
    }

    @Test
    fun `single vault share with items returns all items`() {
        val share = createVaultShare("share-1", "vault-1", isOwner = true, shareRole = ShareRole.Admin)
        val item1 = createItem("item-1", share.id)
        val item2 = createItem("item-2", share.id)

        val input = arrayOf<Pair<List<Share>, List<ItemData>>>(
            listOf<Share>(share) to listOf(
                ItemData.DefaultItem(item1),
                ItemData.DefaultItem(item2)
            )
        )

        val result = ItemFilterProcessor.removeDuplicates(input)

        assertEquals(2, result.size)
        assertEquals(item1.id, result[0].item.id)
        assertEquals(item2.id, result[1].item.id)
    }

    @Test
    fun `duplicate vault shares by vaultId keeps only share with owner=true`() {
        val vaultId = "vault-1"
        val ownerShare = createVaultShare("share-owner", vaultId, isOwner = true, shareRole = ShareRole.Admin)
        val memberShare = createVaultShare("share-member", vaultId, isOwner = false, shareRole = ShareRole.Admin)

        val ownerItem = createItem("item-1", ownerShare.id)
        val memberItem = createItem("item-2", memberShare.id)

        val input = arrayOf<Pair<List<Share>, List<ItemData>>>(
            listOf<Share>(ownerShare) to listOf(ItemData.DefaultItem(ownerItem)),
            listOf<Share>(memberShare) to listOf(ItemData.DefaultItem(memberItem))
        )

        val result = ItemFilterProcessor.removeDuplicates(input)

        // Only items from owner share should remain
        assertEquals(1, result.size)
        assertEquals(ownerItem.id, result[0].item.id)
    }

    @Test
    fun `duplicate vault shares by vaultId with same ownership keeps share with better role`() {
        val vaultId = "vault-1"
        val adminShare = createVaultShare("share-admin", vaultId, isOwner = false, shareRole = ShareRole.Admin)
        val writerShare = createVaultShare("share-writer", vaultId, isOwner = false, shareRole = ShareRole.Write)
        val readerShare = createVaultShare("share-reader", vaultId, isOwner = false, shareRole = ShareRole.Read)

        val adminItem = createItem("item-admin", adminShare.id)
        val writerItem = createItem("item-writer", writerShare.id)
        val readerItem = createItem("item-reader", readerShare.id)

        val input = arrayOf<Pair<List<Share>, List<ItemData>>>(
            listOf<Share>(writerShare) to listOf(ItemData.DefaultItem(writerItem)),
            listOf<Share>(readerShare) to listOf(ItemData.DefaultItem(readerItem)),
            listOf<Share>(adminShare) to listOf(ItemData.DefaultItem(adminItem))
        )

        val result = ItemFilterProcessor.removeDuplicates(input)

        // Only item from admin share should remain (best role)
        assertEquals(1, result.size)
        assertEquals(adminItem.id, result[0].item.id)
    }

    @Test
    fun `item shares are not deduplicated by vaultId`() {
        val vaultId = "vault-1"
        val itemShare1 = createItemShare("share-item-1", vaultId, isOwner = true, shareRole = ShareRole.Admin)
        val itemShare2 = createItemShare("share-item-2", vaultId, isOwner = false, shareRole = ShareRole.Write)

        val item1 = createItem("item-1", itemShare1.id)
        val item2 = createItem("item-2", itemShare2.id)

        val input = arrayOf<Pair<List<Share>, List<ItemData>>>(
            listOf<Share>(itemShare1) to listOf(ItemData.DefaultItem(item1)),
            listOf<Share>(itemShare2) to listOf(ItemData.DefaultItem(item2))
        )

        val result = ItemFilterProcessor.removeDuplicates(input)

        // Both items should be kept (Share.Item not deduplicated)
        assertEquals(2, result.size)
    }

    @Test
    fun `items with same itemUuid are deduplicated keeping item from owner share`() {
        val itemUuid = "same-uuid"
        val ownerShare = createVaultShare("share-owner", "vault-1", isOwner = true, shareRole = ShareRole.Admin)
        val memberShare = createVaultShare("share-member", "vault-2", isOwner = false, shareRole = ShareRole.Admin)

        val ownerItem = createItem("owner-item", ownerShare.id, itemUuid)
        val memberItem = createItem("member-item", memberShare.id, itemUuid)

        val input = arrayOf<Pair<List<Share>, List<ItemData>>>(
            listOf<Share>(ownerShare) to listOf(ItemData.DefaultItem(ownerItem)),
            listOf<Share>(memberShare) to listOf(ItemData.DefaultItem(memberItem))
        )

        val result = ItemFilterProcessor.removeDuplicates(input)

        // Only item from owner share should remain
        assertEquals(1, result.size)
        assertEquals(ownerItem.id, result[0].item.id)
    }

    @Test
    fun `items with same itemUuid and ownership are deduplicated keeping item from better role share`() {
        val itemUuid = "same-uuid"
        val adminShare = createVaultShare("share-admin", "vault-1", isOwner = false, shareRole = ShareRole.Admin)
        val writerShare = createVaultShare("share-writer", "vault-2", isOwner = false, shareRole = ShareRole.Write)

        val adminItem = createItem("admin-item", adminShare.id, itemUuid)
        val writerItem = createItem("writer-item", writerShare.id, itemUuid)

        val input = arrayOf<Pair<List<Share>, List<ItemData>>>(
            listOf<Share>(writerShare) to listOf(ItemData.DefaultItem(writerItem)),
            listOf<Share>(adminShare) to listOf(ItemData.DefaultItem(adminItem))
        )

        val result = ItemFilterProcessor.removeDuplicates(input)

        // Only item from admin share should remain (better role)
        assertEquals(1, result.size)
        assertEquals(adminItem.id, result[0].item.id)
    }

    @Test
    fun `items with different itemUuid are not deduplicated`() {
        val share = createVaultShare("share-1", "vault-1", isOwner = true, shareRole = ShareRole.Admin)

        val item1 = createItem("item-1", share.id, "uuid-1")
        val item2 = createItem("item-2", share.id, "uuid-2")

        val input = arrayOf<Pair<List<Share>, List<ItemData>>>(
            listOf<Share>(share) to listOf(
                ItemData.DefaultItem(item1),
                ItemData.DefaultItem(item2)
            )
        )

        val result = ItemFilterProcessor.removeDuplicates(input)

        assertEquals(2, result.size)
    }

    @Test
    fun `mixed vault and item shares with vault deduplication`() {
        val vaultId = "vault-1"
        val vaultShare1 = createVaultShare("vault-share-1", vaultId, isOwner = true, shareRole = ShareRole.Admin)
        val vaultShare2 = createVaultShare("vault-share-2", vaultId, isOwner = false, shareRole = ShareRole.Write)
        val itemShare = createItemShare("item-share-1", vaultId)

        val vaultItem1 = createItem("v-item-1", vaultShare1.id)
        val vaultItem2 = createItem("v-item-2", vaultShare2.id)
        val itemShareItem = createItem("i-item-1", itemShare.id)

        val input = arrayOf<Pair<List<Share>, List<ItemData>>>(
            listOf<Share>(vaultShare1) to listOf(ItemData.DefaultItem(vaultItem1)),
            listOf<Share>(vaultShare2) to listOf(ItemData.DefaultItem(vaultItem2)),
            listOf<Share>(itemShare) to listOf(ItemData.DefaultItem(itemShareItem))
        )

        val result = ItemFilterProcessor.removeDuplicates(input)

        // Vault shares deduplicated (keep owner), item share kept
        assertEquals(2, result.size)
        assertTrue(result.any { it.item.id == vaultItem1.id })
        assertTrue(result.any { it.item.id == itemShareItem.id })
    }

    @Test
    fun `complex scenario with multiple vaults and item deduplication`() {
        val itemUuid = "shared-item-uuid"

        // Vault 1: owner share
        val vault1Owner = createVaultShare("v1-owner", "vault-1", isOwner = true, shareRole = ShareRole.Admin)

        // Vault 2: admin and writer shares
        val vault2Admin = createVaultShare("v2-admin", "vault-2", isOwner = false, shareRole = ShareRole.Admin)
        val vault2Writer = createVaultShare("v2-writer", "vault-2", isOwner = false, shareRole = ShareRole.Write)

        // Items with same UUID in different vaults
        val v1Item = createItem("v1-item", vault1Owner.id, itemUuid)
        val v2ItemAdmin = createItem("v2-admin-item", vault2Admin.id, itemUuid)
        val v2ItemWriter = createItem("v2-writer-item", vault2Writer.id, itemUuid)

        // Unique items
        val uniqueItem = createItem("unique-item", vault1Owner.id, "unique-uuid")

        val input = arrayOf<Pair<List<Share>, List<ItemData>>>(
            listOf<Share>(vault1Owner) to listOf(
                ItemData.DefaultItem(v1Item),
                ItemData.DefaultItem(uniqueItem)
            ),
            listOf<Share>(vault2Admin) to listOf(ItemData.DefaultItem(v2ItemAdmin)),
            listOf<Share>(vault2Writer) to listOf(ItemData.DefaultItem(v2ItemWriter))
        )

        val result = ItemFilterProcessor.removeDuplicates(input)

        // Vault 2 shares deduplicated (admin kept), then items deduplicated (owner kept)
        assertEquals(2, result.size)
        assertTrue(result.any { it.item.id == v1Item.id }) // Owner item kept
        assertTrue(result.any { it.item.id == uniqueItem.id }) // Unique item kept
    }

    @Test
    fun `items from filtered shares are excluded`() {
        val vaultId = "vault-1"
        val ownerShare = createVaultShare("share-owner", vaultId, isOwner = true, shareRole = ShareRole.Admin)
        val memberShare = createVaultShare("share-member", vaultId, isOwner = false, shareRole = ShareRole.Write)

        val ownerItem = createItem("owner-item", ownerShare.id)
        val memberItem1 = createItem("member-item-1", memberShare.id)
        val memberItem2 = createItem("member-item-2", memberShare.id)

        val input = arrayOf<Pair<List<Share>, List<ItemData>>>(
            listOf<Share>(ownerShare) to listOf(ItemData.DefaultItem(ownerItem)),
            listOf<Share>(memberShare) to listOf(
                ItemData.DefaultItem(memberItem1),
                ItemData.DefaultItem(memberItem2)
            )
        )

        val result = ItemFilterProcessor.removeDuplicates(input)

        // Only owner share kept, so member items excluded
        assertEquals(1, result.size)
        assertEquals(ownerItem.id, result[0].item.id)
    }

    @Test
    fun `multiple different vaults all kept`() {
        val vault1 = createVaultShare("share-1", "vault-1")
        val vault2 = createVaultShare("share-2", "vault-2")
        val vault3 = createVaultShare("share-3", "vault-3")

        val item1 = createItem("item-1", vault1.id)
        val item2 = createItem("item-2", vault2.id)
        val item3 = createItem("item-3", vault3.id)

        val input = arrayOf<Pair<List<Share>, List<ItemData>>>(
            listOf<Share>(vault1) to listOf(ItemData.DefaultItem(item1)),
            listOf<Share>(vault2) to listOf(ItemData.DefaultItem(item2)),
            listOf<Share>(vault3) to listOf(ItemData.DefaultItem(item3))
        )

        val result = ItemFilterProcessor.removeDuplicates(input)

        assertEquals(3, result.size)
    }
}
