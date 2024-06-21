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

package proton.android.pass.features.secure.links.list.presentation

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState

@Stable
internal data class SecureLinksListState(
    internal val activeSecureLinksModels: PersistentList<SecureLinkModel>,
    internal val inactiveSecureLinksModels: PersistentList<SecureLinkModel>,
    internal val canLoadExternalImages: Boolean,
    internal val isLoadingState: IsLoadingState
) {

    private val hasActiveSecureLinks: Boolean = activeSecureLinksModels.isNotEmpty()

    private val hasInactiveSecureLinks: Boolean = inactiveSecureLinksModels.isNotEmpty()

    internal val hasSecureLinks: Boolean = hasActiveSecureLinks && hasInactiveSecureLinks

    internal companion object {

        internal val Initial: SecureLinksListState = SecureLinksListState(
            activeSecureLinksModels = persistentListOf(),
            inactiveSecureLinksModels = persistentListOf(),
            canLoadExternalImages = false,
            isLoadingState = IsLoadingState.Loading
        )

    }

}
