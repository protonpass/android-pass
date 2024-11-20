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

package proton.android.pass.featureprofile.impl.manageaccountconfirmation.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavParamEncoder
import javax.inject.Inject

@HiltViewModel
class ManageAccountConfirmationViewModel @Inject constructor(
    private val accountManager: AccountManager,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val userId: UserId = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.UserId.key)
        .let(::UserId)

    private val email: String = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.Email.key)
        .let(NavParamEncoder::decode)

    private val eventFlow: MutableStateFlow<ManageAccountConfirmationEvent> =
        MutableStateFlow(ManageAccountConfirmationEvent.Idle)

    val state = eventFlow.map {
        ManageAccountConfirmationState(email, it)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ManageAccountConfirmationState.Initial
    )

    fun switchAccount() {
        viewModelScope.launch {
            accountManager.setAsPrimary(userId)
            eventFlow.update { ManageAccountConfirmationEvent.ToAccount }
        }
    }

    fun consumeEvent(event: ManageAccountConfirmationEvent) {
        viewModelScope.launch {
            eventFlow.compareAndSet(event, ManageAccountConfirmationEvent.Idle)
        }
    }
}
