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

package proton.android.pass.features.secure.links.shared.navigation

import proton.android.pass.domain.items.ItemCategory
import proton.android.pass.domain.securelinks.SecureLinkId
import proton.android.pass.features.secure.links.overview.navigation.SecureLinksOverviewNavScope

sealed interface SecureLinksNavDestination {

    data object CloseScreen : SecureLinksNavDestination

    @JvmInline
    value class CloseScreenWithCategory(val itemCategory: ItemCategory) : SecureLinksNavDestination

    data object DismissBottomSheet : SecureLinksNavDestination

    data object Profile : SecureLinksNavDestination

    data class SecureLinkOverview(
        val secureLinkId: SecureLinkId,
        val scope: SecureLinksOverviewNavScope
    ) : SecureLinksNavDestination

    data object SecureLinksList : SecureLinksNavDestination

    @JvmInline
    value class SecureLinksListMenu(val secureLinkId: SecureLinkId?) : SecureLinksNavDestination

}
