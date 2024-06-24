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

package proton.android.pass.features.secure.links.listmenu.presentation

import androidx.compose.runtime.Stable
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetItemAction
import proton.android.pass.domain.securelinks.SecureLink

@Stable
internal data class SecureLinksListMenuState(
    internal val action: BottomSheetItemAction,
    internal val event: SecureLinksListMenuEvent,
    private val secureLinkOption: Option<SecureLink>
) {

    internal val menuModel: SecureLinksListMenuModel = when (secureLinkOption) {
        None -> SecureLinksListMenuModel.AllInactiveSecureLinks
        is Some -> SecureLinksListMenuModel.SingleSecureLink(
            isLinkActive = secureLinkOption.value.isActive
        )
    }

    internal val secureLinkUrl: String = when (secureLinkOption) {
        None -> ""
        is Some -> secureLinkOption.value.url
    }

    internal companion object {

        internal val Initial: SecureLinksListMenuState = SecureLinksListMenuState(
            action = BottomSheetItemAction.None,
            event = SecureLinksListMenuEvent.Idle,
            secureLinkOption = None
        )

    }

}
