package proton.android.pass.features.itemcreate.creditcard

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.CommonRegex.NON_DIGIT_REGEX
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.some
import proton.android.pass.commonpresentation.api.attachments.AttachmentsHandler
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.toEncryptedByteArray
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.FileMetadata
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.common.CommonFieldValidationError
import proton.android.pass.features.itemcreate.common.CreditCardItemValidationError
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepository
import proton.android.pass.features.itemcreate.common.CustomFieldValidationError
import proton.android.pass.features.itemcreate.common.DraftFormFieldEvent
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.itemcreate.common.ValidationError
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldHandler
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldIdentifier
import proton.android.pass.features.itemcreate.common.formprocessor.CreditCardFormProcessorType
import proton.android.pass.features.itemcreate.common.formprocessor.CreditCardItemFormProcessor
import proton.android.pass.features.itemcreate.common.formprocessor.FormProcessingResult
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.DisplayFileAttachmentsBanner.NotDisplay
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import java.net.URI

abstract class BaseCreditCardViewModel(
    private val encryptionContextProvider: EncryptionContextProvider,
    private val attachmentsHandler: AttachmentsHandler,
    private val userPreferencesRepository: UserPreferencesRepository,
    protected val customFieldHandler: CustomFieldHandler,
    private val creditCardItemFormProcessor: CreditCardFormProcessorType,
    private val clipboardManager: ClipboardManager,
    customFieldDraftRepository: CustomFieldDraftRepository,
    canPerformPaidAction: CanPerformPaidAction,
    savedStateHandleProvider: SavedStateHandleProvider,
    featureFlagsPreferencesRepository: FeatureFlagsPreferencesRepository
) : ViewModel() {

    private val hasUserEditedContentState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    protected val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    private val focusedFieldState: MutableStateFlow<Option<CreditCardField>> =
        MutableStateFlow<Option<CreditCardField>>(None)

    init {
        customFieldDraftRepository.observeCustomFieldEvents()
            .onEach {
                onUserEditedContent()
                when (it) {
                    is DraftFormFieldEvent.FieldAdded -> onFieldAdded(it)
                    is DraftFormFieldEvent.FieldRemoved -> onFieldRemoved(it)
                    is DraftFormFieldEvent.FieldRenamed -> onFieldRenamed(it)
                }
            }
            .launchIn(viewModelScope)
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

    private val validationErrorsState: MutableStateFlow<Set<ValidationError>> =
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

    internal val baseState: StateFlow<BaseCreditCardUiState> = combineN(
        isLoadingState,
        hasUserEditedContentState,
        validationErrorsState,
        isItemSavedState,
        canPerformPaidAction(),
        userPreferencesRepository.observeDisplayFileAttachmentsOnboarding(),
        attachmentsHandler.attachmentState,
        focusedFieldState,
        featureFlagsPreferencesRepository.get<Boolean>(FeatureFlag.PASS_ALLOW_CREDIT_CARD_FREE_USERS)
    ) { isLoading, hasUserEditedContent, validationErrors, isItemSaved, canPerformPaidAction,
        displayFileAttachmentsOnboarding, attachmentsState, focusedField, allowCreditCreditFreeUsers ->
        BaseCreditCardUiState(
            isLoading = isLoading.value(),
            hasUserEditedContent = hasUserEditedContent,
            validationErrors = validationErrors.toPersistentSet(),
            isItemSaved = isItemSaved,
            canPerformPaidAction = canPerformPaidAction,
            displayFileAttachmentsOnboarding = displayFileAttachmentsOnboarding.value(),
            attachmentsState = attachmentsState,
            focusedField = focusedField,
            allowCreditCreditFreeUsers = allowCreditCreditFreeUsers
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
            it.toMutableSet().apply { remove(CommonFieldValidationError.BlankTitle) }
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
            it.toMutableSet().apply { remove(CreditCardItemValidationError.InvalidExpirationDate) }
        }
    }

    fun onNoteChanged(value: String) {
        onUserEditedContent()
        creditCardItemFormMutableState = creditCardItemFormMutableState.copy(note = value)
    }

    protected suspend fun isFormStateValid(originalCustomFields: List<UICustomFieldContent> = emptyList()): Boolean {
        val result = encryptionContextProvider.withEncryptionContextSuspendable {
            creditCardItemFormProcessor.process(
                CreditCardItemFormProcessor.Input(
                    formState = creditCardItemFormState,
                    originalCustomFields = originalCustomFields
                ),
                ::decrypt,
                ::encrypt
            )
        }
        return when (result) {
            is FormProcessingResult.Error -> {
                validationErrorsState.update { result.errors }
                false
            }

            is FormProcessingResult.Success -> {
                creditCardItemFormMutableState = result.sanitized
                true
            }
        }
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

    private fun onFieldRemoved(event: DraftFormFieldEvent.FieldRemoved) {
        val (_, index) = event
        creditCardItemFormMutableState = creditCardItemFormState.copy(
            customFields = creditCardItemFormState.customFields
                .toMutableList()
                .apply { removeAt(index) }
                .toPersistentList()
        )
    }

    private fun onFieldRenamed(event: DraftFormFieldEvent.FieldRenamed) {
        val (_, index, newLabel) = event
        val updated = customFieldHandler.onCustomFieldRenamed(
            customFieldList = creditCardItemFormState.customFields,
            index = index,
            newLabel = newLabel
        )
        creditCardItemFormMutableState = creditCardItemFormState.copy(customFields = updated)
    }

    private fun onFieldAdded(event: DraftFormFieldEvent.FieldAdded) {
        val (_, label, type) = event
        val added = customFieldHandler.onCustomFieldAdded(label, type)
        creditCardItemFormMutableState = creditCardItemFormState.copy(
            customFields = creditCardItemFormState.customFields + added
        )
        val identifier = CustomFieldIdentifier(
            index = creditCardItemFormState.customFields.lastIndex,
            type = type
        )
        focusedFieldState.update { CreditCardField.CustomField(identifier).some() }
    }

    private fun removeValidationErrors(vararg errors: ValidationError) {
        validationErrorsState.update { currentValidationErrors ->
            currentValidationErrors.toMutableSet().apply {
                errors.forEach { error -> remove(error) }
            }
        }
    }

    internal fun onCustomFieldChange(id: CustomFieldIdentifier, value: String) {
        removeValidationErrors(CustomFieldValidationError.InvalidTotp(index = id.index))
        val updated = customFieldHandler.onCustomFieldValueChanged(
            customFieldIdentifier = id,
            customFieldList = creditCardItemFormState.customFields,
            value = value
        )
        creditCardItemFormMutableState = creditCardItemFormState.copy(
            customFields = updated.toPersistentList()
        )
    }

    internal fun onFocusChange(field: CreditCardField.CustomField, isFocused: Boolean) {
        val customFields = customFieldHandler.onCustomFieldFocusedChanged(
            customFieldIdentifier = field.field,
            customFieldList = creditCardItemFormState.customFields,
            isFocused = isFocused
        )
        creditCardItemFormMutableState = creditCardItemFormState.copy(customFields = customFields)
        if (isFocused) {
            focusedFieldState.update { field.some() }
        } else {
            focusedFieldState.update { None }
        }
    }

    fun onPasteTotp() {
        viewModelScope.launch(Dispatchers.IO) {
            onUserEditedContent()
            clipboardManager.getClipboardContent()
                .onSuccess { clipboardContent ->
                    withContext(Dispatchers.Main) {
                        when (val field = focusedFieldState.value.value()) {
                            is CreditCardField.CustomField -> {
                                val sanitisedContent = clipboardContent
                                    .replace(" ", "")
                                    .replace("\n", "")
                                val updated = customFieldHandler.onCustomFieldValueChanged(
                                    customFieldIdentifier = field.field,
                                    customFieldList = creditCardItemFormState.customFields,
                                    value = sanitisedContent
                                )
                                creditCardItemFormMutableState = creditCardItemFormState.copy(
                                    customFields = updated
                                )
                            }

                            else -> {}
                        }
                    }
                }
                .onFailure { PassLogger.d(TAG, it, "Failed on getting clipboard content") }
        }
    }

    fun setTotp(navTotpUri: String, navTotpIndex: Int) {
        onUserEditedContent()
        val identifier = CustomFieldIdentifier(
            index = navTotpIndex,
            type = CustomFieldType.Totp
        )
        val updated = customFieldHandler.onCustomFieldValueChanged(
            customFieldIdentifier = identifier,
            customFieldList = creditCardItemFormState.customFields,
            value = navTotpUri
        )
        creditCardItemFormMutableState = creditCardItemFormState.copy(
            customFields = updated
        )
    }

    companion object {
        @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
        const val CVV_MAX_LENGTH = 4

        @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
        const val PIN_MAX_LENGTH = 12

        @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
        const val EXPIRATION_DATE_MAX_LENGTH = 4

        private const val TAG = "BaseCreditCardViewModel"
    }
}
