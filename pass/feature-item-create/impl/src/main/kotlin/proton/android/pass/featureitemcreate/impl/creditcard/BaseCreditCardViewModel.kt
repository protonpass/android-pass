package proton.android.pass.featureitemcreate.impl.creditcard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState.NotLoading
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.toEncryptedByteArray
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.pass.domain.HiddenState
import proton.pass.domain.ItemContents

abstract class BaseCreditCardViewModel(
    private val encryptionContextProvider: EncryptionContextProvider
) : ViewModel() {

    protected val isLoadingState: MutableStateFlow<IsLoadingState> = MutableStateFlow(NotLoading)
    private val hasUserEditedContentState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val creditCardValidationErrorsState: MutableStateFlow<Set<CreditCardValidationErrors>> =
        MutableStateFlow(emptySet())
    protected val isItemSavedState: MutableStateFlow<ItemSavedState> =
        MutableStateFlow(ItemSavedState.Unknown)
    protected val itemContentState: MutableStateFlow<ItemContents.CreditCard> =
        MutableStateFlow(
            encryptionContextProvider.withEncryptionContext {
                ItemContents.CreditCard.default(
                    cvv = HiddenState.Empty(encrypt("")),
                    pin = HiddenState.Empty(encrypt(""))
                )
            }
        )

    val baseState: StateFlow<BaseCreditCardUiState> = combine(
        isLoadingState,
        hasUserEditedContentState,
        creditCardValidationErrorsState,
        isItemSavedState,
        itemContentState
    ) { isLoading, hasUserEditedContent, creditCardValidationErrors, isItemSaved, itemContent ->
        BaseCreditCardUiState(
            isLoading = isLoading.value(),
            hasUserEditedContent = hasUserEditedContent,
            validationErrors = creditCardValidationErrors.toPersistentSet(),
            isItemSaved = isItemSaved,
            contents = itemContent
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = encryptionContextProvider.withEncryptionContext {
                BaseCreditCardUiState.default(
                    cvv = HiddenState.Empty(encrypt("")),
                    pin = HiddenState.Empty(encrypt(""))
                )
            }
        )

    fun onTitleChange(value: String) {
        onUserEditedContent()
        itemContentState.update { itemContentState.value.copy(title = value) }
        creditCardValidationErrorsState.update {
            it.toMutableSet().apply { remove(CreditCardValidationErrors.BlankTitle) }
        }
    }

    fun onNameChanged(value: String) {
        onUserEditedContent()
        itemContentState.update { itemContentState.value.copy(cardHolder = value) }
    }

    fun onNumberChanged(value: String) {
        val sanitisedValue = value.replace(nonDigitRegex, "").take(19)
        onUserEditedContent()
        itemContentState.update { itemContentState.value.copy(number = sanitisedValue) }
    }

    fun onCVVChanged(value: String) {
        val sanitisedValue = value.replace(nonDigitRegex, "").take(4)
        onUserEditedContent()
        encryptionContextProvider.withEncryptionContext {
            itemContentState.update {
                if (sanitisedValue.isNotBlank()) {
                    it.copy(cvv = HiddenState.Revealed(encrypt(sanitisedValue), sanitisedValue))
                } else {
                    it.copy(cvv = HiddenState.Empty(encrypt(sanitisedValue)))
                }
            }
        }
    }

    fun onExpirationDateChanged(value: String) {
        val sanitisedValue = value.replace(nonDigitRegex, "").take(6)
        onUserEditedContent()
        itemContentState.update { itemContentState.value.copy(expirationDate = sanitisedValue) }
    }

    fun onNoteChanged(value: String) {
        onUserEditedContent()
        itemContentState.update { itemContentState.value.copy(note = value) }
    }

    protected fun validateItem(): Boolean {
        val validationErrors = itemContentState.value.validate()
        if (validationErrors.isNotEmpty()) {
            creditCardValidationErrorsState.update { validationErrors }
            return false
        }
        return true
    }

    protected fun onUserEditedContent() {
        if (hasUserEditedContentState.value) return
        hasUserEditedContentState.update { true }
    }

    fun onCVVFocusChanged(isFocused: Boolean) {
        encryptionContextProvider.withEncryptionContext {
            itemContentState.update {
                val decryptedByteArray = decrypt(it.cvv.encrypted.toEncryptedByteArray())
                when {
                    decryptedByteArray.isEmpty() -> it.copy(cvv = HiddenState.Empty(it.cvv.encrypted))
                    isFocused -> it.copy(
                        cvv = HiddenState.Revealed(
                            encrypted = it.cvv.encrypted,
                            clearText = decryptedByteArray.decodeToString()
                        )
                    )

                    else -> it.copy(cvv = HiddenState.Concealed(it.cvv.encrypted))
                }
            }
        }
    }

    companion object {
        val nonDigitRegex: Regex = "\\D".toRegex()
    }
}
