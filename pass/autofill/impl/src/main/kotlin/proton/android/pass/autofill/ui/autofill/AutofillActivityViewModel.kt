package proton.android.pass.autofill.ui.autofill

import android.view.autofill.AutofillId
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.autofill.entities.AndroidAutofillFieldId
import proton.android.pass.autofill.entities.AutofillAppState
import proton.android.pass.autofill.entities.AutofillItem
import proton.android.pass.autofill.entities.FieldType
import proton.android.pass.autofill.entities.isValid
import proton.android.pass.autofill.extensions.deserializeParcelable
import proton.android.pass.autofill.ui.autofill.AutofillActivity.Companion.ARG_APP_NAME
import proton.android.pass.autofill.ui.autofill.AutofillActivity.Companion.ARG_AUTOFILL_IDS
import proton.android.pass.autofill.ui.autofill.AutofillActivity.Companion.ARG_AUTOFILL_TYPES
import proton.android.pass.autofill.ui.autofill.AutofillActivity.Companion.ARG_INLINE_SUGGESTION_AUTOFILL_ITEM
import proton.android.pass.autofill.ui.autofill.AutofillActivity.Companion.ARG_PACKAGE_NAME
import proton.android.pass.autofill.ui.autofill.AutofillActivity.Companion.ARG_TITLE
import proton.android.pass.autofill.ui.autofill.AutofillActivity.Companion.ARG_WEB_DOMAIN
import proton.android.pass.autofill.ui.autofill.AutofillUiState.NotValidAutofillUiState
import proton.android.pass.autofill.ui.autofill.AutofillUiState.StartAutofillUiState
import proton.android.pass.autofill.ui.autofill.AutofillUiState.UninitialisedAutofillUiState
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.preferences.BiometricLockState
import proton.android.pass.preferences.ThemePreference
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import javax.inject.Inject

@HiltViewModel
class AutofillActivityViewModel @Inject constructor(
    preferenceRepository: UserPreferencesRepository,
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
                packageInfoUi = packageInfo.value(),
                androidAutofillIds = ids.value() ?: emptyList(),
                fieldTypes = types.value() ?: emptyList(),
                webDomain = webDomain,
                title = title.value() ?: ""
            )
        )

    private val selectedAutofillItemState: MutableStateFlow<Option<AutofillItem>> =
        MutableStateFlow(
            savedStateHandle.get<ByteArray>(ARG_INLINE_SUGGESTION_AUTOFILL_ITEM)
                ?.deserializeParcelable<AutofillItem>()
                .toOption()
        )

    private val copyTotpToClipboardPreferenceState = preferenceRepository
        .getCopyTotpToClipboardEnabled()
        .distinctUntilChanged()

    private val themePreferenceState: Flow<ThemePreference> = preferenceRepository
        .getThemePreference()
        .distinctUntilChanged()

    private val biometricLockState: Flow<BiometricLockState> = preferenceRepository
        .getBiometricLockState()
        .distinctUntilChanged()

    val state: StateFlow<AutofillUiState> = combine(
        themePreferenceState,
        biometricLockState,
        autofillAppState,
        selectedAutofillItemState,
        copyTotpToClipboardPreferenceState
    ) { themePreference, biometricLock, autofillAppState, selectedAutofillItem, copyTotpToClipboard ->
        when {
            autofillAppState.isValid() -> NotValidAutofillUiState
            else -> StartAutofillUiState(
                themePreference = themePreference.value(),
                isFingerprintRequiredPreference = biometricLock.value(),
                autofillAppState = autofillAppState,
                copyTotpToClipboardPreference = copyTotpToClipboard.value(),
                selectedAutofillItem = selectedAutofillItem
            )
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = UninitialisedAutofillUiState
        )
}
