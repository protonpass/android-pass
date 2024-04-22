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
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.features.security.center.shared.presentation.EmailBreachUiState

@Stable
sealed interface CustomEmailUiStatus {

    @JvmInline
    value class Suggestion(val usedInLoginsCount: Int) : CustomEmailUiStatus

    @JvmInline
    value class Unverified(val id: BreachEmailId.Custom) : CustomEmailUiStatus

    data class Verified(
        val id: BreachEmailId.Custom,
        val breachesDetected: Int
    ) : CustomEmailUiStatus {
        internal val hasBreaches: Boolean = breachesDetected > 0
    }
}

@Stable
data class CustomEmailUiState(
    val email: String,
    val status: CustomEmailUiStatus
)

@Stable
enum class DarkWebStatus {
    AllGood,
    Warning,
    Loading
}

@Stable
enum class DarkWebEmailsError {
    CannotLoad,
    Unknown
}

@Stable
sealed interface DarkWebCustomEmailsState {
    data object Loading : DarkWebCustomEmailsState

    @JvmInline
    value class Error(val reason: DarkWebEmailsError) : DarkWebCustomEmailsState

    data class Success(
        val emails: ImmutableList<CustomEmailUiState>,
        val suggestions: ImmutableList<CustomEmailUiState>
    ) : DarkWebCustomEmailsState
}

@Stable
sealed interface DarkWebEmailBreachState {
    fun list(): ImmutableList<EmailBreachUiState> = when (this) {
        is Success -> emails
        else -> persistentListOf()
    }

    data object Loading : DarkWebEmailBreachState

    @JvmInline
    value class Error(val reason: DarkWebEmailsError) : DarkWebEmailBreachState

    @JvmInline
    value class Success(val emails: ImmutableList<EmailBreachUiState>) : DarkWebEmailBreachState
}

@Stable
data class DarkWebUiState(
    val protonEmailState: DarkWebEmailBreachState,
    val aliasEmailState: DarkWebEmailBreachState,
    val customEmailState: DarkWebCustomEmailsState,
    val darkWebStatus: DarkWebStatus,
    val lastCheckTime: Option<String>
) {
    companion object {
        val Initial = DarkWebUiState(
            protonEmailState = DarkWebEmailBreachState.Loading,
            aliasEmailState = DarkWebEmailBreachState.Loading,
            customEmailState = DarkWebCustomEmailsState.Loading,
            darkWebStatus = DarkWebStatus.Loading,
            lastCheckTime = None
        )
    }
}
