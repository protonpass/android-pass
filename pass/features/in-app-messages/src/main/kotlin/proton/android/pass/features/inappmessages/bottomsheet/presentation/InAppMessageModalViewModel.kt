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
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.asResultWithoutLoading
import proton.android.pass.common.api.getOrNull
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.inappmessages.ObserveDeliverableInAppMessages
import proton.android.pass.data.api.work.WorkerItem
import proton.android.pass.data.api.work.WorkerLauncher
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.features.inappmessages.bottomsheet.navigation.InAppMessageNavArgId
import proton.android.pass.navigation.api.CommonNavArgId
import javax.inject.Inject

@HiltViewModel
class InAppMessageModalViewModel @Inject constructor(
    private val workerLauncher: WorkerLauncher,
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
        .asResultWithoutLoading()
        .filter { it.getOrNull().orEmpty().any { message -> message.id == inAppMessageId } }
        .map {
            val firstElement = it.getOrNull()?.firstOrNull()
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

    fun onInAppMessageDismissed(userId: UserId, inAppMessageId: InAppMessageId) {
        workerLauncher.launch(WorkerItem.MarkInAppMessageAsDismissed(userId, inAppMessageId))
    }
}
