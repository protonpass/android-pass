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

import kotlinx.coroutines.flow.Flow
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.domain.ItemBase

interface GetSuggestedAutofillItems {
    operator fun invoke(
        itemTypeFilter: ItemTypeFilter,
        suggestion: Option<Suggestion> = None,
        userId: Option<UserId> = None
    ): Flow<SuggestedAutofillItemsResult>
}

sealed interface SuggestedAutofillItemsResult {
    @JvmInline
    value class Items(val suggestedItems: List<SuggestedItem>) : SuggestedAutofillItemsResult
    data object ShowUpgrade : SuggestedAutofillItemsResult
}

sealed interface Suggestion {
    val value: String

    @JvmInline
    value class PackageName(override val value: String) : Suggestion

    data class Url(
        override val value: String,
        val isDALSuggestion: Boolean = false
    ) : Suggestion
}

data class SuggestedItem(
    private val item: ItemBase,
    val suggestion: Option<Suggestion>
) : ItemBase by item {
    val isDALSuggestion: Boolean
        get() = suggestion is Some && (suggestion.value as? Suggestion.Url)?.isDALSuggestion ?: false
}
