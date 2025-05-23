package proton.android.pass.features.itemcreate.creditcard

import android.content.Context
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.CommonRegex.NON_DIGIT_REGEX
import proton.android.pass.common.api.combineN
import proton.android.pass.commonpresentation.api.attachments.AttachmentsHandler
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.toEncryptedByteArray
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.FileMetadata
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.preferences.DisplayFileAttachmentsBanner.NotDisplay
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import java.net.URI

abstract class BaseCreditCardViewModel(
    private val encryptionContextProvider: EncryptionContextProvider,
    private val attachmentsHandler: AttachmentsHandler,
    private val featureFlagsRepository: FeatureFlagsPreferencesRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    canPerformPaidAction: CanPerformPaidAction,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val hasUserEditedContentState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    protected val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)

    init {
        attachmentsHandler.observeNewAttachments {
            onUserEditedContent()
            viewModelScope.launch {
                isLoadingState.update { IsLoadingState.Loading }
                attachmentsHandler.uploadNewAttachment(it.metadata)
                isLoadingState.update { IsLoadingState.NotLoading }
            }
        }.launchIn(viewModelScope)
        attachmentsHandler.observeHasDeletedAttachments {
            onUserEditedContent()
        }.launchIn(viewModelScope)
        attachmentsHandler.observeHasRenamedAttachments {
            onUserEditedContent()
        }.launchIn(viewModelScope)
    }

    private val validationErrorsState: MutableStateFlow<Set<CreditCardValidationErrors>> =
        MutableStateFlow(emptySet())
    protected val isItemSavedState: MutableStateFlow<ItemSavedState> =
        MutableStateFlow(ItemSavedState.Unknown)

    @OptIn(SavedStateHandleSaveableApi::class)
    protected var creditCardItemFormMutableState: CreditCardItemFormState by savedStateHandleProvider.get()
        .saveable {
            mutableStateOf(
                encryptionContextProvider.withEncryptionContext {
                    CreditCardItemFormState.default(this)
                }
            )
        }
    val creditCardItemFormState: CreditCardItemFormState get() = creditCardItemFormMutableState

    val baseState: StateFlow<BaseCreditCardUiState> = combineN(
        isLoadingState,
        hasUserEditedContentState,
        validationErrorsState,
        isItemSavedState,
        canPerformPaidAction(),
        featureFlagsRepository.get<Boolean>(FeatureFlag.FILE_ATTACHMENTS_V1),
        userPreferencesRepository.observeDisplayFileAttachmentsOnboarding(),
        attachmentsHandler.attachmentState
    ) { isLoading, hasUserEditedContent, validationErrors, isItemSaved, canPerformPaidAction,
        isFileAttachmentsEnabled, displayFileAttachmentsOnboarding, attachmentsState ->
        BaseCreditCardUiState(
            isLoading = isLoading.value(),
            hasUserEditedContent = hasUserEditedContent,
            validationErrors = validationErrors.toPersistentSet(),
            isItemSaved = isItemSaved,
            isDowngradedMode = !canPerformPaidAction,
            displayFileAttachmentsOnboarding = displayFileAttachmentsOnboarding.value(),
            isFileAttachmentsEnabled = isFileAttachmentsEnabled,
            attachmentsState = attachmentsState
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BaseCreditCardUiState.Initial
        )

    fun onTitleChange(value: String) {
        onUserEditedContent()
        creditCardItemFormMutableState = creditCardItemFormMutableState.copy(title = value)
        validationErrorsState.update {
            it.toMutableSet().apply { remove(CreditCardValidationErrors.BlankTitle) }
        }
    }

    fun onNameChanged(value: String) {
        onUserEditedContent()
        creditCardItemFormMutableState = creditCardItemFormMutableState.copy(cardHolder = value)
    }

    fun onNumberChanged(value: String) {
        val sanitisedValue = value.replace(NON_DIGIT_REGEX, "").take(19)
        onUserEditedContent()
        creditCardItemFormMutableState =
            creditCardItemFormMutableState.copy(number = sanitisedValue)
    }

    fun onCVVChanged(value: String) {
        val sanitisedValue = value.replace(NON_DIGIT_REGEX, "").take(CVV_MAX_LENGTH)
        onUserEditedContent()
        creditCardItemFormMutableState = encryptionContextProvider.withEncryptionContext {
            if (sanitisedValue.isNotBlank()) {
                creditCardItemFormMutableState.copy(
                    cvv = UIHiddenState.Revealed(
                        encrypt(sanitisedValue),
                        sanitisedValue
                    )
                )
            } else {
                creditCardItemFormMutableState.copy(cvv = UIHiddenState.Empty(encrypt(sanitisedValue)))
            }
        }
    }

    fun onPinChanged(value: String) {
        val sanitisedValue = value.replace(NON_DIGIT_REGEX, "").take(PIN_MAX_LENGTH)
        onUserEditedContent()
        creditCardItemFormMutableState = encryptionContextProvider.withEncryptionContext {
            if (sanitisedValue.isNotBlank()) {
                creditCardItemFormMutableState.copy(
                    pin = UIHiddenState.Revealed(
                        encrypt(sanitisedValue),
                        sanitisedValue
                    )
                )
            } else {
                creditCardItemFormMutableState.copy(pin = UIHiddenState.Empty(encrypt(sanitisedValue)))
            }
        }
    }

    fun onExpirationDateChanged(value: String) {
        val sanitisedValue = value.replace(NON_DIGIT_REGEX, "").take(EXPIRATION_DATE_MAX_LENGTH)
        onUserEditedContent()
        creditCardItemFormMutableState =
            creditCardItemFormMutableState.copy(expirationDate = sanitisedValue)
        validationErrorsState.update {
            it.toMutableSet().apply { remove(CreditCardValidationErrors.InvalidExpirationDate) }
        }
    }

    fun onNoteChanged(value: String) {
        onUserEditedContent()
        creditCardItemFormMutableState = creditCardItemFormMutableState.copy(note = value)
    }

    protected fun validateItem(): Boolean {
        val validationErrors = creditCardItemFormMutableState.validate()
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
        val state = creditCardItemFormMutableState
        creditCardItemFormMutableState = encryptionContextProvider.withEncryptionContext {
            val decryptedByteArray =
                decrypt(state.cvv.encrypted.toEncryptedByteArray())
            when {
                decryptedByteArray.isEmpty() -> state.copy(
                    cvv = UIHiddenState.Empty(state.cvv.encrypted)
                )

                isFocused -> state.copy(
                    cvv = UIHiddenState.Revealed(
                        encrypted = creditCardItemFormMutableState.cvv.encrypted,
                        clearText = decryptedByteArray.decodeToString()
                    )
                )

                else -> state.copy(cvv = UIHiddenState.Concealed(state.cvv.encrypted))
            }
        }
    }

    fun onPinFocusChanged(isFocused: Boolean) {
        val state = creditCardItemFormMutableState
        creditCardItemFormMutableState = encryptionContextProvider.withEncryptionContext {
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

    internal fun clearDraftData() {
        attachmentsHandler.onClearAttachments()
    }

    fun openDraftAttachment(
        contextHolder: ClassHolder<Context>,
        uri: URI,
        mimetype: String
    ) {
        attachmentsHandler.openDraftAttachment(contextHolder, uri, mimetype)
    }

    fun openAttachment(contextHolder: ClassHolder<Context>, attachment: Attachment) {
        viewModelScope.launch {
            attachmentsHandler.openAttachment(
                contextHolder = contextHolder,
                attachment = attachment
            )
        }
    }

    suspend fun isFileAttachmentsEnabled() = featureFlagsRepository.get<Boolean>(FeatureFlag.FILE_ATTACHMENTS_V1)
        .firstOrNull()
        ?: false

    fun retryUploadDraftAttachment(metadata: FileMetadata) {
        viewModelScope.launch {
            isLoadingState.update { IsLoadingState.Loading }
            attachmentsHandler.uploadNewAttachment(metadata)
            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    fun dismissFileAttachmentsOnboardingBanner() {
        viewModelScope.launch {
            userPreferencesRepository.setDisplayFileAttachmentsOnboarding(NotDisplay)
        }
    }

    companion object {
        @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
        const val CVV_MAX_LENGTH = 4

        @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
        const val PIN_MAX_LENGTH = 12

        @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
        const val EXPIRATION_DATE_MAX_LENGTH = 4
    }
}
