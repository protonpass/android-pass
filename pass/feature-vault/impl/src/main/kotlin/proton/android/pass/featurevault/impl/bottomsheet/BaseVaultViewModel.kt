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

package proton.android.pass.featurevault.impl.bottomsheet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon

abstract class BaseVaultViewModel : ViewModel() {

    protected val formFlow: MutableStateFlow<CreateVaultFormValues> =
        MutableStateFlow(CreateVaultFormValues())
    protected val hasEditedTitleFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    protected val isLoadingFlow: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    protected val eventFlow: MutableStateFlow<IsVaultCreatedEvent> =
        MutableStateFlow(IsVaultCreatedEvent.Unknown)

    val state: StateFlow<BaseVaultUiState> = combine(
        formFlow,
        hasEditedTitleFlow,
        isLoadingFlow,
        eventFlow
    ) { form, hasEdited, isLoading, vaultCreated ->
        val isTitleRequiredError = hasEdited && form.name.isBlank()
        BaseVaultUiState(
            name = form.name,
            color = form.color,
            icon = form.icon,
            isLoading = isLoading,
            isTitleRequiredError = isTitleRequiredError,
            isVaultCreatedEvent = vaultCreated,
            isCreateButtonEnabled = IsButtonEnabled.from(form.name.isNotBlank())
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = BaseVaultUiState.Initial
    )

    fun onNameChange(value: String) {
        formFlow.update { it.copy(name = value) }
        hasEditedTitleFlow.update { true }
    }

    fun onIconChange(value: ShareIcon) {
        formFlow.update { it.copy(icon = value) }
    }

    fun onColorChange(value: ShareColor) {
        formFlow.update { it.copy(color = value) }
    }

    fun clearEvent() {
        eventFlow.update { IsVaultCreatedEvent.Unknown }
    }

    data class CreateVaultFormValues(
        val name: String = "",
        val icon: ShareIcon = ShareIcon.Icon1,
        val color: ShareColor = ShareColor.Color1
    )
}
