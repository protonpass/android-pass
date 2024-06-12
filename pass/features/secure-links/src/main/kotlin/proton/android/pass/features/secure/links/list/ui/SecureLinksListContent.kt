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

import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.composecomponents.impl.topbar.BackArrowTopAppBar
import proton.android.pass.features.secure.links.R
import proton.android.pass.features.secure.links.list.presentation.SecureLinksListState

@Composable
internal fun SecureLinksListContent(
    modifier: Modifier = Modifier,
    onUiEvent: (SecureLinksListUiEvent) -> Unit,
    state: SecureLinksListState
) = with(state) {
    Scaffold(
        modifier = modifier,
        topBar = {
            BackArrowTopAppBar(
                title = stringResource(id = R.string.secure_links_list_title),
                onUpClick = { onUiEvent(SecureLinksListUiEvent.OnBackClicked) }
            )
        },
    ) { innerPadding ->

    }
}
