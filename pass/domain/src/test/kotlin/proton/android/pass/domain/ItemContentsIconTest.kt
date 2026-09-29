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

package proton.android.pass.domain

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ItemContentsIconTest {

    private val note = ItemContents.Note(title = "title", note = "note", customFields = emptyList())

    @Test
    fun `withIcon sets the icon and keeps the concrete type`() {
        val empty = HiddenState.Empty("")
        listOf(
            note,
            ItemContents.Login.create(password = empty, primaryTotp = empty),
            ItemContents.CreditCard.default(cvv = empty, pin = empty),
            ItemContents.Custom(title = "", note = "", customFields = emptyList(), sectionContentList = emptyList()),
            ItemContents.Unknown(title = "", note = "", customFields = emptyList())
        ).forEach { contents ->
            val withIcon = contents.withIcon(ICON)

            assertThat(withIcon.icon).isEqualTo(ICON)
            assertThat(withIcon::class).isEqualTo(contents::class)
            assertThat(withIcon.withIcon(null)).isEqualTo(contents)
        }
    }

    @Test
    fun `items with a different icon are not equal`() {
        assertThat(areItemContentsEqual(note, note.copy(icon = ICON), decrypt = { it })).isFalse()
        assertThat(areItemContentsEqual(note.copy(icon = ICON), note, decrypt = { it })).isFalse()
    }

    @Test
    fun `items with the same icon are equal`() {
        assertThat(areItemContentsEqual(note.copy(icon = ICON), note.copy(icon = ICON), decrypt = { it })).isTrue()
        assertThat(areItemContentsEqual(note, note.copy(), decrypt = { it })).isTrue()
    }

    private companion object {
        const val ICON = "data:image/png;base64,QUJD"
    }
}
