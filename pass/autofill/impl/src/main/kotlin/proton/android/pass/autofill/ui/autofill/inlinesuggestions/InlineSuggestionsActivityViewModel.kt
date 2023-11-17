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

import android.view.autofill.AutofillId
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.autofill.AutofillDone
import proton.android.pass.autofill.AutofillTriggerSource
import proton.android.pass.autofill.MFAAutofillCopied
import proton.android.pass.autofill.entities.AndroidAutofillFieldId
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.autofill.entities.FieldType
import proton.android.pass.autofill.extensions.deserializeParcelable
import proton.android.pass.autofill.service.R
import proton.android.pass.autofill.ui.autofill.AutofillIdListList
import proton.android.pass.autofill.ui.autofill.AutofillIntentExtras.ARG_APP_NAME
import proton.android.pass.autofill.ui.autofill.AutofillIntentExtras.ARG_AUTOFILL_IDS
import proton.android.pass.autofill.ui.autofill.AutofillIntentExtras.ARG_AUTOFILL_IS_FOCUSED
import proton.android.pass.autofill.ui.autofill.AutofillIntentExtras.ARG_AUTOFILL_PARENT_ID
import proton.android.pass.autofill.ui.autofill.AutofillIntentExtras.ARG_AUTOFILL_TYPES
import proton.android.pass.autofill.ui.autofill.AutofillIntentExtras.ARG_INLINE_SUGGESTION_AUTOFILL_ITEM
import proton.android.pass.autofill.ui.autofill.AutofillIntentExtras.ARG_PACKAGE_NAME
import proton.android.pass.autofill.ui.autofill.AutofillIntentExtras.ARG_TITLE
import proton.android.pass.autofill.ui.autofill.AutofillIntentExtras.ARG_WEB_DOMAIN
import proton.android.pass.autofill.heuristics.ItemFieldMapper
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.crypto.api.context.EncryptionContext
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.UpdateAutofillItem
import proton.android.pass.data.api.usecases.UpdateAutofillItemData
import proton.android.pass.inappreview.api.InAppReviewTriggerMetrics
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.ToastManager
import proton.android.pass.preferences.CopyTotpToClipboard
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.telemetry.api.TelemetryManager
import proton.android.pass.totp.api.GetTotpCodeFromUri
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class InlineSuggestionsActivityViewModel @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider,
    private val clipboardManager: ClipboardManager,
    private val getTotpCodeFromUri: GetTotpCodeFromUri,
    private val toastManager: ToastManager,
    private val updateAutofillItem: UpdateAutofillItem,
    private val telemetryManager: TelemetryManager,
    preferenceRepository: UserPreferencesRepository,
    inAppReviewTriggerMetrics: InAppReviewTriggerMetrics,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val packageInfo = savedStateHandle.get<String>(ARG_PACKAGE_NAME)
        .toOption()
        .map { packageName ->
            PackageInfoUi(
                packageName = packageName,
                appName = savedStateHandle.get<String>(ARG_APP_NAME) ?: packageName
            )
        }
    private val webDomain: Option<String> = savedStateHandle.get<String>(ARG_WEB_DOMAIN)
        .toOption()
    private val title: Option<String> = savedStateHandle.get<String>(ARG_TITLE)
        .toOption()
    private val types: Option<List<FieldType>> =
        savedStateHandle.get<List<String>>(ARG_AUTOFILL_TYPES)
            .toOption()
            .map { list -> list.map(FieldType.Companion::from) }
    private val ids: Option<List<AndroidAutofillFieldId?>> =
        savedStateHandle.get<List<AutofillId?>>(ARG_AUTOFILL_IDS)
            .toOption()
            .map { list -> list.map { item -> item?.let { AndroidAutofillFieldId(it) } } }
    private val fieldIsFocusedList: Option<List<Boolean>> =
        savedStateHandle.get<BooleanArray>(ARG_AUTOFILL_IS_FOCUSED)
            .toOption()
            .map { it.toList() }
    private val parentIdList: Option<List<List<AndroidAutofillFieldId>>> =
        savedStateHandle.get<ByteArray>(ARG_AUTOFILL_PARENT_ID)
            ?.deserializeParcelable<AutofillIdListList>()
            .toOption()
            .map { list ->
                list.content.map { item ->
                    item.autofillIds.map { autofillId -> AndroidAutofillFieldId(autofillId) }
                }
            }

    private val autofillAppState: MutableStateFlow<AutofillAppState> =
        MutableStateFlow(
            AutofillAppState(
                androidAutofillIds = ids.value() ?: emptyList(),
                fieldTypes = types.value() ?: emptyList(),
                packageInfoUi = packageInfo.value(),
                webDomain = webDomain,
                title = title.value() ?: "",
                fieldIsFocusedList = fieldIsFocusedList.value() ?: emptyList(),
                parentIdList = parentIdList.value() ?: emptyList(),
            )
        )

    private val autofillItemState: MutableStateFlow<Option<AutofillItem>> =
        MutableStateFlow(
            savedStateHandle.get<ByteArray>(ARG_INLINE_SUGGESTION_AUTOFILL_ITEM)
                ?.deserializeParcelable<AutofillItem>()
                .toOption()
        )

    private val copyTotpToClipboardState = preferenceRepository
        .getCopyTotpToClipboardEnabled()
        .distinctUntilChanged()

    val state: StateFlow<InlineSuggestionAutofillNoUiState> = combine(
        autofillAppState,
        autofillItemState,
        copyTotpToClipboardState
    ) { autofillAppState, autofillItemOption, copyTotpToClipboard ->
        val mappingsOption = autofillItemOption
            .map { autofillItem ->
                getMappings(autofillItem, copyTotpToClipboard, autofillAppState)
            }
        if (mappingsOption is Some) {
            if (mappingsOption.value.mappings.isNotEmpty()) {
                telemetryManager.sendEvent(AutofillDone(AutofillTriggerSource.Source))
                inAppReviewTriggerMetrics.incrementItemAutofillCount()
                InlineSuggestionAutofillNoUiState.Success(mappingsOption.value)
            } else {
                PassLogger.i(TAG, "Empty mappings")
                InlineSuggestionAutofillNoUiState.Error
            }
        } else {
            PassLogger.i(TAG, "No mappings found")
            InlineSuggestionAutofillNoUiState.Error
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
    ): AutofillMappings =
        encryptionContextProvider.withEncryptionContext {
            handleTotpUri(
                encryptionContext = this@withEncryptionContext,
                copyTotpToClipboard = copyTotpToClipboard,
                totp = autofillItem.totp
            )

            updateAutofillItem(
                UpdateAutofillItemData(
                    shareId = ShareId(autofillItem.shareId),
                    itemId = ItemId(autofillItem.itemId),
                    packageInfo = autofillAppState.packageInfoUi.toOption()
                        .map(PackageInfoUi::toPackageInfo),
                    url = autofillAppState.webDomain,
                    shouldAssociate = false
                )
            )

            ItemFieldMapper.mapFields(
                encryptionContext = this@withEncryptionContext,
                autofillItem = autofillItem,
                androidAutofillFieldIds = autofillAppState.androidAutofillIds,
                autofillTypes = autofillAppState.fieldTypes,
                fieldIsFocusedList = autofillAppState.fieldIsFocusedList,
                parentIdList = autofillAppState.parentIdList
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
