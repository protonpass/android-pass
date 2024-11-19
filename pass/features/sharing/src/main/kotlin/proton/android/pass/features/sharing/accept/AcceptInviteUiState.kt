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

package proton.android.pass.features.sharing.accept

import androidx.compose.runtime.Stable
import proton.android.pass.domain.PendingInvite

@Stable
data class AcceptInviteButtonsState(
    val confirmLoading: Boolean,
    val rejectLoading: Boolean,
    val hideReject: Boolean,
    val enabled: Boolean
) {
    companion object {
        val Initial = AcceptInviteButtonsState(
            confirmLoading = false,
            hideReject = false,
            rejectLoading = false,
            enabled = true
        )
    }
}

@Stable
sealed interface AcceptInviteProgressState {
    @Stable
    data object Hide : AcceptInviteProgressState

    @Stable
    data class Show(
        val downloaded: Int,
        val total: Int
    ) : AcceptInviteProgressState
}

@Stable
sealed interface AcceptInviteUiContent {
    @Stable
    data object Loading : AcceptInviteUiContent

    @Stable
    data class Content(
        val invite: PendingInvite?,
        val buttonsState: AcceptInviteButtonsState,
        val progressState: AcceptInviteProgressState
    ) : AcceptInviteUiContent
}
