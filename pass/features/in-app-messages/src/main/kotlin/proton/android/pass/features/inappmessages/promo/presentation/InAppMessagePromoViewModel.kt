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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asResultWithoutLoading
import proton.android.pass.common.api.getOrNull
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.inappmessages.ChangeInAppMessageStatus
import proton.android.pass.data.api.usecases.inappmessages.ObserveInAppMessage
import proton.android.pass.domain.inappmessages.InAppMessage
import proton.android.pass.domain.inappmessages.InAppMessageId
import proton.android.pass.domain.inappmessages.InAppMessageKey
import proton.android.pass.domain.inappmessages.InAppMessageStatus
import proton.android.pass.features.inappmessages.InAppMessagesChange
import proton.android.pass.features.inappmessages.InAppMessagesClick
import proton.android.pass.features.inappmessages.InAppMessagesDisplay
import proton.android.pass.features.inappmessages.navigation.InAppMessageNavArgId
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
class InAppMessagePromoViewModel @Inject constructor(
    private val telemetryManager: TelemetryManager,
    observeInAppMessage: ObserveInAppMessage,
    userPreferencesRepository: UserPreferencesRepository,
    savedStateHandleProvider: SavedStateHandleProvider,
    private val changeInAppMessageStatus: ChangeInAppMessageStatus
) : ViewModel() {

    private val userId: UserId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.UserId.key)
        .let(::UserId)

    private val inAppMessageId: InAppMessageId = savedStateHandleProvider.get()
        .require<String>(InAppMessageNavArgId.key)
        .let(::InAppMessageId)

    private val eventFlow = MutableStateFlow<InAppMessagePromoEvent>(InAppMessagePromoEvent.Idle)

    val state: StateFlow<InAppMessagePromoState> = combine(
        observeInAppMessage(userId, inAppMessageId).asResultWithoutLoading().take(1),
        userPreferencesRepository.getThemePreference(),
        eventFlow
    ) { result: LoadingResult<InAppMessage>, themePreference: ThemePreference, event: InAppMessagePromoEvent ->
        when (val message = result.getOrNull()) {
            is InAppMessage.Promo -> InAppMessagePromoState.Success(message, themePreference, event)
            else -> InAppMessagePromoState.Error
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InAppMessagePromoState.Loading
        )

    init {
        viewModelScope.launch {
            state
                .filterIsInstance<InAppMessagePromoState.Success>()
                .distinctUntilChanged { old, new -> old.inAppMessage.key == new.inAppMessage.key }
                .collect { successState ->
                    onInAppMessageDisplayed(successState.inAppMessage.key)
                }
        }
    }

    fun onInAppMessageDisplayed(key: InAppMessageKey) {
        telemetryManager.sendEvent(InAppMessagesDisplay(key))
    }

    fun onCTAClicked(key: InAppMessageKey, website: String) {
        telemetryManager.sendEvent(InAppMessagesClick(key))
        telemetryManager.sendEvent(InAppMessagesChange(key, InAppMessageStatus.Dismissed))

        viewModelScope.launch {
            runCatching {
                changeInAppMessageStatus(userId, inAppMessageId, InAppMessageStatus.Dismissed)
            }
            eventFlow.update { InAppMessagePromoEvent.OnCTAClicked(website) }
        }
    }

    fun onInternalCTAClicked(key: InAppMessageKey, deepLink: String) {
        telemetryManager.sendEvent(InAppMessagesClick(key))
        telemetryManager.sendEvent(InAppMessagesChange(key, InAppMessageStatus.Dismissed))

        viewModelScope.launch {
            runCatching {
                changeInAppMessageStatus(userId, inAppMessageId, InAppMessageStatus.Dismissed)
            }
            eventFlow.update { InAppMessagePromoEvent.OnInternalCTAClicked(deepLink) }
        }
    }

    fun onClose(key: InAppMessageKey) {
        telemetryManager.sendEvent(InAppMessagesChange(key, InAppMessageStatus.Read))
        viewModelScope.launch {
            runCatching {
                changeInAppMessageStatus(userId, inAppMessageId, InAppMessageStatus.Read)
            }
            emitCloseEvent()
        }
    }

    fun onDontShowAgain(key: InAppMessageKey) {
        telemetryManager.sendEvent(InAppMessagesChange(key, InAppMessageStatus.Dismissed))
        viewModelScope.launch {
            runCatching {
                changeInAppMessageStatus(userId, inAppMessageId, InAppMessageStatus.Dismissed)
            }
            emitCloseEvent()
        }
    }

    fun onConsumeEvent(event: InAppMessagePromoEvent) {
        eventFlow.compareAndSet(event, InAppMessagePromoEvent.Idle)
    }

    private fun emitCloseEvent() {
        eventFlow.update { InAppMessagePromoEvent.OnClose }
    }

}
