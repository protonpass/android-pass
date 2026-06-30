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

package proton.android.pass.data.impl.work

import com.google.common.truth.Truth.assertThat
import me.proton.core.domain.entity.UserId
import org.junit.Test
import proton.android.pass.domain.ShareId

class FetchItemsWorkerTest {

    @Test
    fun `getRequestFor ForceSync does not include share IDs in WorkManager Data`() {
        val request = FetchItemsWorker.getRequestFor(
            source = FetchItemsWorker.FetchSource.ForceSync,
            userId = UserId("user-1"),
            warnings = FetchItemsWorker.SyncWarnings(
                hasInactiveShares = false,
                hasInvalidGroupShares = false
            )
        )
        assertThat(request.workSpec.input.getStringArray("share_ids")).isNull()
    }

    @Test
    fun `getRequestFor NewShare includes share IDs in WorkManager Data`() {
        val shareIds = setOf(ShareId("share-abc"), ShareId("share-xyz"))
        val request = FetchItemsWorker.getRequestFor(
            source = FetchItemsWorker.FetchSource.NewShare(shareIds),
            userId = UserId("user-1"),
            warnings = FetchItemsWorker.SyncWarnings(
                hasInactiveShares = false,
                hasInvalidGroupShares = false
            )
        )
        val packed = requireNotNull(request.workSpec.input.getStringArray("share_ids"))
        assertThat(packed.toSet()).isEqualTo(shareIds.map { it.id }.toSet())
    }

    @Test
    fun `getRequestFor FirstSync does not include share IDs in WorkManager Data`() {
        val request = FetchItemsWorker.getRequestFor(
            source = FetchItemsWorker.FetchSource.FirstSync,
            userId = UserId("user-1"),
            warnings = FetchItemsWorker.SyncWarnings(
                hasInactiveShares = false,
                hasInvalidGroupShares = false
            )
        )
        assertThat(request.workSpec.input.getStringArray("share_ids")).isNull()
    }
}
