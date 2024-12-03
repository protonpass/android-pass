/*
 * Copyright (c) 2023 Proton AG
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

package proton.android.pass.features.sharing.sharingwith

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.domain.Share

@Stable
internal data class EnteredEmailState(
    val email: String,
    val isError: Boolean
)

@Stable
internal enum class ErrorMessage {
    NoAddressesCanBeInvited,
    SomeAddressesCannotBeInvited,
    EmailNotValid,
    CannotInviteOutsideOrg,
    EmailAlreadyAdded,
    None
}

@Stable
internal data class SharingWithUIState(
    val enteredEmails: ImmutableList<EnteredEmailState> = persistentListOf(),
    val selectedEmailIndex: Option<Int> = None,
    val share: Share? = null,
    val event: SharingWithEvents = SharingWithEvents.Idle,
    val isLoading: Boolean = false,
    val showEditVault: Boolean = false,
    val suggestionsUIState: SuggestionsUIState = SuggestionsUIState.Initial,
    val scrollToBottom: Boolean = false,
    val isContinueEnabled: Boolean = false,
    val canOnlyPickFromSelection: Boolean = false,
    val errorMessage: ErrorMessage = ErrorMessage.None
)

internal sealed interface SuggestionsUIState {

    @Stable
    data object Initial : SuggestionsUIState

    @Stable
    data object Loading : SuggestionsUIState

    @Stable
    data class Content(
        val groupDisplayName: String = "",
        val recentEmails: ImmutableList<Pair<String, Boolean>> = persistentListOf(),
        val planEmails: ImmutableList<Pair<String, Boolean>> = persistentListOf()
    ) : SuggestionsUIState

}
