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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.proton.core.crypto.common.keystore.EncryptedString
import proton.android.pass.autofill.AutofillDone
import proton.android.pass.autofill.AutofillTriggerSource
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.autofill.service.R
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.UpdateAutofillItem
import proton.android.pass.data.api.usecases.UpdateAutofillItemData
import proton.android.pass.inappreview.api.InAppReviewTriggerMetrics
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.ToastManager
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.telemetry.api.TelemetryManager
import proton.android.pass.totp.api.GetTotpCodeFromUri
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class AutofillAppViewModel @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider,
    private val clipboardManager: ClipboardManager,
    private val getTotpCodeFromUri: GetTotpCodeFromUri,
    private val toastManager: ToastManager,
    private val updateAutofillItem: UpdateAutofillItem,
    private val preferenceRepository: UserPreferencesRepository,
    private val telemetryManager: TelemetryManager,
    private val inAppReviewTriggerMetrics: InAppReviewTriggerMetrics
) : ViewModel() {

    fun getMappings(
        autofillItem: AutofillItem,
        autofillAppState: AutofillAppState
    ): AutofillMappings {
        handleTotpUri(autofillItem.totp)
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

        return encryptionContextProvider.withEncryptionContext {
            ItemFieldMapper.mapFields(
                encryptionContext = this@withEncryptionContext,
                autofillItem = autofillItem,
                androidAutofillFieldIds = autofillAppState.androidAutofillIds,
                autofillTypes = autofillAppState.fieldTypes,
                fieldIsFocusedList = autofillAppState.fieldIsFocusedList,
                parentIdList = autofillAppState.parentIdList
            )
        }
    }


    fun onAutofillItemSelected(source: AutofillTriggerSource) = viewModelScope.launch {
        telemetryManager.sendEvent(AutofillDone(source))
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
                        toastManager.showToast(R.string.autofill_notification_copy_to_clipboard)
                    }
                    .onFailure {
                        PassLogger.w(TAG, "Could not copy totp code")
                    }
            }
        }
    }

    companion object {
        private const val TAG = "AutofillAppViewModel"
    }
}
