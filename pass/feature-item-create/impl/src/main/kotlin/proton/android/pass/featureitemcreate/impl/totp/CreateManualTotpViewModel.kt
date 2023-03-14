package proton.android.pass.featureitemcreate.impl.totp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.totp.api.TotpAlgorithm
import proton.android.pass.totp.api.TotpDigits
import proton.android.pass.totp.api.TotpManager
import javax.inject.Inject

@HiltViewModel
class CreateManualTotpViewModel @Inject constructor(
    private val totpManager: TotpManager
) : ViewModel() {

    private val _state: MutableStateFlow<CreateManualTotpUiState> =
        MutableStateFlow(CreateManualTotpUiState.Initial)
    val state: StateFlow<CreateManualTotpUiState> = _state
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CreateManualTotpUiState.Initial
        )

    fun onAddManualTotp(totpSpecUi: TotpSpecUi) {
        _state.update { it.copy(isLoadingState = IsLoadingState.Loading) }
        val validationErrors = totpSpecUi.validate()
        if (validationErrors.isNotEmpty()) {
            _state.update {
                it.copy(
                    validationErrors = validationErrors,
                    isLoadingState = IsLoadingState.NotLoading
                )
            }
        } else {
            val uri = totpManager.generateUri(totpSpecUi.toTotSpec())
            _state.update {
                it.copy(
                    validationErrors = emptySet(),
                    isTotpUriCreatedState = IsTotpUriCreatedState.Success(uri),
                    isLoadingState = IsLoadingState.NotLoading
                )
            }
        }
    }

    fun onSecretChange(value: String) {
        _state.update { state ->
            state.copy(
                totpSpec = state.totpSpec.copy(secret = value.trim())
            )
        }
    }

    fun onValidPeriodChange(value: Int?) {
        _state.update { state ->
            state.copy(
                totpSpec = state.totpSpec.copy(validPeriodSeconds = value)
            )
        }
    }

    fun onDigitsChange(value: TotpDigits) {
        _state.update { state ->
            state.copy(
                totpSpec = state.totpSpec.copy(digits = value)
            )
        }
    }

    fun onAlgorithmChange(value: TotpAlgorithm) {
        _state.update { state ->
            state.copy(
                totpSpec = state.totpSpec.copy(algorithm = value)
            )
        }
    }

    fun onIssuerChange(value: String) {
        _state.update { state ->
            state.copy(
                totpSpec = state.totpSpec.copy(issuer = value.trim())
            )
        }
    }

    fun onLabelChange(value: String) {
        _state.update { state ->
            state.copy(
                totpSpec = state.totpSpec.copy(label = value.trim())
            )
        }
    }
}
