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

package proton.android.pass.data.impl.usecases.folders

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.api.usecases.folders.FolderPresence
import proton.android.pass.data.impl.fakes.FakeRemoteFolderDataSource
import proton.android.pass.data.impl.fakes.FakeShareRepository
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType

internal class HasAnyFoldersImplTest {

    private lateinit var remoteFolderDataSource: FakeRemoteFolderDataSource
    private lateinit var shareRepository: FakeShareRepository
    private lateinit var instance: HasAnyFoldersImpl

    @Before
    fun setup() {
        remoteFolderDataSource = FakeRemoteFolderDataSource()
        shareRepository = FakeShareRepository()
        instance = HasAnyFoldersImpl(
            remoteFolderDataSource = remoteFolderDataSource,
            shareRepository = shareRepository
        )
    }

    @Test
    fun `no shares is unknown, not a no`() = runTest {
        val result = instance(USER_ID, emptySet())

        assertThat(result).isEqualTo(FolderPresence.Unknown)
        assertThat(remoteFolderDataSource.countFoldersCalls).isEmpty()
    }

    @Test
    fun `only non vault shares is unknown, not a no`() = runTest {
        val itemShare = ShareId("item-1")
        shareRepository.setShareType(itemShare, ShareType.Item)

        val result = instance(USER_ID, setOf(itemShare))

        assertThat(result).isEqualTo(FolderPresence.Unknown)
        assertThat(remoteFolderDataSource.countFoldersCalls).isEmpty()
    }

    @Test
    fun `reports folders when a share has a non zero total`() = runTest {
        val share = vaultShare("vault-1")
        remoteFolderDataSource.countFoldersResults[share] = Result.success(4L)

        val result = instance(USER_ID, setOf(share))

        assertThat(result).isEqualTo(FolderPresence.HasFolders)
    }

    @Test
    fun `reports no folders when every share answers zero`() = runTest {
        val shares = (1..3).map { index -> vaultShare("vault-$index") }
        shares.forEach { share -> remoteFolderDataSource.countFoldersResults[share] = Result.success(0L) }

        val result = instance(USER_ID, shares.toSet())

        assertThat(result).isEqualTo(FolderPresence.NoFolders)
        assertThat(remoteFolderDataSource.countFoldersCalls).hasSize(3)
    }

    @Test
    fun `a share that fails to answer makes the result unknown, not no`() = runTest {
        val answering = vaultShare("vault-1")
        val failing = vaultShare("vault-2")
        remoteFolderDataSource.countFoldersResults[answering] = Result.success(0L)
        remoteFolderDataSource.countFoldersResults[failing] = Result.failure(RuntimeException("boom"))

        val result = instance(USER_ID, setOf(answering, failing))

        assertThat(result).isEqualTo(FolderPresence.Unknown)
    }

    @Test
    fun `stops querying once folders are found`() = runTest {
        val shares = (1..6).map { index -> vaultShare("vault-$index") }
        shares.forEach { share -> remoteFolderDataSource.countFoldersResults[share] = Result.success(0L) }
        remoteFolderDataSource.countFoldersResults[shares.first()] = Result.success(1L)

        val result = instance(USER_ID, shares.toSet())

        assertThat(result).isEqualTo(FolderPresence.HasFolders)
        assertThat(remoteFolderDataSource.countFoldersCalls).hasSize(3)
    }

    @Test
    fun `folders found outweigh a share that failed to answer`() = runTest {
        val withFolders = vaultShare("vault-1")
        val failing = vaultShare("vault-2")
        remoteFolderDataSource.countFoldersResults[withFolders] = Result.success(2L)
        remoteFolderDataSource.countFoldersResults[failing] = Result.failure(RuntimeException("boom"))

        val result = instance(USER_ID, setOf(withFolders, failing))

        assertThat(result).isEqualTo(FolderPresence.HasFolders)
    }

    private fun vaultShare(id: String): ShareId = ShareId(id)
        .also { shareId -> shareRepository.setShareType(shareId, ShareType.Vault) }

    private companion object {

        private val USER_ID = UserId("user-id")

    }
}
