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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import me.proton.core.domain.entity.UserId
import org.junit.Before
import org.junit.Test
import proton.android.pass.data.impl.fakes.FakeFolderRepository
import proton.android.pass.domain.FolderId
import proton.android.pass.domain.ShareId
import proton.android.pass.test.domain.FolderTestFactory

internal class ObserveFolderImplTest {

    private lateinit var folderRepository: FakeFolderRepository
    private lateinit var instance: ObserveFolderImpl

    @Before
    fun setup() {
        folderRepository = FakeFolderRepository()
        instance = ObserveFolderImpl(folderRepository = folderRepository)
    }

    @Test
    fun `delegates to repository and returns observed folder`() = runTest {
        val expectedFolder = FolderTestFactory.create()
        folderRepository.observeFolderResult = expectedFolder

        val result = instance.invoke(
            userId = USER_ID,
            shareId = SHARE_ID,
            folderId = FOLDER_ID
        ).first()

        assertThat(result).isEqualTo(expectedFolder)
        assertThat(folderRepository.lastObserveFolderCall).isEqualTo(
            FakeFolderRepository.ObserveFolderCall(
                userId = USER_ID,
                shareId = SHARE_ID,
                folderId = FOLDER_ID
            )
        )
    }

    private companion object {
        private val USER_ID = UserId("user-id")
        private val SHARE_ID = ShareId("share-id")
        private val FOLDER_ID = FolderId("folder-id")
    }
}
