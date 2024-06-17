/*
 * Copyright (c) 2024 Proton AG
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

package proton.android.pass.features.secure.links.list.ui.grid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.features.secure.links.list.presentation.SecureLinkModel
import proton.android.pass.features.secure.links.list.ui.SecureLinksListUiEvent

private const val SECURE_LINKS_GRID_COLUMN_COUNT = 2

@Composable
internal fun SecureLinksListGrid(
    modifier: Modifier = Modifier,
    onUiEvent: (SecureLinksListUiEvent) -> Unit,
    secureLinksModels: List<SecureLinkModel>,
    canLoadExternalImages: Boolean
) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(count = SECURE_LINKS_GRID_COLUMN_COUNT),
        horizontalArrangement = Arrangement.spacedBy(space = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        items(items = secureLinksModels) { secureLinksModel ->
            SecureLinksListGridCell(
                itemCategory = secureLinksModel.itemCategory,
                title = secureLinksModel.itemTitle,
                website = secureLinksModel.itemWebsite,
                packageName = secureLinksModel.itemPackageName,
                remainingTime = secureLinksModel.remainingTime,
                views = secureLinksModel.views,
                canLoadExternalImages = canLoadExternalImages,
                onCellClick = { onUiEvent(SecureLinksListUiEvent.OnCellClicked) },
                onCellOptionsClick = { onUiEvent(SecureLinksListUiEvent.OnCellOptionsClicked) }
            )
        }
    }
}
