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

package proton.android.pass.data.impl.extensions

import org.junit.Test
import proton.android.pass.data.impl.fakes.mother.ItemEntityTestFactory
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ItemMapperTest {

    @Test
    fun `toEncryptedDomain maps folderId when present`() {
        val itemEntity = ItemEntityTestFactory.create(folderId = "folder-123")

        val itemEncrypted = itemEntity.toEncryptedDomain()

        assertEquals("folder-123", itemEncrypted.folderId?.id)
    }

    @Test
    fun `toEncryptedDomain maps null folderId when absent`() {
        val itemEntity = ItemEntityTestFactory.create(folderId = null)

        val itemEncrypted = itemEntity.toEncryptedDomain()

        assertNull(itemEncrypted.folderId)
    }
}
