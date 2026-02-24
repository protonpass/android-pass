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

package proton.android.pass.features.home

import com.google.common.truth.Truth.assertThat
import kotlinx.collections.immutable.persistentMapOf
import org.junit.Test
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareRole
import proton.android.pass.searchoptions.api.VaultSelectionOption
import proton.android.pass.test.domain.ShareTestFactory

internal class HomeUiStateTest {

    @Test
    fun `selected folder is read-only when selected share is viewer`() {
        val shareId = ShareId("share-id")
        val folderId = FolderId("folder-id")
        val uiState = HomeUiState.Loading.copy(
            homeListUiState = HomeListUiState.Loading.copy(
                homeVaultSelection = VaultSelectionOption.Folder(shareId = shareId, folderId = folderId),
                shares = persistentMapOf(
                    shareId to ShareTestFactory.Vault.create(id = shareId.id, shareRole = ShareRole.Read)
                )
            )
        )

        assertThat(uiState.isSelectedVaultReadOnly()).isTrue()
    }

    @Test
    fun `selected folder is writable when selected share has write permissions`() {
        val shareId = ShareId("share-id")
        val folderId = FolderId("folder-id")
        val uiState = HomeUiState.Loading.copy(
            homeListUiState = HomeListUiState.Loading.copy(
                homeVaultSelection = VaultSelectionOption.Folder(shareId = shareId, folderId = folderId),
                shares = persistentMapOf(
                    shareId to ShareTestFactory.Vault.create(id = shareId.id, shareRole = ShareRole.Admin)
                )
            )
        )

        assertThat(uiState.isSelectedVaultReadOnly()).isFalse()
    }
}
