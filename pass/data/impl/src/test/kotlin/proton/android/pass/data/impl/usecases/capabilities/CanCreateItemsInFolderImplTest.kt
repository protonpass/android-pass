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

package proton.android.pass.data.impl.usecases.capabilities

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.api.usecases.capabilities.CanCreateFolderResult
import proton.android.pass.data.fakes.usecases.FakeCanCreateFolder
import proton.android.pass.data.fakes.usecases.shares.FakeObserveShare
import proton.android.pass.domain.ShareId
import proton.android.pass.test.domain.ShareTestFactory

internal class CanCreateItemsInFolderImplTest {

    private lateinit var instance: CanCreateItemsInFolderImpl

    private lateinit var canCreateFolder: FakeCanCreateFolder
    private lateinit var observeShare: FakeObserveShare

    @Before
    fun setup() {
        canCreateFolder = FakeCanCreateFolder()
        observeShare = FakeObserveShare()

        instance = CanCreateItemsInFolderImpl(
            canCreateFolder = canCreateFolder,
            observeShare = observeShare
        )
    }

    @Test
    fun `vault share with plan allowing folders can create items in folder`() = runTest {
        canCreateFolder.sendValue(CanCreateFolderResult(roleAllows = true, planAllows = true))
        observeShare.emitValue(ShareTestFactory.Vault.create(id = SHARE_ID.id))

        val result = instance(SHARE_ID).first()

        assertThat(result).isTrue()
    }

    @Test
    fun `vault share with plan blocking folders cannot create items in folder`() = runTest {
        canCreateFolder.sendValue(CanCreateFolderResult(roleAllows = true, planAllows = false))
        observeShare.emitValue(ShareTestFactory.Vault.create(id = SHARE_ID.id))

        val result = instance(SHARE_ID).first()

        assertThat(result).isFalse()
    }

    @Test
    fun `item share cannot create items in folder even when plan allows folders`() = runTest {
        canCreateFolder.sendValue(CanCreateFolderResult(roleAllows = true, planAllows = true))
        observeShare.emitValue(ShareTestFactory.Item.create(id = SHARE_ID.id))

        val result = instance(SHARE_ID).first()

        assertThat(result).isFalse()
    }

    private companion object {
        private val SHARE_ID = ShareId("share-id")
    }
}
