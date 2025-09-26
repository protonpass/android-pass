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

package proton.android.pass.features.security.center.report.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.user.domain.entity.AddressId
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.runCatching
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.ItemTypeFilter
import proton.android.pass.data.api.usecases.ObserveItems
import proton.android.pass.data.api.usecases.breach.MarkEmailBreachAsResolved
import proton.android.pass.data.api.usecases.breach.ObserveBreachEmailReport
import proton.android.pass.data.api.usecases.breach.ObserveBreachesForEmail
import proton.android.pass.data.api.usecases.vaults.ObserveVaultsGroupedByShareId
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemState
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareFlag
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.ShareSelection
import proton.android.pass.domain.breach.BreachEmailId
import proton.android.pass.domain.breach.BreachEmailReport
import proton.android.pass.domain.breach.BreachId
import proton.android.pass.domain.breach.CustomEmailId
import proton.android.pass.features.security.center.report.presentation.SecurityCenterReportSnackbarMessage.BreachResolvedError
import proton.android.pass.features.security.center.report.presentation.SecurityCenterReportSnackbarMessage.BreachResolvedSuccessfully
import proton.android.pass.features.security.center.shared.navigation.CustomEmailIdArgId
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.navigation.api.NavParamEncoder
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import javax.inject.Inject

@HiltViewModel
class SecurityCenterReportViewModel @Inject constructor(
    observeBreachEmailReport: ObserveBreachEmailReport,
    observeBreachesForEmail: ObserveBreachesForEmail,
    observeItems: ObserveItems,
    observeVaultsGroupedByShareId: ObserveVaultsGroupedByShareId,
    private val markEmailBreachAsResolved: MarkEmailBreachAsResolved,
    private val snackbarDispatcher: SnackbarDispatcher,
    userPreferencesRepository: UserPreferencesRepository,
    encryptionContextProvider: EncryptionContextProvider,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val customEmailId: BreachEmailId.Custom? = savedStateHandleProvider.get()
        .get<String>(CustomEmailIdArgId.key)
        ?.let { BreachEmailId.Custom(BreachId(""), CustomEmailId(it)) }

    private val aliasEmailId: BreachEmailId.Alias? = run {
        val shareId = savedStateHandleProvider.get()
            .get<String>(CommonNavArgId.ShareId.key)
            ?.let(::ShareId)
        val itemId = savedStateHandleProvider.get()
            .get<String>(CommonNavArgId.ItemId.key)
            ?.let(::ItemId)
        if (shareId != null && itemId != null) {
            BreachEmailId.Alias(BreachId(""), shareId, itemId)
        } else {
            null
        }
    }

    private val protonEmailId: BreachEmailId.Proton? = savedStateHandleProvider.get()
        .get<String>(CommonNavArgId.AddressId.key)
        ?.let { BreachEmailId.Proton(BreachId(""), AddressId(it)) }

    private val breachEmailId: BreachEmailId = when {
        protonEmailId != null -> protonEmailId
        aliasEmailId != null -> aliasEmailId
        customEmailId != null -> customEmailId
        else -> throw IllegalStateException("Invalid state")
    }

    private val email: String = savedStateHandleProvider.get()
        .require<String>(CommonNavArgId.Email.key)
        .let(NavParamEncoder::decode)

    private val eventFlow: MutableStateFlow<SecurityCenterReportEvent> =
        MutableStateFlow(SecurityCenterReportEvent.Idle)

    private val observeBreachForEmailFlow = observeBreachesForEmail(breachEmailId)
        .asLoadingResult()
        .distinctUntilChanged()
        .onEach {
            if (it is LoadingResult.Error) {
                PassLogger.w(TAG, "Failed to observe breaches for email")
                PassLogger.w(TAG, it.exception)
                snackbarDispatcher(SecurityCenterReportSnackbarMessage.GetBreachesError)
            }
        }

    private val usedInLoginItemsFlow = observeItems(
        selection = ShareSelection.AllShares,
        itemState = ItemState.Active,
        filter = ItemTypeFilter.Logins,
        shareFlags = mapOf(ShareFlag.IsHidden to false)
    ).map { loginItems ->
        loginItems
            .filter { loginItem ->
                (loginItem.itemType as ItemType.Login).itemEmail == email
            }
            .let { usedInLoginItems ->
                encryptionContextProvider.withEncryptionContext {
                    usedInLoginItems.map { usedInLoginItem -> usedInLoginItem.toUiModel(this) }
                }
            }
    }
        .asLoadingResult()
        .distinctUntilChanged()

    private val breachReportFlow: Flow<LoadingResult<BreachEmailReport>> =
        observeBreachEmailReport(breachEmailId)
            .asLoadingResult()
            .onEach {
                if (it is LoadingResult.Error) {
                    PassLogger.w(TAG, "Failed to observe breach email report")
                    PassLogger.w(TAG, it.exception)
                    snackbarDispatcher(SecurityCenterReportSnackbarMessage.GetBreachesError)
                }
            }

    internal val state: StateFlow<SecurityCenterReportState> = combineN(
        breachReportFlow,
        observeBreachForEmailFlow,
        usedInLoginItemsFlow,
        userPreferencesRepository.getUseFaviconsPreference(),
        eventFlow,
        observeVaultsGroupedByShareId(includeHidden = false)
    ) { breachEmailReportResult,
        breachesForEmailResult,
        usedInLoginItemsResult,
        useFavIconsPreference,
        event,
        groupedVaults ->
        SecurityCenterReportState(
            breachEmailId = breachEmailId,
            canLoadExternalImages = useFavIconsPreference.value(),
            breachEmailResult = breachEmailReportResult,
            breachEmailsResult = breachesForEmailResult,
            usedInLoginItemsResult = usedInLoginItemsResult,
            event = event,
            groupedVaults = groupedVaults
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SecurityCenterReportState.Initial
    )

    internal fun onResolveEmailBreaches() {
        eventFlow.update { SecurityCenterReportEvent.OnResolveEmailBreaches }
    }

    internal fun onResolveEmailBreachesConfirmed(emailId: BreachEmailId) = viewModelScope.launch {
        eventFlow.update { SecurityCenterReportEvent.OnResolveEmailBreachesConfirmed }

        runCatching { markEmailBreachAsResolved(breachEmailId = emailId) }
            .onSuccess {
                eventFlow.update { SecurityCenterReportEvent.OnEmailBreachesResolved }
                snackbarDispatcher(BreachResolvedSuccessfully)
            }
            .onError {
                eventFlow.update { SecurityCenterReportEvent.OnResolveEmailBreachesCancelled }
                snackbarDispatcher(BreachResolvedError)
                PassLogger.i(TAG, "Failed to mark as resolved email breach")
                PassLogger.w(TAG, it)
            }
    }

    internal fun onResolveEmailBreachesCancelled() {
        eventFlow.update { SecurityCenterReportEvent.OnResolveEmailBreachesCancelled }
    }

    internal fun consumeEvent(event: SecurityCenterReportEvent) {
        eventFlow.compareAndSet(event, SecurityCenterReportEvent.Idle)
    }

    internal companion object {

        private const val TAG = "SecurityCenterReportViewModel"

    }

}
