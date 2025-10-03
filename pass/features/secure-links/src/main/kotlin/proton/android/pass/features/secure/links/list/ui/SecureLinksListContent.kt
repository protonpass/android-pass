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

package proton.android.pass.features.secure.links.list.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.commonui.api.Spacing
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.features.secure.links.R
import proton.android.pass.features.secure.links.list.presentation.SecureLinksListState
import proton.android.pass.features.secure.links.list.ui.grid.SecureLinksListGrid
import proton.android.pass.features.secure.links.list.ui.grid.SecureLinksListGridEmpty

@Composable
internal fun SecureLinksListContent(
    modifier: Modifier = Modifier,
    onUiEvent: (SecureLinksListUiEvent) -> Unit,
    state: SecureLinksListState
) = with(state) {
    Scaffold(
        modifier = modifier.systemBarsPadding(),
        topBar = {
            BackArrowTopAppBar(
                title = stringResource(id = R.string.secure_links_list_title),
                onUpClick = { onUiEvent(SecureLinksListUiEvent.OnBackClicked) }
            )
        }
    ) { innerPadding ->
        when (isLoadingState) {
            IsLoadingState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            IsLoadingState.NotLoading -> {
                if (hasSecureLinks) {
                    SecureLinksListGrid(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(paddingValues = innerPadding)
                            .padding(all = Spacing.medium),
                        activeSecureLinksModels = activeSecureLinksModels,
                        inactiveSecureLinksModels = inactiveSecureLinksModels,
                        canLoadExternalImages = canLoadExternalImages,
                        hasActiveSecureLinks = hasActiveSecureLinks,
                        hasInactiveSecureLinks = hasInactiveSecureLinks,
                        onUiEvent = onUiEvent
                    )
                } else {
                    SecureLinksListGridEmpty()
                }
            }
        }
    }
}
