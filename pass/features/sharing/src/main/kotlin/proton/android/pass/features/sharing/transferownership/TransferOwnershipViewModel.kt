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

package proton.android.pass.features.sharing.transferownership

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.usecases.TransferVaultOwnership
import proton.android.pass.domain.ShareId
import proton.android.pass.features.sharing.SharingSnackbarMessage
import proton.android.pass.features.sharing.manage.bottomsheet.MemberEmailArg
import proton.android.pass.features.sharing.manage.bottomsheet.MemberShareIdArg
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavParamEncoder
import proton.android.pass.notifications.api.SnackbarDispatcher
import javax.inject.Inject

@HiltViewModel
class TransferOwnershipViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val transferVaultOwnership: TransferVaultOwnership,
    savedState: SavedStateHandleProvider
) : ViewModel() {

    private val shareId = ShareId(savedState.get().require(CommonNavArgId.ShareId.key))
    private val memberShareId = ShareId(savedState.get().require(MemberShareIdArg.key))
    private val memberEmail = savedState.get()
        .require<String>(MemberEmailArg.key)
        .let(NavParamEncoder::decode)

    private val loadingFlow: MutableStateFlow<IsLoadingState> = MutableStateFlow(IsLoadingState.NotLoading)
    private val eventFlow: MutableStateFlow<TransferOwnershipEvent> = MutableStateFlow(TransferOwnershipEvent.Unknown)

    val state: StateFlow<TransferOwnershipState> = combine(
        flowOf(memberEmail),
        loadingFlow,
        eventFlow,
        ::TransferOwnershipState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = TransferOwnershipState.initial(memberEmail)
    )

    fun transferOwnership() = viewModelScope.launch {
        loadingFlow.update { IsLoadingState.Loading }
        runCatching {
            transferVaultOwnership(shareId, memberShareId)
        }.onSuccess {
            PassLogger.i(TAG, "Ownership transferred")
            eventFlow.update { TransferOwnershipEvent.OwnershipTransferred }
            snackbarDispatcher(SharingSnackbarMessage.TransferOwnershipSuccess)
        }.onFailure {
            PassLogger.w(TAG, "Error transferring ownership")
            PassLogger.w(TAG, it)
            snackbarDispatcher(SharingSnackbarMessage.TransferOwnershipError)
        }
        loadingFlow.update { IsLoadingState.NotLoading }
    }

    fun clearEvent() {
        eventFlow.update { TransferOwnershipEvent.Unknown }
    }

    companion object {
        private const val TAG = "TransferOwnershipViewModel"
    }
}
