package proton.android.pass.autofill.ui.autofill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.extensions.toAutoFillItem
import proton.android.pass.biometry.BiometryManager
import proton.android.pass.biometry.BiometryStatus
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.logError
import proton.android.pass.common.api.map
import proton.android.pass.common.api.onSuccess
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.NotificationManager
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.android.pass.preferences.BiometricLockState
import proton.android.pass.preferences.CopyTotpToClipboard
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.totp.api.TotpManager
import javax.inject.Inject

@HiltViewModel
class AutofillAppViewModel @Inject constructor(
    preferenceRepository: UserPreferencesRepository,
    private val biometryManager: BiometryManager,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val clipboardManager: ClipboardManager,
    private val totpManager: TotpManager,
    private val notificationManager: NotificationManager,
    private val snackbarMessageRepository: SnackbarMessageRepository
) : ViewModel() {

    private val itemSelectedState: MutableStateFlow<AutofillItemSelectedState> =
        MutableStateFlow(AutofillItemSelectedState.Unknown)

    private val themeState: Flow<ThemePreference> = preferenceRepository
        .getThemePreference()
        .distinctUntilChanged()

    private val biometricLockState: Flow<BiometricLockState> = preferenceRepository
        .getBiometricLockState()
        .distinctUntilChanged()

    private val copyTotpToClipboardState: Flow<CopyTotpToClipboard> = preferenceRepository
        .getCopyTotpToClipboardEnabled()
        .distinctUntilChanged()

    val state: StateFlow<AutofillAppUiState> = combine(
        themeState,
        biometricLockState,
        itemSelectedState,
        copyTotpToClipboardState,
        snackbarMessageRepository.snackbarMessage
    ) { theme, fingerprint, itemSelected, copyTotpToClipboard, snackbarMessage ->
        val fingerprintRequired = when (biometryManager.getBiometryStatus()) {
            BiometryStatus.CanAuthenticate -> fingerprint is BiometricLockState.Enabled
            else -> false
        }

        AutofillAppUiState(
            theme = theme,
            isFingerprintRequired = fingerprintRequired,
            itemSelected = itemSelected,
            copyTotpToClipboard = copyTotpToClipboard.value(),
            snackbarMessage = snackbarMessage.value()
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = AutofillAppUiState.Initial
        )

    fun onAutofillItemClicked(
        autofillAppState: AutofillAppState,
        autofillItem: AutofillItem
    ) = viewModelScope.launch {
        when (autofillItem) {
            is AutofillItem.Login -> {
                if (autofillItem.totp.isNotBlank() && state.value.copyTotpToClipboard) {
                    totpManager.parse(autofillItem.totp)
                        .map { totpManager.observeCode(it).first().first }
                        .onSuccess { code ->
                            clipboardManager.copyToClipboard(code)
                        }
                        .logError(PassLogger, TAG, "Could not copy totp code")
                    notificationManager.sendNotification()
                }
            }
            AutofillItem.Unknown -> {}
        }
        updateAutofillItemState(autofillAppState, autofillItem)
    }

    fun onItemCreated(autofillAppState: AutofillAppState, item: ItemUiModel) =
        viewModelScope.launch {
            encryptionContextProvider.withEncryptionContext {
                onAutofillItemClicked(
                    autofillAppState = autofillAppState,
                    autofillItem = item.toAutoFillItem(this@withEncryptionContext)
                )
            }
        }

    fun onSnackbarMessageDelivered() = viewModelScope.launch {
        snackbarMessageRepository.snackbarMessageDelivered()
    }

    private fun updateAutofillItemState(
        autofillAppState: AutofillAppState,
        autofillItem: AutofillItem
    ) {
        val response = ItemFieldMapper.mapFields(
            item = autofillItem,
            androidAutofillFieldIds = autofillAppState.androidAutofillIds,
            autofillTypes = autofillAppState.fieldTypes
        )
        itemSelectedState.update { AutofillItemSelectedState.Selected(response) }
    }

    companion object {
        private const val TAG = "AutofillAppViewModel"
    }
}
