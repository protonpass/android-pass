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

package proton.android.pass.featuresharing.impl.confirmed

import androidx.compose.runtime.Stable
import proton.android.pass.domain.PendingInvite
import proton.android.pass.domain.ShareId

sealed interface InviteConfirmedEvent {
    object Unknown : InviteConfirmedEvent
    object Close : InviteConfirmedEvent

    @JvmInline
    value class Confirmed(val shareId: ShareId) : InviteConfirmedEvent
}

@Stable
data class InviteConfirmedUiState(
    val event: InviteConfirmedEvent,
    val content: InviteConfirmedUiContent
) {
    companion object {
        val Initial = InviteConfirmedUiState(
            event = InviteConfirmedEvent.Unknown,
            content = InviteConfirmedUiContent.Loading
        )
    }
}


@Stable
data class InviteConfirmedButtonsState(
    val confirmLoading: Boolean,
    val rejectLoading: Boolean,
    val hideReject: Boolean,
    val enabled: Boolean
) {
    companion object {
        val Initial = InviteConfirmedButtonsState(
            confirmLoading = false,
            hideReject = false,
            rejectLoading = false,
            enabled = true
        )
    }
}

@Stable
sealed interface InviteConfirmedProgressState {
    @Stable
    object Hide : InviteConfirmedProgressState

    @Stable
    data class Show(
        val downloaded: Int,
        val total: Int
    ) : InviteConfirmedProgressState
}

@Stable
sealed interface InviteConfirmedUiContent {
    @Stable
    object Loading : InviteConfirmedUiContent

    @Stable
    data class Content(
        val invite: PendingInvite?,
        val buttonsState: InviteConfirmedButtonsState,
        val progressState: InviteConfirmedProgressState
    ) : InviteConfirmedUiContent
}
