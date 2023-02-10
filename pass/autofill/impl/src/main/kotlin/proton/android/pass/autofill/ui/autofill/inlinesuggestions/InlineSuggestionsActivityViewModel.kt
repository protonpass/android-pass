package proton.android.pass.autofill.ui.autofill.inlinesuggestions

import android.view.autofill.AutofillId
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import proton.android.pass.autofill.entities.AndroidAutofillFieldId
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.AutofillMappings
import proton.android.pass.autofill.entities.FieldType
import proton.android.pass.autofill.extensions.deserializeParcelable
import proton.android.pass.autofill.ui.autofill.ItemFieldMapper
import proton.android.pass.autofill.ui.autofill.inlinesuggestions.InlineSuggestionsNoUiActivity.Companion.ARG_AUTOFILL_IDS
import proton.android.pass.autofill.ui.autofill.inlinesuggestions.InlineSuggestionsNoUiActivity.Companion.ARG_AUTOFILL_TYPES
import proton.android.pass.autofill.ui.autofill.inlinesuggestions.InlineSuggestionsNoUiActivity.Companion.ARG_INLINE_SUGGESTION_AUTOFILL_ITEM
import proton.android.pass.autofill.ui.autofill.inlinesuggestions.InlineSuggestionsNoUiActivity.Companion.ARG_PACKAGE_NAME
import proton.android.pass.autofill.ui.autofill.inlinesuggestions.InlineSuggestionsNoUiActivity.Companion.ARG_TITLE
import proton.android.pass.autofill.ui.autofill.inlinesuggestions.InlineSuggestionsNoUiActivity.Companion.ARG_WEB_DOMAIN
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.toOption
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.UpdateAutofillItem
import proton.android.pass.data.api.usecases.UpdateAutofillItemData
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.NotificationManager
import proton.android.pass.preferences.CopyTotpToClipboard
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.totp.api.GetTotpCodeFromUri
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import proton.pass.domain.entity.PackageName
import javax.inject.Inject

@HiltViewModel
class InlineSuggestionsActivityViewModel @Inject constructor(
    preferenceRepository: UserPreferencesRepository,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val clipboardManager: ClipboardManager,
    private val getTotpCodeFromUri: GetTotpCodeFromUri,
    private val notificationManager: NotificationManager,
    private val updateAutofillItem: UpdateAutofillItem,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val packageName = savedStateHandle.get<String>(ARG_PACKAGE_NAME)
        .toOption()
        .map { PackageName(it) }
    private val webDomain = savedStateHandle.get<String>(ARG_WEB_DOMAIN)
        .toOption()
    private val title = savedStateHandle.get<String>(ARG_TITLE)
        .toOption()
    private val types = savedStateHandle.get<List<String>>(ARG_AUTOFILL_TYPES)
        .toOption()
        .map { list -> list.map(FieldType.Companion::from) }
    private val ids = savedStateHandle.get<List<AutofillId>>(ARG_AUTOFILL_IDS)
        .toOption()
        .map { list -> list.map { AndroidAutofillFieldId(it) } }

    private val autofillAppState: MutableStateFlow<AutofillAppState> =
        MutableStateFlow(
            AutofillAppState(
                packageName = packageName,
                androidAutofillIds = ids.value() ?: emptyList(),
                fieldTypes = types.value() ?: emptyList(),
                webDomain = webDomain,
                title = title.value() ?: ""
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
        PassLogger.d(TAG, autofillAppState.toString())
        val mappingsOption = autofillItemOption
            .map { autofillItem ->
                getMappings(autofillItem, copyTotpToClipboard, autofillAppState)
            }
        when (mappingsOption) {
            None -> InlineSuggestionAutofillNoUiState.Error
            is Some -> InlineSuggestionAutofillNoUiState.Success(mappingsOption.value)
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
            val totpUri = decrypt(autofillItem.totp)
            if (totpUri.isNotBlank() && copyTotpToClipboard.value()) {
                viewModelScope.launch {
                    getTotpCodeFromUri(totpUri)
                        .onSuccess {
                            withContext(Dispatchers.IO) {
                                clipboardManager.copyToClipboard(it)
                            }
                            notificationManager.sendNotification()
                        }
                        .onFailure { PassLogger.w(TAG, "Could not copy totp code") }
                }
            }
            updateAutofillItem(
                UpdateAutofillItemData(
                    shareId = ShareId(autofillItem.shareId),
                    itemId = ItemId(autofillItem.itemId),
                    packageName = autofillAppState.packageName,
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
        private const val TAG = "InlineSuggestionsActivityViewModel"
    }
}
