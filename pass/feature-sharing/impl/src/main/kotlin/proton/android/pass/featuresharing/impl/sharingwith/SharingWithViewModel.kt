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

package proton.android.pass.featuresharing.impl.sharingwith

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.CommonRegex.EMAIL_VALIDATION_REGEX
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.getOrNull
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.data.api.usecases.GetVaultById
import proton.android.pass.navigation.api.CommonNavArgId
import proton.pass.domain.ShareId
import proton.pass.domain.Vault
import javax.inject.Inject

@HiltViewModel
class SharingWithViewModel @Inject constructor(
    getVaultById: GetVaultById,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val shareId: ShareId =
        ShareId(savedStateHandleProvider.get().require(CommonNavArgId.ShareId.key))

    private val isEmailNotValidState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val emailState: MutableStateFlow<String> = MutableStateFlow("")

    val state: StateFlow<SharingWithUIState> = combine(
        emailState,
        isEmailNotValidState,
        getVaultById(shareId = shareId).asLoadingResult()
    ) { email, isEmailNotValid, vault: LoadingResult<Vault> ->
        SharingWithUIState(
            email = email,
            vaultName = vault.getOrNull()?.name,
            isEmailNotValid = isEmailNotValid,
            isVaultNotFound = vault is LoadingResult.Error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SharingWithUIState()
    )

    fun onEmailChange(value: String) {
        val sanitised = value.replace(" ", "").replace("\n", "")
        emailState.update { sanitised }
        isEmailNotValidState.update { false }
    }

    fun onEmailSubmit() {
        val email = emailState.value
        if (email.isBlank() || !EMAIL_VALIDATION_REGEX.matches(email)) {
            isEmailNotValidState.update { true }
        }
    }
}

