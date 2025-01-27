/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.itemcreate.alias.draftrepositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.domain.AliasSuffix
import javax.inject.Inject

interface SuffixDraftRepository {
    fun addSuffixes(suffixes: Set<AliasSuffix>)
    fun selectSuffixById(id: String)
    fun getSelectedSuffixFlow(): Flow<Option<AliasSuffix>>
    fun getAllSuffixesFlow(): Flow<Set<AliasSuffix>>
    fun clearSuffixes()
}

class SuffixDraftRepositoryImpl @Inject constructor() : SuffixDraftRepository {
    private val suffixes = MutableStateFlow<Set<AliasSuffix>>(emptySet())
    private val selectedSuffixId = MutableStateFlow<String?>(null)

    override fun addSuffixes(suffixes: Set<AliasSuffix>) {
        this.suffixes.update { current -> current + suffixes }
    }

    override fun selectSuffixById(id: String) {
        require(suffixes.value.any { it.suffix == id }) { "Suffix with id $id not found." }
        selectedSuffixId.update { id }
    }

    override fun getSelectedSuffixFlow(): Flow<Option<AliasSuffix>> =
        combine(suffixes, selectedSuffixId) { currentSuffixes, currentSelectedId ->
            currentSelectedId?.let { id ->
                currentSuffixes.find { it.suffix == id }
            }.toOption()
        }

    override fun getAllSuffixesFlow(): Flow<Set<AliasSuffix>> = suffixes

    override fun clearSuffixes() {
        suffixes.update { emptySet() }
        selectedSuffixId.update { null }
    }
}
