/*
 * Copyright (c) 2023-2025 Proton AG
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

package proton.android.pass.features.itemcreate.alias.mailboxes.presentation

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import proton.android.pass.domain.AliasMailbox

@Stable
internal data class SelectMailboxesUiState(
    val mailboxes: PersistentSet<AliasMailbox>,
    val selectedMailboxes: PersistentSet<AliasMailbox>,
    val shouldDisplayFeatureDiscoveryBanner: Boolean,
    val event: SelectMailboxesEvent
) {

    internal companion object {

        internal val Initial = SelectMailboxesUiState(
            mailboxes = persistentSetOf(),
            selectedMailboxes = persistentSetOf(),
            shouldDisplayFeatureDiscoveryBanner = false,
            event = SelectMailboxesEvent.Idle
        )

    }

}
