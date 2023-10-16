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

package proton.android.pass.featuresharing.impl.manage.bottomsheet.inviteoptions

import androidx.compose.runtime.Stable

sealed interface InviteOptionsUiEvent {
    object ResendInvite : InviteOptionsUiEvent
    object CancelInvite : InviteOptionsUiEvent
}

enum class LoadingOption {
    ResendInvite,
    CancelInvite
}

@Stable
sealed interface InviteOptionsEvent {
    @Stable
    object Unknown : InviteOptionsEvent

    @Stable
    @JvmInline
    value class Close(val refresh: Boolean) : InviteOptionsEvent
}

@Stable
data class InviteOptionsUiState(
    val loadingOption: LoadingOption?,
    val showResendInvite: Boolean,
    val event: InviteOptionsEvent
) {
    companion object {
        fun Initial(showResendInvite: Boolean) = InviteOptionsUiState(
            loadingOption = null,
            showResendInvite = showResendInvite,
            event = InviteOptionsEvent.Unknown
        )
    }
}
