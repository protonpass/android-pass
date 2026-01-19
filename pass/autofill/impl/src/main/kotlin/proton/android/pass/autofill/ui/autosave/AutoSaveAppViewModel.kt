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

package proton.android.pass.autofill.ui.autosave

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.proton.core.account.domain.entity.AccountState
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.autofill.AutosaveDisplay
import proton.android.pass.autofill.AutosaveDone
import proton.android.pass.autofill.entities.SaveInformation
import proton.android.pass.autofill.entities.usernamePassword
import proton.android.pass.autofill.extensions.deserializeParcelable
import proton.android.pass.autofill.ui.autosave.AutoSaveActivity.Companion.ARG_LINKED_APP
import proton.android.pass.autofill.ui.autosave.AutoSaveActivity.Companion.ARG_SAVE_INFORMATION
import proton.android.pass.autofill.ui.autosave.AutoSaveActivity.Companion.ARG_WEBSITE
import proton.android.pass.biometry.NeedsBiometricAuth
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.url.UrlSanitizer
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareSelection
import proton.android.pass.features.itemcreate.login.InitialUpdateLoginUiState
import proton.android.pass.log.api.PassLogger
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@HiltViewModel
class AutoSaveAppViewModel @Inject constructor(
    private val needsBiometricAuth: NeedsBiometricAuth,
    private val telemetryManager: TelemetryManager,
    savedStateHandleProvider: SavedStateHandleProvider,
    observeItems: ObserveItems,
    accountManager: AccountManager,
    private val encryptionContextProvider: EncryptionContextProvider
) : ViewModel() {

    private val saveInformationArgs: SaveInformation? = savedStateHandleProvider.get()
        .get<ByteArray>(ARG_SAVE_INFORMATION)
        ?.deserializeParcelable()

    val linkedAppInfoArgs: LinkedAppInfo? = savedStateHandleProvider.get()
        .get<ByteArray>(ARG_LINKED_APP)
        ?.deserializeParcelable()

    val websiteArgs = savedStateHandleProvider.get()
        .get<String>(ARG_WEBSITE)
        ?.let { url ->
            UrlSanitizer.sanitize(url = url)
                .fold(
                    onSuccess = {
                        it
                    },
                    onFailure = {
                        PassLogger.w(TAG, "Failed to sanitize $url. Error : $it")
                        null
                    }
                )
        }

    private val usernamePasswordArgs = saveInformationArgs?.usernamePassword()

    private val accounts = accountManager.getAccounts().map { accountList ->
        accountList.filter { it.state == AccountState.Ready }
    }

    private val allLoginItemsFlow = accounts.flatMapLatest { accountList ->
        val loginItemsList = accountList.map { account ->
            observeItems(
                selection = ShareSelection.AllShares,
                itemState = ItemState.Active,
                filter = ItemTypeFilter.Logins,
                includeHidden = false,
                userId = account.userId
            )
        }
        combine(loginItemsList) { lists -> lists.flatMap { it } }
    }

    init {
        telemetryManager.sendEvent(AutosaveDisplay)
    }

    val state: StateFlow<AutoSaveAppViewState> = combine(
        needsBiometricAuth(),
        allLoginItemsFlow
    ) { needsAuth, loginItems ->
        AutoSaveAppViewState.Ready(
            needsAuth = needsAuth,
            initialUpdateLoginUiState = checkNeedsUpdate(loginItems)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AutoSaveAppViewState.Loading
    )

    fun onItemAutoSaved() {
        telemetryManager.sendEvent(AutosaveDone)
    }

    private fun checkNeedsUpdate(loginItems: List<Item>): InitialUpdateLoginUiState? {
        val user = usernamePasswordArgs ?: return null

        val itemMatch = loginItems.firstOrNull { loginItem ->

            val item = loginItem.itemType as? ItemType.Login ?: return@firstOrNull false

            val usernameMatch =
                item.itemUsername == user.username || item.itemEmail == user.username

            if (!usernameMatch) return@firstOrNull false

            val match = when {
                // if comes from a website
                websiteArgs != null -> {
                    if (websiteArgs.isBlank()) return@firstOrNull false

                    item.websites.any { url ->
                        UrlSanitizer.sanitize(url = url)
                            .fold(
                                onSuccess = {
                                    it == websiteArgs
                                },
                                onFailure = {
                                    false
                                }
                            )
                    }
                }

                // if comes from a app
                linkedAppInfoArgs != null -> {
                    item.packageInfoSet.any {
                        it.packageName.value == linkedAppInfoArgs.packageName
                    }
                }

                else -> false
            }

            match
        }

        return itemMatch?.let {
            InitialUpdateLoginUiState(
                sharedId = it.shareId,
                itemId = it.id,
                userId = it.userId,
                newPassword = encryptionContextProvider.withEncryptionContext {
                    encrypt(usernamePasswordArgs.password.orEmpty())
                }
            )
        }
    }
}

sealed class AutoSaveAppViewState {
    object Loading : AutoSaveAppViewState()

    data class Ready(
        val needsAuth: Boolean,
        val initialUpdateLoginUiState: InitialUpdateLoginUiState? = null
    ) : AutoSaveAppViewState()
}


private const val TAG = "AutoSaveAppViewModel"
