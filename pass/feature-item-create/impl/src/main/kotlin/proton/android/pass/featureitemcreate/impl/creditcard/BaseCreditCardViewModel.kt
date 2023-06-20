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
        onUserEditedContent()
        itemContentState.update { itemContentState.value.copy(number = value) }
    }

    fun onCVVChanged(value: String) {
        onUserEditedContent()
        encryptionContextProvider.withEncryptionContext {
            itemContentState.update {
                it.copy(cvv = HiddenState.Revealed(encrypt(value), value))
            }
        }
    }

    fun onExpirationDateChanged(value: String) {
        onUserEditedContent()
        itemContentState.update { itemContentState.value.copy(expirationDate = value) }
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
}
