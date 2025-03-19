/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.data.api.usecases

import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.domain.AliasMailbox
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemContents

data class UpdateAliasContent(
    val mailboxes: Option<List<AliasMailbox>>,
    val itemData: ItemContents.Alias,
    private val slNoteOption: Option<String>,
    private val displayNameOption: Option<String>
) {

    val hasSLNote: Boolean = when (slNoteOption) {
        None -> false
        is Some -> true
    }

    val slNote: String = when (slNoteOption) {
        None -> ""
        is Some -> slNoteOption.value
    }

    val hasDisplayName: Boolean = when (displayNameOption) {
        None -> false
        is Some -> true
    }

    val displayName: String = when (displayNameOption) {
        None -> ""
        is Some -> displayNameOption.value
    }

}

interface UpdateAlias {
    suspend operator fun invoke(
        userId: UserId,
        item: Item,
        content: UpdateAliasContent
    ): Item
}
