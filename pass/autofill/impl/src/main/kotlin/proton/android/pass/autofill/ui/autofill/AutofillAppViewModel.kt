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

package proton.android.pass.autofill.ui.autofill

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.autofill.AutofillDisplayed
import proton.android.pass.autofill.AutofillDone
import proton.android.pass.autofill.AutofillTriggerSource
import proton.android.pass.autofill.MFAAutofillCopied
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.autofill.extensions.isBrowser
import proton.android.pass.autofill.extensions.toAutoFillItem
import proton.android.pass.autofill.heuristics.ItemFieldMapper
import proton.android.pass.autofill.service.R
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.UpdateAutofillItem
import proton.android.pass.data.api.usecases.UpdateAutofillItemData
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.entity.PackageName
import proton.android.pass.inappreview.api.InAppReviewTriggerMetrics
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.ToastManager
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.preferences.LastItemAutofillPreference
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.telemetry.api.TelemetryManager
import proton.android.pass.totp.api.GetTotpCodeFromUri
import javax.inject.Inject

@Immutable
sealed interface AutofillAppEvent {
    @Immutable
    data object Unknown : AutofillAppEvent

    @Immutable
    data object Cancel : AutofillAppEvent

    @Immutable
    data class ShowAssociateDialog(
        val item: ItemUiModel
    ) : AutofillAppEvent

    @Immutable
    data class ShowWarningDialog(
        val item: ItemUiModel
    ) : AutofillAppEvent

    @Immutable
    @JvmInline
    value class SendResponse(val mappings: AutofillMappings) : AutofillAppEvent
}

@HiltViewModel
class AutofillAppViewModel @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider,
    private val clipboardManager: ClipboardManager,
    private val getTotpCodeFromUri: GetTotpCodeFromUri,
    private val toastManager: ToastManager,
    private val updateAutofillItem: UpdateAutofillItem,
    private val preferenceRepository: UserPreferencesRepository,
    private val telemetryManager: TelemetryManager,
    private val inAppReviewTriggerMetrics: InAppReviewTriggerMetrics,
    private val getItemById: GetItemById,
    private val internalSettingsRepository: InternalSettingsRepository,
    private val clock: Clock
) : ViewModel() {

    private var hadSelectedAutofillItem: Option<Boolean> = None

    private val _eventFlow: MutableStateFlow<AutofillAppEvent> =
        MutableStateFlow(AutofillAppEvent.Unknown)

    internal val stateFlow: StateFlow<AutofillAppEvent> = _eventFlow

    internal fun setHadSelectedAutofillItem(value: Boolean) {
        hadSelectedAutofillItem = value.some()
    }

    internal fun onSelectItemScreenShown(state: AutofillAppState) {
        val event = with(state.autofillData) {
            AutofillDisplayed(
                source = AutofillTriggerSource.App,
                eventItemType = assistInfo.cluster.eventItemType(),
                app = packageInfo.packageName
            )
        }
        telemetryManager.sendEvent(event)
    }

    internal fun onItemSelected(
        state: AutofillAppState,
        autofillItem: AutofillItem,
        isSuggestion: Boolean
    ) {
        viewModelScope.launch {
            val item = runCatching { getItemById(autofillItem.shareId(), autofillItem.itemId()) }
                .getOrNull()
                ?: run {
                    PassLogger.d(TAG, "Could not get item isSuggestion = $isSuggestion")
                    return@launch
                }
            val itemUiModel = encryptionContextProvider.withEncryptionContext {
                item.toUiModel(this@withEncryptionContext)
            }

            when {
                state.autofillData.isDangerousAutofill -> _eventFlow.update {
                    AutofillAppEvent.ShowWarningDialog(itemUiModel)
                }

                !isSuggestion && shouldAskForAssociation(itemUiModel.contents, state) -> {
                    _eventFlow.update {
                        AutofillAppEvent.ShowAssociateDialog(itemUiModel)
                    }
                }

                else -> sendMappings(
                    item = autofillItem,
                    state = state,
                    associate = autofillItem.shouldAssociate()
                )
            }
        }
    }

    internal fun onAssociationResult(
        state: AutofillAppState,
        item: ItemUiModel,
        associate: Boolean
    ) {
        viewModelScope.launch {
            sendMappings(item.toAutoFillItem(), state, associate)
        }
    }

    internal fun onWarningConfirmed(state: AutofillAppState, item: ItemUiModel) {
        viewModelScope.launch {
            if (shouldAskForAssociation(state = state, item = item.contents)) {
                _eventFlow.update { AutofillAppEvent.ShowAssociateDialog(item) }
            } else {
                sendMappings(item.toAutoFillItem(), state, false)
            }
        }
    }

    internal fun onAssociationCancelled(isInlineSuggestionSession: Boolean) {
        viewModelScope.launch {
            if (isInlineSuggestionSession) {
                _eventFlow.update { AutofillAppEvent.Cancel }
            }
        }
    }

    internal fun clearEvent() {
        _eventFlow.update { AutofillAppEvent.Unknown }
    }

    private fun getMappings(autofillItem: AutofillItem, autofillAppState: AutofillAppState): AutofillMappings {
        if (autofillItem is AutofillItem.Login) {
            handleTotpUri(autofillItem.totp)
        }

        return encryptionContextProvider.withEncryptionContext {
            ItemFieldMapper.mapFields(
                encryptionContext = this@withEncryptionContext,
                autofillItem = autofillItem,
                cluster = autofillAppState.autofillData.assistInfo.cluster
            )
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal suspend fun sendMappings(
        item: AutofillItem,
        state: AutofillAppState,
        associate: Boolean
    ) {
        setLastItemAutofillForMultiStep(item)
        sendItemSelectedTelemetry(state)
        val (updatePackageInfo, updateUrl) = state.updateAutofillFields()
        val data = UpdateAutofillItemData(
            shareId = item.shareId(),
            itemId = item.itemId(),
            packageInfo = updatePackageInfo,
            url = updateUrl,
            shouldAssociate = associate
        )
        updateAutofillItem(data)

        val mappings = getMappings(item, state)
        if (mappings.mappings.isNotEmpty()) {
            _eventFlow.update { AutofillAppEvent.SendResponse(mappings) }
        } else {
            _eventFlow.update { AutofillAppEvent.Cancel }
        }
    }

    private fun setLastItemAutofillForMultiStep(item: AutofillItem) {
        internalSettingsRepository.setLastItemAutofill(
            LastItemAutofillPreference(
                itemId = item.itemId().id,
                shareId = item.shareId().id,
                lastAutofillTimestamp = clock.now().epochSeconds
            )
        )
    }

    private suspend fun sendItemSelectedTelemetry(state: AutofillAppState) {
        val source = if (hadSelectedAutofillItem.value() == true) {
            // We had an item selected
            AutofillTriggerSource.Source
        } else {
            // We didn't have an item selected, so the user must have opened the app
            AutofillTriggerSource.App
        }

        val event = with(state.autofillData) {
            AutofillDone(
                source = source,
                eventItemType = assistInfo.cluster.eventItemType(),
                app = packageInfo.packageName
            )
        }
        telemetryManager.sendEvent(event)
        inAppReviewTriggerMetrics.incrementItemAutofillCount()
    }

    private fun handleTotpUri(totp: EncryptedString?) {
        if (totp == null) return

        val totpUri = encryptionContextProvider.withEncryptionContext { decrypt(totp) }
        viewModelScope.launch {
            val copyTotpToClipboard = preferenceRepository.getCopyTotpToClipboardEnabled().first()
            if (totpUri.isNotBlank() && copyTotpToClipboard.value()) {
                getTotpCodeFromUri(totpUri)
                    .onSuccess {
                        clipboardManager.copyToClipboard(it)
                        telemetryManager.sendEvent(MFAAutofillCopied)
                        toastManager.showToast(R.string.autofill_notification_copy_to_clipboard)
                    }
                    .onFailure {
                        PassLogger.w(TAG, "Could not copy totp code")
                    }
            }
        }
    }

    private companion object {
        private const val TAG = "AutofillAppViewModel"

        private fun shouldAskForAssociation(item: ItemContents, state: AutofillAppState): Boolean = when (item) {
            is ItemContents.Login -> shouldAskForAssociation(
                item = item,
                packageName = state.autofillData.packageInfo.packageName,
                webDomain = state.autofillData.assistInfo.url.value()
            )

            else -> false
        }

        @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
        fun shouldAskForAssociation(
            item: ItemContents.Login,
            packageName: PackageName,
            webDomain: String?
        ): Boolean = when {
            // If the package name is not a browser and the package name is already associated with the item
            // do not ask for association
            !packageName.isBrowser() && item.packageInfoSet.map {
                it.packageName
            }.contains(packageName) -> false

            // If the package name is not a browser and there is no web domain, ask for association
            !packageName.isBrowser() && webDomain.isNullOrBlank() -> true

            // From then on we are sure that there is a webDomain
            // Check if is already there, and if it is, do not ask for association
            !webDomain.isNullOrBlank() && item.urls.contains(webDomain) -> false

            else -> true
        }
    }
}
