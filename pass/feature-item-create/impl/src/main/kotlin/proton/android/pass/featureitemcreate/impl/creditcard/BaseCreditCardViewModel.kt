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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.CommonRegex.NON_DIGIT_REGEX
import proton.android.pass.common.api.combineN
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonuimodels.api.attachments.AttachmentsState
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState.NotLoading
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.toEncryptedByteArray
import proton.android.pass.data.api.repositories.DraftAttachmentRepository
import proton.android.pass.data.api.repositories.MetadataResolver
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.attachments.ClearAttachments
import proton.android.pass.data.api.usecases.attachments.UploadAttachment
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.common.UIHiddenState
import proton.android.pass.log.api.PassLogger
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import java.net.URI

abstract class BaseCreditCardViewModel(
    private val encryptionContextProvider: EncryptionContextProvider,
    private val uploadAttachment: UploadAttachment,
    private val clearAttachments: ClearAttachments,
    draftAttachmentRepository: DraftAttachmentRepository,
    metadataResolver: MetadataResolver,
    canPerformPaidAction: CanPerformPaidAction,
    featureFlagsRepository: FeatureFlagsPreferencesRepository,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val hasUserEditedContentState: MutableStateFlow<Boolean> = MutableStateFlow(false)

    init {
        draftAttachmentRepository.observeNew()
            .onEach { newUris: Set<URI> ->
                if (newUris.isEmpty()) return@onEach
                onUserEditedContent()
                newUris.forEach(::uploadNewAttachment)
            }
            .launchIn(viewModelScope)
    }

    protected val isLoadingState: MutableStateFlow<IsLoadingState> = MutableStateFlow(NotLoading)
    private val validationErrorsState: MutableStateFlow<Set<CreditCardValidationErrors>> =
        MutableStateFlow(emptySet())
    protected val isItemSavedState: MutableStateFlow<ItemSavedState> =
        MutableStateFlow(ItemSavedState.Unknown)
    private val isUploadingAttachment: MutableStateFlow<Set<URI>> = MutableStateFlow(emptySet())

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

    private val draftAttachments = draftAttachmentRepository.observeAll()
        .map { uris -> uris.mapNotNull { metadataResolver.extractMetadata(it) } }

    private val attachmentsFlow = combine(
        isUploadingAttachment,
        draftAttachments
    ) { loadingAttachments, draftAttachmentsList ->
        AttachmentsState(
            loadingDraftAttachments = loadingAttachments,
            draftAttachmentsList = draftAttachmentsList,
            attachmentsList = emptyList(),
            loadingAttachments = emptySet()
        )
    }

    val baseState: StateFlow<BaseCreditCardUiState> = combineN(
        isLoadingState,
        hasUserEditedContentState,
        validationErrorsState,
        isItemSavedState,
        canPerformPaidAction(),
        featureFlagsRepository.get<Boolean>(FeatureFlag.FILE_ATTACHMENTS_V1),
        attachmentsFlow
    ) { isLoading, hasUserEditedContent, validationErrors, isItemSaved, canPerformPaidAction,
        isFileAttachmentsEnabled, attachmentsState ->
        BaseCreditCardUiState(
            isLoading = isLoading.value(),
            hasUserEditedContent = hasUserEditedContent,
            validationErrors = validationErrors.toPersistentSet(),
            isItemSaved = isItemSaved,
            isDowngradedMode = !canPerformPaidAction,
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

    private fun uploadNewAttachment(uri: URI) {
        isUploadingAttachment.update { it + uri }
        viewModelScope.launch {
            runCatching { uploadAttachment(uri) }
                .onFailure {
                    PassLogger.w(TAG, "Could not upload attachment: $uri")
                    PassLogger.w(TAG, it)
                }
        }
        isUploadingAttachment.update { it - uri }
    }

    override fun onCleared() {
        clearAttachments()
        super.onCleared()
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
