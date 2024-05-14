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

package proton.android.pass.features.security.center.darkweb.presentation

import androidx.compose.runtime.Stable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.domain.breach.CustomEmailId
import proton.android.pass.features.security.center.shared.presentation.EmailBreachUiState

internal sealed interface DarkWebEvent {
    data object Idle : DarkWebEvent

    data class OnVerifyCustomEmail(
        val email: String,
        val customEmailId: CustomEmailId
    ) : DarkWebEvent
}

@Stable
internal sealed interface CustomEmailUiStatus {

    data class Suggestion(
        val usedInLoginsCount: Int,
        val isLoadingState: IsLoadingState
    ) : CustomEmailUiStatus

    @JvmInline
    value class Unverified(val id: CustomEmailId) : CustomEmailUiStatus

    data class Verified(
        val id: CustomEmailId,
        val breachesDetected: Int
    ) : CustomEmailUiStatus {
        internal val hasBreaches: Boolean = breachesDetected > 0
    }
}

@Stable
internal data class CustomEmailUiState(
    internal val email: String,
    internal val status: CustomEmailUiStatus
) {
    internal val key = when (status) {
        is CustomEmailUiStatus.Unverified -> "custom-unverified-$email-${status.id}"
        is CustomEmailUiStatus.Verified -> "custom-verified-$email-${status.id}"
        is CustomEmailUiStatus.Suggestion -> "custom-suggestion-$email"
    }
}

@Stable
internal enum class DarkWebStatus {
    AllGood,
    Warning,
    Loading
}

@Stable
internal enum class DarkWebEmailsError {
    CannotLoad,
    Unknown
}

@Stable
internal sealed interface DarkWebCustomEmailsState {

    data object Loading : DarkWebCustomEmailsState

    @JvmInline
    value class Error(val reason: DarkWebEmailsError) : DarkWebCustomEmailsState

    data class Success(
        val emails: ImmutableList<CustomEmailUiState>,
        val suggestions: ImmutableList<CustomEmailUiState>
    ) : DarkWebCustomEmailsState

    fun count() = when (this) {
        is Success -> emails.size
        else -> 0
    }
}

@Stable
internal sealed interface DarkWebEmailBreachState {

    fun list(): ImmutableList<EmailBreachUiState> = when (this) {
        is Success -> emails
        else -> persistentListOf()
    }

    fun enabledMonitoring(): Boolean = when (this) {
        is Success -> enabledMonitoring
        else -> false
    }

    data object Loading : DarkWebEmailBreachState

    @JvmInline
    value class Error(val reason: DarkWebEmailsError) : DarkWebEmailBreachState

    data class Success(
        val emails: ImmutableList<EmailBreachUiState>,
        val enabledMonitoring: Boolean
    ) : DarkWebEmailBreachState

}

@Stable
internal data class DarkWebUiState(
    internal val protonEmailState: DarkWebEmailBreachState,
    internal val aliasEmailState: DarkWebEmailBreachState,
    internal val customEmailState: DarkWebCustomEmailsState,
    internal val darkWebStatus: DarkWebStatus,
    internal val lastCheckTime: Option<String>,
    internal val canAddCustomEmails: Boolean,
    internal val canNavigateToAlias: Boolean,
    internal val event: DarkWebEvent
) {

    internal companion object {

        internal val Initial = DarkWebUiState(
            protonEmailState = DarkWebEmailBreachState.Loading,
            aliasEmailState = DarkWebEmailBreachState.Loading,
            customEmailState = DarkWebCustomEmailsState.Loading,
            darkWebStatus = DarkWebStatus.Loading,
            lastCheckTime = None,
            canAddCustomEmails = false,
            canNavigateToAlias = false,
            event = DarkWebEvent.Idle
        )

    }

}
