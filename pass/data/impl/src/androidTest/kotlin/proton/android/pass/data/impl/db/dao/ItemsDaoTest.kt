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

package proton.android.pass.data.impl.db.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import proton.android.pass.data.impl.db.AppDatabase
import proton.android.pass.data.impl.db.entities.ShareEntity
import proton.android.pass.data.impl.fakes.mother.FolderEntityTestFactory
import proton.android.pass.data.impl.fakes.mother.ItemEntityTestFactory
import proton.android.pass.domain.ItemStateValues
import kotlin.test.assertEquals
import kotlin.test.assertTrue


@RunWith(AndroidJUnit4::class)
class ItemsDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var itemsDao: ItemsDao
    private lateinit var foldersDao: FoldersDao
    private lateinit var sharesDao: SharesDao

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        db.query("PRAGMA foreign_keys=OFF;", null)
        itemsDao = db.itemsDao()
        foldersDao = db.foldersDao()
        sharesDao = db.sharesDao()
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun observeItemsByFolder_returnsItemsInSelectedFolder() = runTest {
        val userId = "user-1"
        val shareId = "share-1"
        val folderId = "folder-root"

        insertFolder(userId, shareId, folderId, parentFolderId = null)
        insertItem(userId, shareId, itemId = "item-in-folder", folderId = folderId)
        insertItem(userId, shareId, itemId = "item-no-folder", folderId = null)

        val result = itemsDao.observeItemsByFolder(
            userId = userId,
            shareId = shareId,
            rootFolderId = folderId,
            itemState = null,
            itemTypes = null,
            applyItemTypes = false,
            setFlags = null,
            clearFlags = null
        ).first()

        assertEquals(1, result.size)
        assertEquals("item-in-folder", result.first().id)
    }

    @Test
    fun observeItemsByFolder_includesDescendantFolderItems() = runTest {
        val userId = "user-1"
        val shareId = "share-1"
        val rootId = "folder-root"
        val childId = "folder-child"
        val grandchildId = "folder-grandchild"

        insertFolder(userId, shareId, rootId, parentFolderId = null)
        insertFolder(userId, shareId, childId, parentFolderId = rootId)
        insertFolder(userId, shareId, grandchildId, parentFolderId = childId)

        insertItem(userId, shareId, itemId = "item-root", folderId = rootId)
        insertItem(userId, shareId, itemId = "item-child", folderId = childId)
        insertItem(userId, shareId, itemId = "item-grandchild", folderId = grandchildId)
        insertItem(userId, shareId, itemId = "item-no-folder", folderId = null)

        val result = itemsDao.observeItemsByFolder(
            userId = userId,
            shareId = shareId,
            rootFolderId = rootId,
            itemState = null,
            itemTypes = null,
            applyItemTypes = false,
            setFlags = null,
            clearFlags = null
        ).first()

        val ids = result.map { it.id }.toSet()
        assertEquals(3, result.size)
        assertTrue(ids.contains("item-root"))
        assertTrue(ids.contains("item-child"))
        assertTrue(ids.contains("item-grandchild"))
    }

    @Test
    fun observeItemsByFolder_excludesSiblingFolderItems() = runTest {
        val userId = "user-1"
        val shareId = "share-1"
        val targetId = "folder-target"
        val siblingId = "folder-sibling"

        insertFolder(userId, shareId, targetId, parentFolderId = null)
        insertFolder(userId, shareId, siblingId, parentFolderId = null)

        insertItem(userId, shareId, itemId = "item-target", folderId = targetId)
        insertItem(userId, shareId, itemId = "item-sibling", folderId = siblingId)

        val result = itemsDao.observeItemsByFolder(
            userId = userId,
            shareId = shareId,
            rootFolderId = targetId,
            itemState = null,
            itemTypes = null,
            applyItemTypes = false,
            setFlags = null,
            clearFlags = null
        ).first()

        assertEquals(1, result.size)
        assertEquals("item-target", result.first().id)
    }

    @Test
    fun observeItemsByFolder_missingRootFolderReturnsEmpty() = runTest {
        val userId = "user-1"
        val shareId = "share-1"

        insertItem(userId, shareId, itemId = "item-1", folderId = "some-other-folder")

        val result = itemsDao.observeItemsByFolder(
            userId = userId,
            shareId = shareId,
            rootFolderId = "non-existent-folder",
            itemState = null,
            itemTypes = null,
            applyItemTypes = false,
            setFlags = null,
            clearFlags = null
        ).first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun observeItemsByFolder_itemStateFilterApplies() = runTest {
        val userId = "user-1"
        val shareId = "share-1"
        val folderId = "folder-1"

        insertFolder(userId, shareId, folderId, parentFolderId = null)
        insertItem(userId, shareId, itemId = "active-item", folderId = folderId, state = ItemStateValues.ACTIVE)
        insertItem(userId, shareId, itemId = "trashed-item", folderId = folderId, state = ItemStateValues.TRASHED)

        val result = itemsDao.observeItemsByFolder(
            userId = userId,
            shareId = shareId,
            rootFolderId = folderId,
            itemState = ItemStateValues.ACTIVE,
            itemTypes = null,
            applyItemTypes = false,
            setFlags = null,
            clearFlags = null
        ).first()

        assertEquals(1, result.size)
        assertEquals("active-item", result.first().id)
    }

    @Test
    fun observeItemsByFolder_itemTypeFilterApplies() = runTest {
        val userId = "user-1"
        val shareId = "share-1"
        val folderId = "folder-1"
        val loginType = 1
        val noteType = 2

        insertFolder(userId, shareId, folderId, parentFolderId = null)
        insertItem(userId, shareId, itemId = "login-item", folderId = folderId, itemType = loginType)
        insertItem(userId, shareId, itemId = "note-item", folderId = folderId, itemType = noteType)

        val result = itemsDao.observeItemsByFolder(
            userId = userId,
            shareId = shareId,
            rootFolderId = folderId,
            itemState = null,
            itemTypes = listOf(loginType),
            applyItemTypes = true,
            setFlags = null,
            clearFlags = null
        ).first()

        assertEquals(1, result.size)
        assertEquals("login-item", result.first().id)
    }

    @Test
    fun observeItemsByFolder_setFlagsFilterApplies() = runTest {
        val userId = "user-1"
        val shareId = "share-1"
        val folderId = "folder-1"
        val targetFlag = 0b0001

        insertFolder(userId, shareId, folderId, parentFolderId = null)
        insertItem(userId, shareId, itemId = "flagged-item", folderId = folderId, flags = targetFlag)
        insertItem(userId, shareId, itemId = "unflagged-item", folderId = folderId, flags = 0)

        val result = itemsDao.observeItemsByFolder(
            userId = userId,
            shareId = shareId,
            rootFolderId = folderId,
            itemState = null,
            itemTypes = null,
            applyItemTypes = false,
            setFlags = targetFlag,
            clearFlags = null
        ).first()

        assertEquals(1, result.size)
        assertEquals("flagged-item", result.first().id)
    }

    @Test
    fun getByVaultIdAndItemId_filtersByShareUserId() = runTest {
        val vaultId = "vault-1"
        val itemId = "same-item-id"
        val allowedUser = "allowed-user"
        val blockedUser = "blocked-user"
        val allowedShare = "share-allowed"
        val blockedShare = "share-blocked"

        insertShare(userId = allowedUser, shareId = allowedShare, vaultId = vaultId)
        insertShare(userId = blockedUser, shareId = blockedShare, vaultId = vaultId)

        insertItem(
            userId = allowedUser,
            shareId = allowedShare,
            itemId = itemId,
            folderId = null
        )
        insertItem(
            userId = blockedUser,
            shareId = blockedShare,
            itemId = itemId,
            folderId = null
        )

        val result = itemsDao.getByVaultIdAndItemId(
            userIds = listOf(allowedUser),
            vaultId = vaultId,
            itemId = itemId
        )

        assertEquals(1, result.size)
        assertEquals(allowedShare, result.first().shareId)
        assertEquals(allowedUser, result.first().userId)
    }

    private suspend fun insertFolder(
        userId: String,
        shareId: String,
        folderId: String,
        parentFolderId: String?
    ) {
        foldersDao.insertOrUpdate(
            FolderEntityTestFactory.create(
                id = folderId,
                userId = userId,
                shareId = shareId,
                vaultId = "vault-1",
                parentFolderId = parentFolderId,
                keyRotation = 1L,
                contentFormatVersion = 1,
                content = "",
                folderKey = ""
            )
        )
    }

    private suspend fun insertItem(
        userId: String,
        shareId: String,
        itemId: String,
        folderId: String?,
        state: Int = ItemStateValues.ACTIVE,
        itemType: Int = 1,
        flags: Int = 0
    ) {
        itemsDao.insertOrUpdate(
            ItemEntityTestFactory.create(
                id = itemId,
                userId = userId,
                addressId = "address-1",
                shareId = shareId,
                folderId = folderId,
                revision = 1L,
                contentFormatVersion = 1,
                keyRotation = 1L,
                content = "",
                key = null,
                state = state,
                itemType = itemType,
                aliasEmail = null,
                createTime = 1000L,
                modifyTime = 1000L,
                lastUsedTime = null,
                encryptedKey = null,
                isPinned = false,
                pinTime = null,
                flags = flags,
                shareCount = 0,
                hasTotp = false,
                hasPasskeys = false
            )
        )
    }

    private suspend fun insertShare(
        userId: String,
        shareId: String,
        vaultId: String
    ) {
        sharesDao.insertOrUpdate(
            ShareEntity(
                id = shareId,
                userId = userId,
                addressId = "address-1",
                vaultId = vaultId,
                groupId = null,
                targetType = 1,
                targetId = "target-id",
                permission = 1,
                content = null,
                contentKeyRotation = null,
                contentFormatVersion = null,
                expirationTime = null,
                createTime = 1L,
                encryptedContent = null,
                isActive = true,
                shareRoleId = "1",
                owner = true,
                targetMembers = 1,
                shared = false,
                targetMaxMembers = 10,
                pendingInvites = 0,
                newUserInvitesReady = 0,
                canAutofill = true,
                flags = 0,
                groupEmail = null
            )
        )
    }
}
