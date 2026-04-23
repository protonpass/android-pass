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

package proton.android.pass.features.explore.presentation.codes

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId

data class CodesUiState(
    val isLoading: Boolean,
    val rows: ImmutableList<CodeRow>,
    val totalSeconds: Int,
    val remainingSeconds: Int,
    val showPerRowProgress: Boolean,
    val searchQuery: String,
    val inSearchMode: Boolean
) {
    companion object {
        val Initial = CodesUiState(
            isLoading = true,
            rows = persistentListOf(),
            totalSeconds = DEFAULT_TOTAL_SECONDS,
            remainingSeconds = DEFAULT_TOTAL_SECONDS,
            showPerRowProgress = false,
            searchQuery = "",
            inSearchMode = false
        )

        const val DEFAULT_TOTAL_SECONDS = 30
    }
}

data class CodeRow(
    val rowId: String,
    val shareId: ShareId,
    val itemId: ItemId,
    val title: String,
    val subtitle: String,
    val websites: List<String>,
    val packageName: String?,
    val code: String,
    val totalSeconds: Int,
    val remainingSeconds: Int
)
