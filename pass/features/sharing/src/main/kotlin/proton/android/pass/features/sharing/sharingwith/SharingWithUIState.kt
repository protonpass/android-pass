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
import proton.android.pass.domain.GroupId
import proton.android.pass.domain.Share

@Stable
internal data class EnteredEmailUiModel(
    val email: String,
    val isError: Boolean = false,
    val isFocused: Boolean = false
)

@Stable
internal data class EmailUiModel(
    val email: String,
    val isSelected: Boolean = false
) : SuggestionItem {
    override val sortKey: String = email.lowercase()
}

@Stable
internal data class GroupSuggestionUiModel(
    val id: GroupId,
    val email: String,
    val name: String,
    val memberCount: Int,
    val isSelected: Boolean = false,
    val isFocused: Boolean = false
) : SuggestionItem {
    override val sortKey: String = name.lowercase()
}

@Stable
internal sealed interface SuggestionItem {
    val sortKey: String
}

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
    val enteredEmails: ImmutableList<EnteredEmailUiModel> = persistentListOf(),
    val share: Share? = null,
    val event: SharingWithEvents = SharingWithEvents.Idle,
    val isLoading: Boolean = false,
    val showEditVault: Boolean = false,
    val suggestionsUIState: SuggestionsUIState = SuggestionsUIState.Initial,
    val scrollToBottom: Boolean = false,
    val isContinueEnabled: Boolean = false,
    val canOnlyPickFromSelection: Boolean = false,
    val errorMessage: ErrorMessage = ErrorMessage.None
) {
    val selectedGroups: Set<GroupSuggestionUiModel> = (suggestionsUIState as? SuggestionsUIState.Content)
        ?.let { content ->
            (content.recentSortedItems + content.organizationSortedItems)
                .filterIsInstance<GroupSuggestionUiModel>()
                .filter { it.isSelected }
                .toSet()
        }
        ?: emptySet()
}

internal sealed interface SuggestionsUIState {

    @Stable
    data object Initial : SuggestionsUIState

    @Stable
    data object Loading : SuggestionsUIState

    @Stable
    data class Content(
        val groupDisplayName: String = "",
        val recentSortedItems: ImmutableList<SuggestionItem> = persistentListOf(),
        val organizationSortedItems: ImmutableList<SuggestionItem> = persistentListOf()
    ) : SuggestionsUIState

}
