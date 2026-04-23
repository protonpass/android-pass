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

package proton.android.pass.features.explore.ui.codes

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.explore.presentation.codes.CodeRow
import proton.android.pass.features.explore.presentation.codes.CodesUiState

internal class CodesContentPreviewProvider : PreviewParameterProvider<CodesUiState> {
    override val values: Sequence<CodesUiState> = sequenceOf(
        CodesUiState(
            isLoading = false,
            rows = persistentListOf(
                CodeRow(
                    rowId = "1",
                    shareId = ShareId("share-1"),
                    itemId = ItemId("item-1"),
                    title = "Amazon",
                    subtitle = "eric.norbert@proton.me",
                    websites = listOf("amazon.com"),
                    packageName = null,
                    code = "920827",
                    totalSeconds = 30,
                    remainingSeconds = 23
                ),
                CodeRow(
                    rowId = "2",
                    shareId = ShareId("share-1"),
                    itemId = ItemId("item-2"),
                    title = "issuer.com",
                    subtitle = "moldypotato63",
                    websites = emptyList(),
                    packageName = null,
                    code = "745523",
                    totalSeconds = 30,
                    remainingSeconds = 23
                )
            ),
            totalSeconds = 30,
            remainingSeconds = 23,
            showPerRowProgress = false,
            searchQuery = "",
            inSearchMode = false
        ),
        CodesUiState(
            isLoading = false,
            rows = persistentListOf(
                CodeRow(
                    rowId = "1",
                    shareId = ShareId("share-1"),
                    itemId = ItemId("item-1"),
                    title = "Amazon",
                    subtitle = "eric.norbert@proton.me",
                    websites = listOf("amazon.com"),
                    packageName = null,
                    code = "920827",
                    totalSeconds = 30,
                    remainingSeconds = 23
                ),
                CodeRow(
                    rowId = "2",
                    shareId = ShareId("share-1"),
                    itemId = ItemId("item-2"),
                    title = "issuer.com",
                    subtitle = "moldypotato63",
                    websites = emptyList(),
                    packageName = null,
                    code = "745523",
                    totalSeconds = 60,
                    remainingSeconds = 47
                )
            ),
            totalSeconds = 30,
            remainingSeconds = 23,
            showPerRowProgress = true,
            searchQuery = "",
            inSearchMode = false
        )
    )
}
