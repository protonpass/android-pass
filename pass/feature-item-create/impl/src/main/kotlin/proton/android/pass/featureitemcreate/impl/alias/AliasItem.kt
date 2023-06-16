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

package proton.android.pass.featureitemcreate.impl.alias

import android.os.Parcelable
import androidx.compose.runtime.Immutable
import kotlinx.parcelize.Parcelize
import proton.android.pass.featureitemcreate.impl.alias.AliasUtils.areAllAliasCharactersValid

@Parcelize
@Immutable
data class AliasItem(
    val title: String = "",
    val prefix: String = "",
    val note: String = "",
    val mailboxTitle: String = "",
    val aliasOptions: AliasOptionsUiModel = AliasOptionsUiModel(emptyList(), emptyList()),
    val selectedSuffix: AliasSuffixUiModel? = null,
    val mailboxes: List<SelectedAliasMailboxUiModel> = emptyList(),
    val aliasToBeCreated: String? = null
) : Parcelable {

    fun validate(allowEmptyTitle: Boolean): Set<AliasItemValidationErrors> {
        val mutableSet = mutableSetOf<AliasItemValidationErrors>()
        if (!allowEmptyTitle) {
            if (title.isBlank()) mutableSet.add(AliasItemValidationErrors.BlankTitle)
        }

        if (prefix.isBlank()) mutableSet.add(AliasItemValidationErrors.BlankPrefix)

        if (prefix.startsWith(".")) mutableSet.add(AliasItemValidationErrors.InvalidAliasContent)

        if (prefix.endsWith(".")) mutableSet.add(AliasItemValidationErrors.InvalidAliasContent)

        if (prefix.contains("..")) mutableSet.add(AliasItemValidationErrors.InvalidAliasContent)

        if (prefix.length > MAX_PREFIX_LENGTH) mutableSet.add(AliasItemValidationErrors.InvalidAliasContent)

        if (!areAllAliasCharactersValid(prefix)) mutableSet.add(AliasItemValidationErrors.InvalidAliasContent)

        if (mailboxes.count { it.selected } == 0) mutableSet.add(AliasItemValidationErrors.NoMailboxes)

        return mutableSet.toSet()
    }

    companion object {
        const val MAX_PREFIX_LENGTH: Int = 40

        val Empty = AliasItem()
    }
}

sealed interface AliasItemValidationErrors {
    object BlankTitle : AliasItemValidationErrors
    object BlankPrefix : AliasItemValidationErrors
    object InvalidAliasContent : AliasItemValidationErrors
    object NoMailboxes : AliasItemValidationErrors
}
