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
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.domain.breach.BreachCustomEmailId

@Stable
sealed interface CustomEmailUiStatus {
    @JvmInline
    value class NotVerified(val usedInLoginsCount: Int) : CustomEmailUiStatus

    data class Verified(val breachesDetected: Int) : CustomEmailUiStatus {
        internal val hasBreaches: Boolean = breachesDetected > 0
    }
}

@Stable
data class CustomEmailUiState(
    val id: BreachCustomEmailId,
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
sealed interface DarkWebEmailsState {
    data object Loading : DarkWebEmailsState

    @JvmInline
    value class Error(val reason: DarkWebEmailsError) : DarkWebEmailsState

    @JvmInline
    value class Success(val emails: ImmutableList<CustomEmailUiState>) : DarkWebEmailsState
}

@Stable
data class DarkWebUiState(
    val customEmails: DarkWebEmailsState,
    val darkWebStatus: DarkWebStatus,
    val lastCheckTime: Option<String>
) {
    companion object {
        val Initial = DarkWebUiState(
            customEmails = DarkWebEmailsState.Loading,
            darkWebStatus = DarkWebStatus.Loading,
            lastCheckTime = None
        )
    }
}
