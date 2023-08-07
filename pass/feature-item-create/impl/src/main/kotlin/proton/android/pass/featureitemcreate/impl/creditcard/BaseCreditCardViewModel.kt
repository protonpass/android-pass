package proton.android.pass.featureitemcreate.impl.creditcard

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.CommonRegex.NON_DIGIT_REGEX
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState.NotLoading
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.toEncryptedByteArray
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.common.UIHiddenState

abstract class BaseCreditCardViewModel(
    private val encryptionContextProvider: EncryptionContextProvider,
    canPerformPaidAction: CanPerformPaidAction,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    protected val isLoadingState: MutableStateFlow<IsLoadingState> = MutableStateFlow(NotLoading)
    private val hasUserEditedContentState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val validationErrorsState: MutableStateFlow<Set<CreditCardValidationErrors>> =
        MutableStateFlow(emptySet())
    protected val isItemSavedState: MutableStateFlow<ItemSavedState> =
        MutableStateFlow(ItemSavedState.Unknown)

    @OptIn(SavedStateHandleSaveableApi::class)
    protected var creditCardFormItemState: CreditCardFormItem by savedStateHandleProvider.get()
        .saveable {
            mutableStateOf(
                encryptionContextProvider.withEncryptionContext {
                    CreditCardFormItem.default(this)
                }
            )
        }
    val creditCardFormItem: CreditCardFormItem get() = creditCardFormItemState

    val baseState: StateFlow<BaseCreditCardUiState> = combine(
        isLoadingState,
        hasUserEditedContentState,
        validationErrorsState,
        isItemSavedState,
        canPerformPaidAction()
    ) { isLoading, hasUserEditedContent, validationErrors, isItemSaved, canPerformPaidAction ->
        BaseCreditCardUiState(
            isLoading = isLoading.value(),
            hasUserEditedContent = hasUserEditedContent,
            validationErrors = validationErrors.toPersistentSet(),
            isItemSaved = isItemSaved,
            isDowngradedMode = !canPerformPaidAction
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BaseCreditCardUiState.DEFAULT
        )

    fun onTitleChange(value: String) {
        onUserEditedContent()
        creditCardFormItemState = creditCardFormItemState.copy(title = value)
        validationErrorsState.update {
            it.toMutableSet().apply { remove(CreditCardValidationErrors.BlankTitle) }
        }
    }

    fun onNameChanged(value: String) {
        onUserEditedContent()
        creditCardFormItemState = creditCardFormItemState.copy(cardHolder = value)
    }

    fun onNumberChanged(value: String) {
        val sanitisedValue = value.replace(NON_DIGIT_REGEX, "").take(19)
        onUserEditedContent()
        creditCardFormItemState = creditCardFormItemState.copy(number = sanitisedValue)
    }

    fun onCVVChanged(value: String) {
        val sanitisedValue = value.replace(NON_DIGIT_REGEX, "").take(CVV_MAX_LENGTH)
        onUserEditedContent()
        creditCardFormItemState = encryptionContextProvider.withEncryptionContext {
            if (sanitisedValue.isNotBlank()) {
                creditCardFormItemState.copy(
                    cvv = UIHiddenState.Revealed(
                        encrypt(sanitisedValue),
                        sanitisedValue
                    )
                )
            } else {
                creditCardFormItemState.copy(cvv = UIHiddenState.Empty(encrypt(sanitisedValue)))
            }
        }
    }

    fun onPinChanged(value: String) {
        val sanitisedValue = value.replace(NON_DIGIT_REGEX, "").take(PIN_MAX_LENGTH)
        onUserEditedContent()
        creditCardFormItemState = encryptionContextProvider.withEncryptionContext {
            if (sanitisedValue.isNotBlank()) {
                creditCardFormItemState.copy(
                    pin = UIHiddenState.Revealed(
                        encrypt(sanitisedValue),
                        sanitisedValue
                    )
                )
            } else {
                creditCardFormItemState.copy(pin = UIHiddenState.Empty(encrypt(sanitisedValue)))
            }
        }
    }

    fun onExpirationDateChanged(value: String) {
        val sanitisedValue = value.replace(NON_DIGIT_REGEX, "").take(6)
        val converted = adaptToProtoFormat(sanitisedValue)
        onUserEditedContent()
        creditCardFormItemState = creditCardFormItemState.copy(expirationDate = converted)
        validationErrorsState.update {
            it.toMutableSet().apply { remove(CreditCardValidationErrors.InvalidExpirationDate) }
        }
    }

    private fun adaptToProtoFormat(value: String): String =
        if (value.length < 2) {
            value
        } else {
            val firstPart = value.substring(0, 2)
            val secondPart = value.substring(2)
            "$secondPart-$firstPart"
        }

    fun onNoteChanged(value: String) {
        onUserEditedContent()
        creditCardFormItemState = creditCardFormItemState.copy(note = value)
    }

    protected fun validateItem(): Boolean {
        val validationErrors = creditCardFormItemState.validate()
        if (validationErrors.isNotEmpty()) {
            validationErrorsState.update { validationErrors }
            return false
        }
        return true
    }

    protected fun onUserEditedContent() {
        if (hasUserEditedContentState.value) return
        hasUserEditedContentState.update { true }
    }

    fun onCVVFocusChanged(isFocused: Boolean) {
        val state = creditCardFormItemState
        creditCardFormItemState = encryptionContextProvider.withEncryptionContext {
            val decryptedByteArray =
                decrypt(state.cvv.encrypted.toEncryptedByteArray())
            when {
                decryptedByteArray.isEmpty() -> state.copy(
                    cvv = UIHiddenState.Empty(state.cvv.encrypted)
                )

                isFocused -> state.copy(
                    cvv = UIHiddenState.Revealed(
                        encrypted = creditCardFormItemState.cvv.encrypted,
                        clearText = decryptedByteArray.decodeToString()
                    )
                )

                else -> state.copy(cvv = UIHiddenState.Concealed(state.cvv.encrypted))
            }
        }
    }

    fun onPinFocusChanged(isFocused: Boolean) {
        val state = creditCardFormItemState
        creditCardFormItemState = encryptionContextProvider.withEncryptionContext {
            val decryptedByteArray = decrypt(state.pin.encrypted.toEncryptedByteArray())
            when {
                decryptedByteArray.isEmpty() -> state.copy(pin = UIHiddenState.Empty(state.pin.encrypted))
                isFocused -> state.copy(
                    pin = UIHiddenState.Revealed(
                        encrypted = state.pin.encrypted,
                        clearText = decryptedByteArray.decodeToString()
                    )
                )

                else -> state.copy(pin = UIHiddenState.Concealed(state.pin.encrypted))
            }
        }
    }

    companion object {
        @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
        const val CVV_MAX_LENGTH = 4

        @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
        const val PIN_MAX_LENGTH = 12
    }
}
