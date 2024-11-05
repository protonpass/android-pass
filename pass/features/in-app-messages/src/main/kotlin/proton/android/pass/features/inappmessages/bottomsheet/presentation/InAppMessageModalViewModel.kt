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

package proton.android.pass.features.inappmessages.bottomsheet.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.runCatching
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.inappmessages.ChangeInAppMessageStatus
import proton.android.pass.data.api.usecases.inappmessages.ObserveDeliverableInAppMessages
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.domain.inappmessages.InAppMessageStatus
import proton.android.pass.features.inappmessages.bottomsheet.navigation.InAppMessageNavArgId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import javax.inject.Inject

@HiltViewModel
class InAppMessageModalViewModel @Inject constructor(
    private val changeInAppMessageStatus: ChangeInAppMessageStatus,
    observeDeliverableInAppMessages: ObserveDeliverableInAppMessages,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val userId: UserId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.UserId.key)
        .let(::UserId)

    private val inAppMessageId: InAppMessageId = savedStateHandleProvider.get()
        .require<String>(InAppMessageNavArgId.key)
        .let(::InAppMessageId)

    val state = observeDeliverableInAppMessages(userId)
        .filter { it.any { message -> message.id == inAppMessageId } }
        .map {
            val firstElement = it.firstOrNull()
            if (firstElement != null) {
                InAppMessageModalState.Success(firstElement)
            } else {
                InAppMessageModalState.Error
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InAppMessageModalState.Loading
        )

    fun onInAppMessageRead(userId: UserId, inAppMessageId: InAppMessageId) {
        viewModelScope.launch {
            runCatching {
                changeInAppMessageStatus(userId, inAppMessageId, InAppMessageStatus.Read)
            }
                .onSuccess {
                    PassLogger.i(TAG, "In-app message read")
                }
                .onError {
                    PassLogger.w(TAG, "Error reading in-app message")
                    PassLogger.w(TAG, it)
                }
        }
    }

    companion object {
        private const val TAG = "InAppMessageModalViewModel"
    }
}
