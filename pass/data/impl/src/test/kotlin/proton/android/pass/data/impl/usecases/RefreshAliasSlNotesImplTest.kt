/*
 * Copyright (c) 2026 Proton AG
 * This file is part of Proton Pass.
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
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Test
import proton.android.pass.data.fakes.repositories.FakeAliasRepository
import proton.android.pass.data.impl.fakes.FakeShareRepository
import proton.android.pass.domain.ShareId
import proton.android.pass.test.domain.ShareTestFactory

internal class RefreshAliasSlNotesImplTest {

    @Test
    fun `passes all shares to the alias repository`() = runTest {
        val userId = UserId("user-id")
        val shareIds = listOf("share-1", "share-2", "share-3")
        val shareRepository = FakeShareRepository().apply {
            emitObserveShares(
                Result.success(shareIds.map { shareId -> ShareTestFactory.Vault.create(id = shareId) })
            )
        }
        val aliasRepository = FakeAliasRepository()
        val instance = RefreshAliasSlNotesImpl(aliasRepository, shareRepository)

        instance(userId)

        assertThat(aliasRepository.getRefreshBulkAliasSlNotesMemory()).containsExactly(
            userId to listOf(ShareId("share-1"), ShareId("share-2"), ShareId("share-3"))
        )
    }
}
