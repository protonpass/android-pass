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

package proton.android.pass.autofill.ui.autofill.inlinesuggestions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.autofill.AutofillDone
import proton.android.pass.autofill.AutofillTriggerSource
import proton.android.pass.autofill.MFAAutofillCopied
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.autofill.heuristics.ItemFieldMapper
import proton.android.pass.autofill.service.R
import proton.android.pass.autofill.ui.autofill.AutofillIntentExtras
import proton.android.pass.autofill.ui.autofill.common.AutofillConfirmMode
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.commonui.api.require
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.UpdateAutofillItem
import proton.android.pass.data.api.usecases.UpdateAutofillItemData
import proton.android.pass.inappreview.api.InAppReviewTriggerMetrics
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.ToastManager
import proton.android.pass.preferences.CopyTotpToClipboard
import proton.android.pass.preferences.InternalSettingsRepository
import proton.android.pass.preferences.LastItemAutofillPreference
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.telemetry.api.TelemetryManager
import proton.android.pass.totp.api.GetTotpCodeFromUri
import javax.inject.Inject

@HiltViewModel
class InlineSuggestionsActivityViewModel @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider,
    private val clipboardManager: ClipboardManager,
    private val getTotpCodeFromUri: GetTotpCodeFromUri,
    private val toastManager: ToastManager,
    private val updateAutofillItem: UpdateAutofillItem,
    private val telemetryManager: TelemetryManager,
    private val internalSettingsRepository: InternalSettingsRepository,
    preferenceRepository: UserPreferencesRepository,
    inAppReviewTriggerMetrics: InAppReviewTriggerMetrics,
    clock: Clock,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val extras = AutofillIntentExtras.fromExtras(
        savedStateHandle.require(AutofillIntentExtras.ARG_EXTRAS_BUNDLE)
    )

    private val appState = AutofillAppState(extras.first)
    private val selectedAutofillItem: Option<AutofillItem> = extras.second

    init {
        val preference = selectedAutofillItem.map {
            LastItemAutofillPreference(
                itemId = it.itemId().id,
                shareId = it.shareId().id,
                lastAutofillTimestamp = clock.now().epochSeconds
            )
        }
        preference.value()?.let { internalSettingsRepository.setLastItemAutofill(it) }
    }

    private val copyTotpToClipboardState = preferenceRepository
        .getCopyTotpToClipboardEnabled()
        .distinctUntilChanged()

    private val themeState: Flow<ThemePreference> = preferenceRepository
        .getThemePreference()
        .distinctUntilChanged()

    val state: StateFlow<InlineSuggestionAutofillNoUiState> = combine(
        copyTotpToClipboardState,
        themeState
    ) { copyTotpToClipboard, theme ->
        val mappingsOption = selectedAutofillItem
            .map { autofillItem ->
                getMappings(autofillItem, copyTotpToClipboard, appState)
            }
        if (mappingsOption is Some) {
            if (mappingsOption.value.mappings.isNotEmpty()) {
                val event = with(appState.autofillData) {
                    AutofillDone(
                        source = AutofillTriggerSource.Source,
                        eventItemType = assistInfo.cluster.eventItemType(),
                        app = packageInfo.packageName
                    )
                }
                telemetryManager.sendEvent(event)
                inAppReviewTriggerMetrics.incrementItemAutofillCount()
                PassLogger.i(TAG, "Mappings found: ${mappingsOption.value.mappings.size}")

                if (appState.autofillData.isDangerousAutofill) {
                    InlineSuggestionAutofillNoUiState.SuccessWithConfirmation(
                        autofillMappings = mappingsOption.value,
                        mode = AutofillConfirmMode.DangerousAutofill,
                        theme = theme
                    )
                } else {
                    InlineSuggestionAutofillNoUiState.Success(mappingsOption.value)
                }
            } else {
                PassLogger.i(TAG, "Empty mappings")
                InlineSuggestionAutofillNoUiState.Close
            }
        } else {
            PassLogger.i(TAG, "No mappings found")
            InlineSuggestionAutofillNoUiState.Close
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = InlineSuggestionAutofillNoUiState.NotInitialised
        )

    private fun getMappings(
        autofillItem: AutofillItem,
        copyTotpToClipboard: CopyTotpToClipboard,
        autofillAppState: AutofillAppState
    ): AutofillMappings = encryptionContextProvider.withEncryptionContext {
        if (autofillItem is AutofillItem.Login) {
            handleTotpUri(
                encryptionContext = this@withEncryptionContext,
                copyTotpToClipboard = copyTotpToClipboard,
                totp = autofillItem.totp
            )
        }

        val (updatePackageInfo, updateUrl) = autofillAppState.updateAutofillFields()
        val data = UpdateAutofillItemData(
            shareId = autofillItem.shareId(),
            itemId = autofillItem.itemId(),
            packageInfo = updatePackageInfo,
            url = updateUrl,
            shouldAssociate = autofillItem.shouldAssociate()
        )
        updateAutofillItem(data)

        ItemFieldMapper.mapFields(
            encryptionContext = this@withEncryptionContext,
            autofillItem = autofillItem,
            cluster = autofillAppState.autofillData.assistInfo.cluster
        )
    }


    private fun handleTotpUri(
        encryptionContext: EncryptionContext,
        copyTotpToClipboard: CopyTotpToClipboard,
        totp: EncryptedString?
    ) {
        if (totp == null) return

        val totpUri = encryptionContext.decrypt(totp)
        if (totpUri.isNotBlank() && copyTotpToClipboard.value()) {
            viewModelScope.launch {
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

    companion object {
        private const val TAG = "InlineSuggestionsActivityViewModel"
    }
}
