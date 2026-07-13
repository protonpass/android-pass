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

package proton.android.pass.data.impl.local

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import proton.android.pass.data.impl.fakes.mother.ItemEntityTestFactory

class LocalItemDataSourceSlNoteTest {

    @Test
    fun `alias refreshed from the Pass API keeps the stored SL note`() {
        val incoming = ItemEntityTestFactory.create(
            id = ALIAS_ITEM_ID,
            shareId = SHARE_ID,
            aliasEmail = "alias@passmail.com",
            slNote = null
        )

        val result = mergeStoredSlNotes(
            items = listOf(incoming),
            storedSlNotes = mapOf(shareItemKey(SHARE_ID, ALIAS_ITEM_ID) to STORED_NOTE)
        )

        assertThat(result.single().slNote).isEqualTo(STORED_NOTE)
    }

    @Test
    fun `an incoming SL note wins over the stored one`() {
        val incoming = ItemEntityTestFactory.create(
            id = ALIAS_ITEM_ID,
            shareId = SHARE_ID,
            aliasEmail = "alias@passmail.com",
            slNote = INCOMING_NOTE
        )

        val result = mergeStoredSlNotes(
            items = listOf(incoming),
            storedSlNotes = mapOf(shareItemKey(SHARE_ID, ALIAS_ITEM_ID) to STORED_NOTE)
        )

        assertThat(result.single().slNote).isEqualTo(INCOMING_NOTE)
    }

    @Test
    fun `stored note of another item is never applied`() {
        val incoming = ItemEntityTestFactory.create(
            id = ALIAS_ITEM_ID,
            shareId = SHARE_ID,
            aliasEmail = "alias@passmail.com",
            slNote = null
        )

        val result = mergeStoredSlNotes(
            items = listOf(incoming),
            storedSlNotes = mapOf(shareItemKey(SHARE_ID, "another-item-id") to STORED_NOTE)
        )

        assertThat(result.single().slNote).isNull()
    }

    @Test
    fun `non alias items are left untouched`() {
        val login = ItemEntityTestFactory.create(
            id = ALIAS_ITEM_ID,
            shareId = SHARE_ID,
            aliasEmail = null,
            slNote = null
        )

        val result = mergeStoredSlNotes(
            items = listOf(login),
            storedSlNotes = mapOf(shareItemKey(SHARE_ID, ALIAS_ITEM_ID) to STORED_NOTE)
        )

        assertThat(result.single().slNote).isNull()
    }

    private companion object {
        const val SHARE_ID = "share-id"
        const val ALIAS_ITEM_ID = "alias-item-id"
        const val STORED_NOTE = "encrypted-stored-note"
        const val INCOMING_NOTE = "encrypted-incoming-note"
    }
}
