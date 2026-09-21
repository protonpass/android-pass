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

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.fakes.repositories.FakeItemRepository
import proton.android.pass.data.fakes.usecases.FakeObserveCurrentUser
import proton.android.pass.data.impl.fakes.FakeShareRepository
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareSelection
import proton.android.pass.test.domain.FolderTestFactory
import proton.android.pass.test.domain.UserTestFactory

class ObserveItemCountImplTest {

    private lateinit var observeCurrentUser: FakeObserveCurrentUser
    private lateinit var itemRepository: FakeItemRepository
    private lateinit var shareRepository: FakeShareRepository
    private lateinit var instance: ObserveItemCountImpl

    @Before
    fun setUp() {
        observeCurrentUser = FakeObserveCurrentUser()
        itemRepository = FakeItemRepository()
        shareRepository = FakeShareRepository()

        instance = ObserveItemCountImpl(
            observeCurrentUser = observeCurrentUser,
            itemRepository = itemRepository,
            shareRepository = shareRepository
        )
    }

    @Test
    fun `forwards folderId when ShareSelection is Folder`() = runTest {
        val user = UserTestFactory.create()
        val shareId = ShareId("share-1")
        val folder = FolderTestFactory.create(shareId = shareId)
        val selection = ShareSelection.Folder(shareId, folder.folderId)

        observeCurrentUser.sendUser(user)
        instance(
            itemState = null,
            shareSelection = selection,
            applyItemStateToSharedItems = false,
            includeHiddenVault = false
        ).first()

        val calls = itemRepository.getObserveItemCountSummaryMemory()
        assertThat(calls).hasSize(1)
        assertThat(calls[0].folderId).isEqualTo(folder.folderId)
    }

    @Test
    fun `does not forward folderId when ShareSelection is Share`() = runTest {
        val user = UserTestFactory.create()
        val shareId = ShareId("share-1")
        val selection = ShareSelection.Share(shareId)

        observeCurrentUser.sendUser(user)
        instance(
            itemState = null,
            shareSelection = selection,
            applyItemStateToSharedItems = false,
            includeHiddenVault = false
        ).first()

        val calls = itemRepository.getObserveItemCountSummaryMemory()
        assertThat(calls).hasSize(1)
        assertThat(calls[0].folderId).isNull()
    }

    @Test
    fun `forwards restrictToRootFolder when ShareSelection is Share`() = runTest {
        val user = UserTestFactory.create()
        val shareId = ShareId("share-1")
        val selection = ShareSelection.Share(shareId)

        observeCurrentUser.sendUser(user)
        instance(
            itemState = null,
            shareSelection = selection,
            applyItemStateToSharedItems = false,
            includeHiddenVault = false,
            restrictToRootFolder = true
        ).first()

        val calls = itemRepository.getObserveItemCountSummaryMemory()
        assertThat(calls).hasSize(1)
        assertThat(calls[0].restrictToRootFolder).isTrue()
    }

    @Test
    fun `does not set restrictToRootFolder by default for ShareSelection Share`() = runTest {
        val user = UserTestFactory.create()
        val shareId = ShareId("share-1")
        val selection = ShareSelection.Share(shareId)

        observeCurrentUser.sendUser(user)
        instance(
            itemState = null,
            shareSelection = selection,
            applyItemStateToSharedItems = false,
            includeHiddenVault = false
        ).first()

        val calls = itemRepository.getObserveItemCountSummaryMemory()
        assertThat(calls).hasSize(1)
        assertThat(calls[0].restrictToRootFolder).isFalse()
    }

    @Test
    fun `does not set restrictToRootFolder for ShareSelection Folder even when requested`() = runTest {
        val user = UserTestFactory.create()
        val shareId = ShareId("share-1")
        val folder = FolderTestFactory.create(shareId = shareId)
        val selection = ShareSelection.Folder(shareId, folder.folderId)

        observeCurrentUser.sendUser(user)
        instance(
            itemState = null,
            shareSelection = selection,
            applyItemStateToSharedItems = false,
            includeHiddenVault = false,
            restrictToRootFolder = true
        ).first()

        val calls = itemRepository.getObserveItemCountSummaryMemory()
        assertThat(calls).hasSize(1)
        assertThat(calls[0].restrictToRootFolder).isFalse()
    }

    @Test
    fun `does not forward folderId when ShareSelection is AllShares`() = runTest {
        val user = UserTestFactory.create()
        val selection = ShareSelection.AllShares
        val shareId = ShareId("share-1")

        observeCurrentUser.sendUser(user)
        shareRepository.setUsableShareIdsResult(Result.success(listOf(shareId)))

        instance(
            itemState = null,
            shareSelection = selection,
            applyItemStateToSharedItems = false,
            includeHiddenVault = false
        ).first()

        val calls = itemRepository.getObserveItemCountSummaryMemory()
        assertThat(calls).hasSize(1)
        assertThat(calls[0].folderId).isNull()
    }

    @Test
    fun `does not forward folderId when ShareSelection is Shares`() = runTest {
        val user = UserTestFactory.create()
        val shareIds = listOf(ShareId("share-1"), ShareId("share-2"))
        val selection = ShareSelection.Shares(shareIds)

        observeCurrentUser.sendUser(user)
        instance(
            itemState = null,
            shareSelection = selection,
            applyItemStateToSharedItems = false,
            includeHiddenVault = false
        ).first()

        val calls = itemRepository.getObserveItemCountSummaryMemory()
        assertThat(calls).hasSize(1)
        assertThat(calls[0].folderId).isNull()
    }

    @Test
    fun `passes correct share IDs for Folder selection`() = runTest {
        val user = UserTestFactory.create()
        val shareId = ShareId("share-1")
        val folder = FolderTestFactory.create(shareId = shareId)
        val selection = ShareSelection.Folder(shareId, folder.folderId)

        observeCurrentUser.sendUser(user)
        instance(
            itemState = null,
            shareSelection = selection,
            applyItemStateToSharedItems = false,
            includeHiddenVault = false
        ).first()

        val calls = itemRepository.getObserveItemCountSummaryMemory()
        assertThat(calls).hasSize(1)
        assertThat(calls[0].shareIds).containsExactly(shareId)
    }

    @Test
    fun `forwards all parameters to itemRepository`() = runTest {
        val user = UserTestFactory.create(userId = UserId("test-user"))
        val shareId = ShareId("share-1")
        val folder = FolderTestFactory.create(shareId = shareId)
        val selection = ShareSelection.Folder(shareId, folder.folderId)

        observeCurrentUser.sendUser(user)
        instance(
            itemState = null,
            shareSelection = selection,
            applyItemStateToSharedItems = true,
            includeHiddenVault = true
        ).first()

        val calls = itemRepository.getObserveItemCountSummaryMemory()
        assertThat(calls).hasSize(1)
        val call = calls[0]
        assertThat(call.userId).isEqualTo(user.userId)
        assertThat(call.shareIds).containsExactly(shareId)
        assertThat(call.itemState).isNull()
        assertThat(call.onlyShared).isFalse()
        assertThat(call.applyItemStateToSharedItems).isTrue()
        assertThat(call.includeHiddenVault).isTrue()
        assertThat(call.folderId).isEqualTo(folder.folderId)
    }
}
