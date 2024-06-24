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

package proton.android.pass.features.secure.links.listmenu.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.features.secure.links.listmenu.presentation.SecureLinksListMenuModel
import proton.android.pass.features.secure.links.listmenu.presentation.SecureLinksListMenuState
import proton.android.pass.features.secure.links.listmenu.ui.options.SecureLinksListMenuOptionsAllLinks
import proton.android.pass.features.secure.links.listmenu.ui.options.SecureLinksListMenuOptionsSingleLink

@Composable
internal fun SecureLinksListMenuContent(
    modifier: Modifier = Modifier,
    state: SecureLinksListMenuState,
    onUiEvent: (SecureLinksListMenuUiEvent) -> Unit
) = with(state) {
    when (menuModel) {
        SecureLinksListMenuModel.AllInactiveSecureLinks -> {
            SecureLinksListMenuOptionsAllLinks(
                modifier = modifier,
                action = action,
                onUiEvent = onUiEvent
            )
        }

        is SecureLinksListMenuModel.SingleSecureLink -> {
            SecureLinksListMenuOptionsSingleLink(
                modifier = modifier,
                isLinkActive = menuModel.isLinkActive,
                action = action,
                onUiEvent = onUiEvent
            )
        }
    }
}
