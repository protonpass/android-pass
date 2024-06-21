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

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.PersistentList
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.features.secure.links.R
import proton.android.pass.features.secure.links.list.presentation.SecureLinkModel
import proton.android.pass.features.secure.links.list.ui.SecureLinksListUiEvent
import me.proton.core.presentation.R as CoreR

private const val SECURE_LINKS_GRID_COLUMN_COUNT = 2

@Composable
internal fun SecureLinksListGrid(
    modifier: Modifier = Modifier,
    onUiEvent: (SecureLinksListUiEvent) -> Unit,
    activeSecureLinksModels: PersistentList<SecureLinkModel>,
    inactiveSecureLinksModels: PersistentList<SecureLinkModel>,
    canLoadExternalImages: Boolean
) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(count = SECURE_LINKS_GRID_COLUMN_COUNT),
        horizontalArrangement = Arrangement.spacedBy(space = Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(space = Spacing.small)
    ) {
        secureLinksListGridCellItems(
            secureLinksModels = activeSecureLinksModels,
            canLoadExternalImages = canLoadExternalImages,
            onUiEvent = onUiEvent
        )

        if (inactiveSecureLinksModels.isNotEmpty()) {
            secureLinksListGridHeader(
                textResId = R.string.secure_links_list_header_title_inactive,
                onClick = {}
            )

            secureLinksListGridCellItems(
                secureLinksModels = inactiveSecureLinksModels,
                canLoadExternalImages = canLoadExternalImages,
                onUiEvent = onUiEvent
            )
        }
    }
}

private fun LazyGridScope.secureLinksListGridHeader(@StringRes textResId: Int, onClick: () -> Unit) {
    item(
        span = { GridItemSpan(currentLineSpan = SECURE_LINKS_GRID_COLUMN_COUNT) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = Spacing.medium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = textResId),
                style = ProtonTheme.typography.body2Regular,
                color = PassTheme.colors.textWeak
            )

            IconButton(
                modifier = Modifier.offset(x = Spacing.medium),
                onClick = onClick
            ) {
                Icon(
                    painter = painterResource(id = CoreR.drawable.ic_proton_three_dots_vertical),
                    contentDescription = null,
                    tint = ProtonTheme.colors.iconWeak
                )
            }
        }
    }
}

private fun LazyGridScope.secureLinksListGridCellItems(
    secureLinksModels: List<SecureLinkModel>,
    canLoadExternalImages: Boolean,
    onUiEvent: (SecureLinksListUiEvent) -> Unit
) {
    items(
        items = secureLinksModels,
        key = { secureLinkModel -> secureLinkModel.secureLinkId.id }
    ) { secureLinksModel ->
        SecureLinksListGridCell(
            itemCategory = secureLinksModel.itemCategory,
            title = secureLinksModel.itemTitle,
            website = secureLinksModel.itemWebsite,
            packageName = secureLinksModel.itemPackageName,
            remainingTime = secureLinksModel.remainingTime,
            views = secureLinksModel.views,
            canLoadExternalImages = canLoadExternalImages,
            isEnabled = secureLinksModel.isActive,
            onCellClick = {
                SecureLinksListUiEvent.OnCellClicked(
                    secureLinkId = secureLinksModel.secureLinkId
                ).also(onUiEvent)
            },
            onCellOptionsClick = {
                SecureLinksListUiEvent.OnCellOptionsClicked(
                    secureLinkId = secureLinksModel.secureLinkId
                ).also(onUiEvent)
            }
        )
    }
}
