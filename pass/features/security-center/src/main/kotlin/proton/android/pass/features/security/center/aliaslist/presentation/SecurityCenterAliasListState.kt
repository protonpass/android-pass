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

package proton.android.pass.features.security.center.aliaslist.presentation

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import proton.android.pass.features.security.center.shared.presentation.EmailBreachUiState

@Stable
internal data class SecurityCenterAliasListState(
    internal val isGlobalMonitorEnabled: Boolean,
    internal val listState: AliasListState,
    internal val event: SecurityCenterAliasListEvent
) {

    internal companion object {
        internal val Initial: SecurityCenterAliasListState = SecurityCenterAliasListState(
            isGlobalMonitorEnabled = true,
            listState = AliasListState.Loading,
            event = SecurityCenterAliasListEvent.Idle
        )
    }
}

@Stable
enum class AliasListError {
    CannotLoad
}

@Stable
sealed interface AliasListState {

    data object Loading : AliasListState

    @JvmInline
    value class Error(val reason: AliasListError) : AliasListState

    data class Success(
        val includedBreachedEmails: ImmutableList<EmailBreachUiState>,
        val includedMonitoredEmails: ImmutableList<EmailBreachUiState>,
        val excludedEmails: ImmutableList<EmailBreachUiState>
    ) : AliasListState
}
