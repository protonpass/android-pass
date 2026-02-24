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
import proton.android.pass.data.impl.fakes.FakeFolderRepository
import proton.android.pass.data.impl.fakes.FakeShareRepository
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareType

internal class RefreshFoldersImplTest {

    private lateinit var folderRepository: FakeFolderRepository
    private lateinit var shareRepository: FakeShareRepository
    private lateinit var instance: RefreshFoldersImpl

    @Before
    fun setup() {
        folderRepository = FakeFolderRepository()
        shareRepository = FakeShareRepository()
        instance = RefreshFoldersImpl(
            folderRepository = folderRepository,
            shareRepository = shareRepository
        )
    }

    @Test
    fun `empty shareIds returns immediately without any repository calls`() = runTest {
        instance.invoke(userId = USER_ID, shareIds = emptySet())

        assertThat(folderRepository.refreshedShareIds).isEmpty()
    }

    @Test
    fun `all vault shares are refreshed`() = runTest {
        val vaultShare1 = ShareId("vault-1")
        val vaultShare2 = ShareId("vault-2")
        shareRepository.setShareType(vaultShare1, ShareType.Vault)
        shareRepository.setShareType(vaultShare2, ShareType.Vault)

        instance.invoke(
            userId = USER_ID,
            shareIds = setOf(vaultShare1, vaultShare2)
        )

        assertThat(folderRepository.refreshedShareIds).containsExactlyElementsIn(
            listOf(vaultShare1, vaultShare2)
        )
    }

    @Test
    fun `item shares are not refreshed`() = runTest {
        val itemShare1 = ShareId("item-1")
        val itemShare2 = ShareId("item-2")
        shareRepository.setShareType(itemShare1, ShareType.Item)
        shareRepository.setShareType(itemShare2, ShareType.Item)

        instance.invoke(
            userId = USER_ID,
            shareIds = setOf(itemShare1, itemShare2)
        )

        assertThat(folderRepository.refreshedShareIds).isEmpty()
    }

    @Test
    fun `only vault shares are refreshed when mixed with item shares`() = runTest {
        val vaultShare = ShareId("vault-1")
        val itemShare = ShareId("item-1")
        shareRepository.setShareType(vaultShare, ShareType.Vault)
        shareRepository.setShareType(itemShare, ShareType.Item)

        instance.invoke(
            userId = USER_ID,
            shareIds = setOf(vaultShare, itemShare)
        )

        assertThat(folderRepository.refreshedShareIds).containsExactly(vaultShare)
    }

    private companion object {
        private val USER_ID = UserId("user-id")
    }
}
