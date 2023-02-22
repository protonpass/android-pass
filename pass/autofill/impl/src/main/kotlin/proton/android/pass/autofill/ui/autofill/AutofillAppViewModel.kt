package proton.android.pass.autofill.ui.autofill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.UpdateAutofillItem
import proton.android.pass.data.api.usecases.UpdateAutofillItemData
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.NotificationManager
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.totp.api.GetTotpCodeFromUri
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class AutofillAppViewModel @Inject constructor(
    private val encryptionContextProvider: EncryptionContextProvider,
    private val clipboardManager: ClipboardManager,
    private val getTotpCodeFromUri: GetTotpCodeFromUri,
    private val notificationManager: NotificationManager,
    private val updateAutofillItem: UpdateAutofillItem,
    private val preferenceRepository: UserPreferencesRepository
) : ViewModel() {

    fun getMappings(
        autofillItem: AutofillItem,
        autofillAppState: AutofillAppState
    ): AutofillMappings =
        encryptionContextProvider.withEncryptionContext {
            val totpUri = decrypt(autofillItem.totp)
            val copyTotpToClipboard = runBlocking {
                preferenceRepository.getCopyTotpToClipboardEnabled().first()
            }
            if (totpUri.isNotBlank() && copyTotpToClipboard.value()) {
                viewModelScope.launch {
                    getTotpCodeFromUri(totpUri)
                        .onSuccess {
                            withContext(Dispatchers.IO) {
                                clipboardManager.copyToClipboard(it)
                            }
                            notificationManager.sendNotification()
                        }
                        .onFailure {
                            PassLogger.w(TAG, "Could not copy totp code")
                        }
                }
            }
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
                autofillTypes = autofillAppState.fieldTypes
            )
        }

    companion object {
        private const val TAG = "AutofillAppViewModel"
    }
}
