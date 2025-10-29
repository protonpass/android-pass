/*
 * Copyright (c) 2025 Proton AG
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

package proton.android.pass.features.password.history

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.passwordHistoryEntry.DeleteOnePasswordHistoryEntryForUser
import proton.android.pass.data.api.usecases.passwordHistoryEntry.DeletePasswordHistoryEntryForUser
import proton.android.pass.data.api.usecases.passwordHistoryEntry.ObservePasswordHistoryEntryForUser
import proton.android.pass.domain.PasswordHistoryEntryId
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.password.history.model.PasswordHistoryItemUiState
import proton.android.pass.features.password.history.model.PasswordHistoryUiState
import proton.android.pass.features.password.history.model.toUiModel
import javax.inject.Inject

@HiltViewModel
class PasswordHistoryEntryViewModel @Inject constructor(
    observePasswordHistoryForUser: ObservePasswordHistoryEntryForUser,
    private val application: Application,
    private val clock: Clock,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val deletePasswordHistoryEntryForUser: DeletePasswordHistoryEntryForUser,
    private val deleteOnePasswordHistoryEntryForUser: DeleteOnePasswordHistoryEntryForUser
) : ViewModel() {

    private val _passwords: MutableStateFlow<PersistentList<PasswordHistoryItemUiState>> =
        MutableStateFlow(persistentListOf())
    private val _isLoading: MutableStateFlow<Boolean> = MutableStateFlow(false)

    internal val state: StateFlow<PasswordHistoryUiState> = combine(
        _isLoading,
        _passwords,
        ::PasswordHistoryUiState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = PasswordHistoryUiState(
            isLoading = true
        )
    )

    init {
        viewModelScope.launch {
            observePasswordHistoryForUser().collect { newPasswordList ->
                _passwords.update { currentUiPasswords ->
                    newPasswordList.map { onePassword ->
                        val index =
                            currentUiPasswords.indexOfFirst {
                                it.passwordHistoryEntryId == onePassword.passwordHistoryEntryId
                            }
                        onePassword.toUiModel(
                            clock = clock,
                            context = application.applicationContext,
                            defaultUIHiddenState = if (index >= 0 && index < currentUiPasswords.size) {
                                currentUiPasswords[index].value
                            } else {
                                // by default
                                UIHiddenState.Concealed(onePassword.encrypted)
                            }
                        )
                    }.toPersistentList()
                }
                _isLoading.update { false }
            }
        }
    }

    fun onHideItem(id: PasswordHistoryEntryId) {
        val index = _passwords.value.indexOfFirst { it.passwordHistoryEntryId == id }
        if (index >= 0) {
            _passwords.update { currentList ->
                val currentItem = currentList[index]
                currentList.set(
                    index,
                    currentItem.copy(
                        value = UIHiddenState.Concealed(
                            encrypted = currentItem.value.encrypted
                        )
                    )
                )
            }
        }
    }

    fun onRevealItem(id: PasswordHistoryEntryId) {
        val index = _passwords.value.indexOfFirst { it.passwordHistoryEntryId == id }
        if (index >= 0) {
            _passwords.update { currentList ->
                val currentItem = currentList[index]
                currentList.set(
                    index,
                    currentItem.copy(
                        value = UIHiddenState.Revealed(
                            encrypted = currentItem.value.encrypted,
                            clearText = encryptionContextProvider.withEncryptionContext {
                                decrypt(currentItem.value.encrypted)
                            }
                        )
                    )
                )
            }
        }
    }

    fun onClearHistory() {
        viewModelScope.launch {
            deletePasswordHistoryEntryForUser()
        }
    }

    fun onClearItem(passwordHistoryEntryId: PasswordHistoryEntryId) {
        viewModelScope.launch {
            val index = _passwords.value.indexOfFirst {
                it.passwordHistoryEntryId == passwordHistoryEntryId
            }
            if (index >= 0) {
                deleteOnePasswordHistoryEntryForUser(_passwords.value[index].passwordHistoryEntryId)
            }
        }
    }
}
