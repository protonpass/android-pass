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

package proton.android.pass.features.inappmessages.promo.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.asResultWithoutLoading
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.inappmessages.ObserveInAppMessage
import proton.android.pass.data.api.work.WorkerItem
import proton.android.pass.data.api.work.WorkerLauncher
import proton.android.pass.domain.inappmessages.InAppMessage
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.domain.inappmessages.InAppMessageKey
import proton.android.pass.domain.inappmessages.InAppMessageStatus
import proton.android.pass.features.inappmessages.InAppMessagesChange
import proton.android.pass.features.inappmessages.InAppMessagesClick
import proton.android.pass.features.inappmessages.InAppMessagesDisplay
import proton.android.pass.features.inappmessages.navigation.InAppMessageNavArgId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
class InAppMessagePromoViewModel @Inject constructor(
    private val workerLauncher: WorkerLauncher,
    private val telemetryManager: TelemetryManager,
    observeInAppMessage: ObserveInAppMessage,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val userId: UserId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.UserId.key)
        .let(::UserId)

    private val inAppMessageId: InAppMessageId = savedStateHandleProvider.get()
        .require<String>(InAppMessageNavArgId.key)
        .let(::InAppMessageId)

    private var inAppMessageStatus: Option<InAppMessageStatus> = None

    val state = observeInAppMessage(userId, inAppMessageId)
        .asResultWithoutLoading()
        .map { result: LoadingResult<InAppMessage> ->
            val message = result.getOrNull() ?: return@map InAppMessagePromoState.Error
            InAppMessagePromoState.Success(message)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InAppMessagePromoState.Loading
        )

    fun onInAppMessageDisplayed(key: InAppMessageKey) {
        telemetryManager.sendEvent(InAppMessagesDisplay(key))
    }

    fun onCTAClicked(key: InAppMessageKey) {
        telemetryManager.sendEvent(InAppMessagesClick(key))
    }

    fun onClose() {
        inAppMessageStatus = InAppMessageStatus.Read.some()
    }

    fun onDontShowAgain() {
        inAppMessageStatus = InAppMessageStatus.Dismissed.some()
    }

    override fun onCleared() {
        inAppMessageStatus.value()?.let {
            workerLauncher.launch(
                WorkerItem.ChangeInAppMessageStatus(
                    userId = userId,
                    inAppMessageId = inAppMessageId,
                    inAppMessageStatus = it
                )
            )
            val key = (state.value as? InAppMessagePromoState.Success)?.inAppMessage?.key
            if (key != null) {
                telemetryManager.sendEvent(InAppMessagesChange(key, InAppMessageStatus.Dismissed))
            }
        }

        super.onCleared()
    }

}
