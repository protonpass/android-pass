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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
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
import proton.android.pass.data.api.autosave.AutosaveLoginMatcher
import proton.android.pass.data.api.url.UrlSanitizer
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveAllShares
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.domain.Item
import me.proton.core.domain.entity.UserId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId
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
    observeAllShares: ObserveAllShares,
    accountManager: AccountManager,
    private val encryptionContextProvider: EncryptionContextProvider
) : ViewModel() {

    private val saveInformationArgs: SaveInformation? = savedStateHandleProvider.get()
        .get<ByteArray>(ARG_SAVE_INFORMATION)
        ?.deserializeParcelable()

    private val linkedAppInfoArgs: LinkedAppInfo? = savedStateHandleProvider.get()
        .get<ByteArray>(ARG_LINKED_APP)
        ?.deserializeParcelable()

    private val websiteArgs: String? = savedStateHandleProvider.get()
        .get<String>(ARG_WEBSITE)
        ?.let { url ->
            UrlSanitizer.sanitize(url = url)
                .fold(
                    onSuccess = { it },
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

    private val updatableShareIdsFlow = accounts.flatMapLatest { accountList ->
        val sharesList = accountList.map { account ->
            observeAllShares(userId = account.userId, includeHidden = false)
        }
        combine(sharesList) { lists ->
            lists.flatMap { it }
                .filter { it.canBeUpdated }
                .map { it.id }
                .toSet()
        }
    }

    private val selectedItemForUpdateFlow: MutableStateFlow<InitialUpdateLoginUiState?> =
        MutableStateFlow(null)

    init {
        telemetryManager.sendEvent(AutosaveDisplay)
    }

    private val baseAutosaveModeFlow = combine(
        allLoginItemsFlow,
        updatableShareIdsFlow
    ) { loginItems, updatableShareIds ->
        determineAutosaveMode(loginItems, updatableShareIds)
    }

    val state: StateFlow<AutoSaveAppViewState> = combine(
        needsBiometricAuth(),
        baseAutosaveModeFlow,
        selectedItemForUpdateFlow
    ) { needsAuth, baseMode, selectedItemForUpdate ->
        val autosaveMode =
            if (selectedItemForUpdate != null && baseMode is AutosaveMode.CreateOrUpdate) {
                baseMode.copy(selectedUpdateState = selectedItemForUpdate)
            } else {
                baseMode
            }
        AutoSaveAppViewState.Ready(
            needsAuth = needsAuth,
            autosaveMode = autosaveMode
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AutoSaveAppViewState.Loading
    )

    fun onItemAutoSaved() {
        telemetryManager.sendEvent(AutosaveDone)
    }

    fun onItemSelectedForUpdate(
        shareId: ShareId,
        itemId: ItemId,
        userId: UserId
    ) {
        val encryptedNewPassword = encryptionContextProvider.withEncryptionContext {
            encrypt(usernamePasswordArgs?.password.orEmpty())
        }
        selectedItemForUpdateFlow.value = InitialUpdateLoginUiState(
            sharedId = shareId,
            itemId = itemId,
            userId = userId,
            newPassword = encryptedNewPassword
        )
    }

    fun onClearSelectedItemForUpdate() {
        selectedItemForUpdateFlow.value = null
    }

    private fun determineAutosaveMode(loginItems: List<Item>, updatableShareIds: Set<ShareId>): AutosaveMode {
        val username = usernamePasswordArgs?.username
        if (username.isNullOrBlank()) return AutosaveMode.Create

        var hasPartialMatch = false

        val matcher = AutosaveLoginMatcher(username, websiteArgs, linkedAppInfoArgs?.packageName)
        val exactMatches = loginItems
            .filter { it.shareId in updatableShareIds }
            .filter { loginItem ->
                val login = loginItem.itemType as? ItemType.Login ?: return@filter false
                if (!matcher.matchesUsername(login)) return@filter false

                hasPartialMatch = true

                matcher.matchesSource(login) ?: false
            }


        return when {
            // if user enter NEW login / password
            // --> go to create screen directly
            exactMatches.isEmpty() && !hasPartialMatch -> AutosaveMode.Create

            // otherwise we display the list of linked items which match
            // OR a empty list if nothing is found with a message "you can only create a item"
            else -> AutosaveMode.CreateOrUpdate(
                username = username,
                website = websiteArgs,
                packageName = linkedAppInfoArgs?.packageName,
                matchCount = exactMatches.size
            )
        }
    }

    companion object {
        private const val TAG = "AutoSaveAppViewModel"
    }
}

sealed class AutosaveMode {
    data object Create : AutosaveMode()
    data class CreateOrUpdate(
        val username: String,
        val website: String? = null,
        val packageName: String? = null,
        val selectedUpdateState: InitialUpdateLoginUiState? = null,
        val matchCount: Int = 0
    ) : AutosaveMode()

    data class Update(val initialUpdateLoginUiState: InitialUpdateLoginUiState) : AutosaveMode()
}

sealed class AutoSaveAppViewState {
    object Loading : AutoSaveAppViewState()

    data class Ready(
        val needsAuth: Boolean,
        val autosaveMode: AutosaveMode = AutosaveMode.Create
    ) : AutoSaveAppViewState()
}


