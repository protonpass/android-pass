/*
 * Copyright (c) 2024-2025 Proton AG
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

package proton.android.pass.features.item.details.passkey.bottomsheet.presentation

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.serialization.json.Json
import proton.android.pass.common.api.FlowUtils.oneShot
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.map
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.data.api.usecases.passkeys.GetPasskeyById
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.Passkey
import proton.android.pass.domain.PasskeyId
import proton.android.pass.domain.ShareId
import proton.android.pass.features.item.details.passkey.bottomsheet.navigation.DirectPasskeyNavArgId
import proton.android.pass.features.item.details.passkey.bottomsheet.navigation.PasskeyIdNavArgId
import proton.android.pass.features.item.details.passkey.bottomsheet.navigation.ViewPasskeyDetailsMode
import proton.android.pass.features.item.details.passkey.bottomsheet.navigation.ViewPasskeyDetailsModeNavArgId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavParamEncoder
import javax.inject.Inject

@Stable
sealed interface PasskeyDetailBottomSheetContent {

    @Stable
    data object Loading : PasskeyDetailBottomSheetContent

    @Stable
    data class Success(val passkey: Passkey) : PasskeyDetailBottomSheetContent
}

@Stable
sealed interface PasskeyDetailBottomSheetEvent {
    @Stable
    data object Idle : PasskeyDetailBottomSheetEvent

    @Stable
    data object Close : PasskeyDetailBottomSheetEvent
}

@Stable
data class PasskeyDetailBottomSheetState(
    val event: PasskeyDetailBottomSheetEvent,
    val content: PasskeyDetailBottomSheetContent
) {
    companion object {
        val Initial = PasskeyDetailBottomSheetState(
            event = PasskeyDetailBottomSheetEvent.Idle,
            content = PasskeyDetailBottomSheetContent.Loading
        )
    }
}

@HiltViewModel
class PasskeyDetailBottomSheetViewModel @Inject constructor(
    private val getPasskeyById: GetPasskeyById,
    private val savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val mode = getMode()

    private val eventFlow: MutableStateFlow<PasskeyDetailBottomSheetEvent> =
        MutableStateFlow(PasskeyDetailBottomSheetEvent.Idle)

    private val getPasskeyFlow: Flow<LoadingResult<Passkey>> = when (mode) {
        is BottomsheetMode.Direct -> flowOf(mode.passkey.toDomain()).asLoadingResult()
        is BottomsheetMode.References -> oneShot {
            getPasskeyById(mode.shareId, mode.itemId, mode.passkeyId)
        }.asLoadingResult().mapLatest {
            when (it) {
                is LoadingResult.Success -> when (val passkey = it.data) {
                    None -> {
                        PassLogger.w(TAG, "Passkey not found")
                        eventFlow.update { PasskeyDetailBottomSheetEvent.Close }
                        LoadingResult.Loading
                    }

                    is Some -> {
                        LoadingResult.Success(passkey.value)
                    }
                }

                is LoadingResult.Error -> {
                    PassLogger.w(TAG, "Error retrieving passkey")
                    PassLogger.w(TAG, it.exception)
                    LoadingResult.Loading
                }

                is LoadingResult.Loading -> LoadingResult.Loading
            }
        }
            .distinctUntilChanged()
    }

    val state: StateFlow<PasskeyDetailBottomSheetState> = combine(
        getPasskeyFlow,
        eventFlow
    ) { result, event ->
        val content = result.map { passkey ->
            PasskeyDetailBottomSheetContent.Success(passkey = passkey)
        }.getOrNull() ?: PasskeyDetailBottomSheetContent.Loading
        PasskeyDetailBottomSheetState(
            event = event,
            content = content
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = PasskeyDetailBottomSheetState.Initial
    )

    fun clearEvent() {
        eventFlow.update { PasskeyDetailBottomSheetEvent.Idle }
    }

    private fun getMode(): BottomsheetMode {
        val mode = savedStateHandleProvider.get().require<String>(
            name = ViewPasskeyDetailsModeNavArgId.key
        )

        return when (mode) {
            ViewPasskeyDetailsMode.Direct.name -> {
                val encoded = savedStateHandleProvider.get().require<String>(
                    name = DirectPasskeyNavArgId.key
                )
                val decoded = Json.decodeFromString<UIPasskeyContent>(
                    string = NavParamEncoder.decode(encoded)
                )
                BottomsheetMode.Direct(decoded)
            }

            ViewPasskeyDetailsMode.References.name -> {
                val shareId: ShareId = savedStateHandleProvider.get()
                    .require<String>(CommonNavArgId.ShareId.key)
                    .let(::ShareId)
                val itemId: ItemId = savedStateHandleProvider.get()
                    .require<String>(CommonNavArgId.ItemId.key)
                    .let(::ItemId)
                val passkeyId: PasskeyId = savedStateHandleProvider.get()
                    .require<String>(PasskeyIdNavArgId.key)
                    .let(::PasskeyId)

                BottomsheetMode.References(shareId, itemId, passkeyId)
            }

            else -> throw IllegalStateException("Invalid mode for ViewPasskeyDetailsMode: $mode")
        }
    }

    private sealed interface BottomsheetMode {
        data class References(
            val shareId: ShareId,
            val itemId: ItemId,
            val passkeyId: PasskeyId
        ) : BottomsheetMode

        @JvmInline
        value class Direct(
            val passkey: UIPasskeyContent
        ) : BottomsheetMode
    }

    companion object {
        private const val TAG = "PasskeyDetailBottomSheetViewModel"
    }

}
