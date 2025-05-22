/*
 * Copyright (c) 2023 Proton AG
 * This file is part of Proton AG and Proton Pass.
 *
 * Proton Pass is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Proton Pass is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Proton Pass.  If not, see <https://www.gnu.org/licenses/>.
 */

package proton.android.pass.features.itemcreate.login

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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonpresentation.api.attachments.AttachmentsHandler
import proton.android.pass.commonrust.api.EmailValidator
import proton.android.pass.commonrust.api.passwords.strengths.PasswordStrengthCalculator
import proton.android.pass.commonui.api.ClassHolder
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.toEncryptedByteArray
import proton.android.pass.data.api.repositories.DRAFT_PASSWORD_KEY
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.url.UrlSanitizer
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.data.api.usecases.tooltips.DisableTooltip
import proton.android.pass.data.api.usecases.tooltips.ObserveTooltipEnabled
import proton.android.pass.domain.CustomFieldType
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.attachments.Attachment
import proton.android.pass.domain.attachments.FileMetadata
import proton.android.pass.domain.tooltips.Tooltip
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.OpenScanState
import proton.android.pass.features.itemcreate.alias.AliasItemFormState
import proton.android.pass.features.itemcreate.alias.CreateAliasViewModel
import proton.android.pass.features.itemcreate.common.CommonFieldValidationError
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepository
import proton.android.pass.features.itemcreate.common.CustomFieldValidationError
import proton.android.pass.features.itemcreate.common.DraftFormFieldEvent
import proton.android.pass.features.itemcreate.common.LoginItemValidationError
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UICustomFieldContent.Companion.createCustomField
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.itemcreate.common.ValidationError
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldIdentifier
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.DisplayFileAttachmentsBanner.NotDisplay
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.preferences.value
import proton.android.pass.totp.api.TotpManager
import java.net.URI

@Suppress("TooManyFunctions", "LargeClass", "LongParameterList")
abstract class BaseLoginViewModel(
    protected val accountManager: AccountManager,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val clipboardManager: ClipboardManager,
    private val totpManager: TotpManager,
    private val draftRepository: DraftRepository,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val passwordStrengthCalculator: PasswordStrengthCalculator,
    protected val emailValidator: EmailValidator,
    private val disableTooltip: DisableTooltip,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val attachmentsHandler: AttachmentsHandler,
    private val featureFlagsRepository: FeatureFlagsPreferencesRepository,
    private val customFieldDraftRepository: CustomFieldDraftRepository,
    observeCurrentUser: ObserveCurrentUser,
    observeUpgradeInfo: ObserveUpgradeInfo,
    observeTooltipEnabled: ObserveTooltipEnabled,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val hasUserEditedContentFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    protected val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)

    @OptIn(SavedStateHandleSaveableApi::class)
    protected var loginItemFormMutableState: LoginItemFormState by savedStateHandleProvider.get()
        .saveable {
            mutableStateOf(
                encryptionContextProvider.withEncryptionContext {
                    LoginItemFormState.default(this)
                }
            )
        }

    internal val loginItemFormState: LoginItemFormState
        get() = loginItemFormMutableState

    protected val aliasLocalItemState: MutableStateFlow<Option<AliasItemFormState>> =
        MutableStateFlow(None)
    private val aliasDraftState: Flow<Option<AliasItemFormState>> = draftRepository
        .get(CreateAliasViewModel.KEY_DRAFT_ALIAS)
    private val focusedFieldFlow: MutableStateFlow<Option<LoginField>> = MutableStateFlow(None)

    init {
        viewModelScope.launch {
            launch { observeGeneratedPassword() }
            launch { observeCustomField() }
            launch { observeDisplayUsernameFieldPreference() }
        }
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

    private val aliasItemFormState: Flow<Option<AliasItemFormState>> = combine(
        aliasLocalItemState,
        aliasDraftState.onStart { emit(None) }
    ) { aliasItem, aliasDraft ->
        when (aliasDraft) {
            is Some -> {
                onAliasCreated(aliasDraft.value)
                aliasDraft
            }

            None -> aliasItem
        }
    }

    protected val isItemSavedState: MutableStateFlow<ItemSavedState> =
        MutableStateFlow(ItemSavedState.Unknown)
    private val openScanState: MutableStateFlow<OpenScanState> =
        MutableStateFlow(OpenScanState.Unknown)

    private val eventsFlow: Flow<Events> = combine(isItemSavedState, openScanState, ::Events)

    private data class Events(
        val itemSavedState: ItemSavedState,
        val openScanState: OpenScanState
    )

    private val loginItemValidationErrorsState: MutableStateFlow<Set<ValidationError>> =
        MutableStateFlow(emptySet())
    private val focusLastWebsiteState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    protected val canUpdateUsernameState: MutableStateFlow<Boolean> = MutableStateFlow(true)

    private val upgradeInfoFlow: Flow<UpgradeInfo> = observeUpgradeInfo().distinctUntilChanged()

    protected val itemHadTotpState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    private val totpUiStateFlow = combine(
        itemHadTotpState,
        upgradeInfoFlow.asLoadingResult()
    ) { itemHadTotp, upgradeInfoResult ->
        when (upgradeInfoResult) {
            is LoadingResult.Error -> TotpUiState.Error
            LoadingResult.Loading -> TotpUiState.Loading
            is LoadingResult.Success -> if (upgradeInfoResult.data.hasReachedTotpLimit()) {
                TotpUiState.Limited(itemHadTotp)
            } else {
                TotpUiState.Success
            }
        }
    }

    private val userInteractionFlow: Flow<UserInteractionWrapper> = combine(
        canUpdateUsernameState,
        focusLastWebsiteState,
        focusedFieldFlow,
        hasUserEditedContentFlow,
        eventsFlow,
        ::UserInteractionWrapper
    )

    private data class UserInteractionWrapper(
        val canUpdateUsername: Boolean,
        val focusLastWebsite: Boolean,
        val focusedField: Option<LoginField>,
        val hasUserEditedContent: Boolean,
        val events: Events
    )

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    internal val baseLoginUiState: StateFlow<BaseLoginUiState> = combineN(
        loginItemValidationErrorsState,
        observeCurrentUser().map { it.email },
        aliasItemFormState,
        isLoadingState,
        totpUiStateFlow,
        upgradeInfoFlow.asLoadingResult(),
        userInteractionFlow,
        observeTooltipEnabled(Tooltip.UsernameSplit),
        featureFlagsRepository.get<Boolean>(FeatureFlag.FILE_ATTACHMENTS_V1),
        userPreferencesRepository.observeDisplayFileAttachmentsOnboarding(),
        attachmentsHandler.attachmentState
    ) { loginItemValidationErrors, primaryEmail, aliasItemFormState, isLoading, totpUiState,
        upgradeInfoResult, userInteraction, isUsernameSplitTooltipEnabled,
        isFileAttachmentsEnabled, displayFileAttachmentsOnboarding, attachmentsState ->
        val userPlan = upgradeInfoResult.getOrNull()?.plan
        BaseLoginUiState(
            validationErrors = loginItemValidationErrors.toPersistentSet(),
            isLoadingState = isLoading,
            isItemSaved = userInteraction.events.itemSavedState,
            openScanState = userInteraction.events.openScanState,
            canUseCustomFields = userPlan?.isPaidPlan == true || userPlan?.isTrialPlan == true,
            focusLastWebsite = userInteraction.focusLastWebsite,
            canUpdateUsername = userInteraction.canUpdateUsername,
            primaryEmail = primaryEmail,
            aliasItemFormState = aliasItemFormState.value(),
            hasUserEditedContent = userInteraction.hasUserEditedContent,
            hasReachedAliasLimit = upgradeInfoResult.getOrNull()?.hasReachedAliasLimit() ?: false,
            totpUiState = totpUiState,
            focusedField = userInteraction.focusedField.value(),
            isUsernameSplitTooltipEnabled = isUsernameSplitTooltipEnabled,
            isFileAttachmentsEnabled = isFileAttachmentsEnabled,
            displayFileAttachmentsOnboarding = displayFileAttachmentsOnboarding.value(),
            attachmentsState = attachmentsState
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BaseLoginUiState.Initial
        )

    internal fun onTitleChange(value: String) {
        onUserEditedContent()
        loginItemFormMutableState = loginItemFormMutableState.copy(title = value)
        removeValidationErrors(CommonFieldValidationError.BlankTitle)

        val aliasItem = aliasLocalItemState.value
        if (aliasItem is Some) {
            aliasLocalItemState.update { aliasItem.value.copy(title = value).toOption() }
        }
    }

    internal fun onEmailChanged(newEmail: String) {
        onUserEditedContent()

        val trimmedNewEmail = newEmail.trim()

        when {
            loginItemFormState.isExpanded -> loginItemFormState.copy(
                email = trimmedNewEmail
            )

            emailValidator.isValid(trimmedNewEmail) -> loginItemFormState.copy(
                email = trimmedNewEmail,
                username = ""
            )

            else -> loginItemFormState.copy(
                email = "",
                username = newEmail.trimStart()
            )
        }.also { updatedLoginItemFormState ->
            loginItemFormMutableState = updatedLoginItemFormState
        }
    }

    internal fun onUsernameChanged(newUsername: String) {
        onUserEditedContent()

        loginItemFormMutableState = loginItemFormMutableState.copy(
            username = newUsername.trimStart()
        )
    }

    internal fun onPasswordChange(newPasswordValue: String) {
        onUserEditedContent()
        loginItemFormMutableState = encryptionContextProvider.withEncryptionContext {
            loginItemFormMutableState.copy(
                password = UIHiddenState.Revealed(
                    encrypted = encrypt(newPasswordValue),
                    clearText = newPasswordValue
                ),
                passwordStrength = passwordStrengthCalculator.calculateStrength(newPasswordValue)
            )
        }
    }

    internal fun onTotpChange(value: String) {
        onUserEditedContent()
        val newValue = totpManager.sanitiseToEdit(value).getOrNull() ?: value
        loginItemFormMutableState = encryptionContextProvider.withEncryptionContext {
            loginItemFormMutableState.copy(
                primaryTotp = UIHiddenState.Revealed(encrypt(newValue), newValue)
            )
        }
        removeValidationErrors(LoginItemValidationError.InvalidTotp)
    }

    internal fun onWebsiteChange(value: String, index: Int) {
        onUserEditedContent()
        val newValue = value.replace(" ", "").replace("\n", "")
        loginItemFormMutableState = loginItemFormState.copy(
            urls = loginItemFormState.urls.toMutableList()
                .apply {
                    if (index < this.size) {
                        this[index] = newValue
                    }
                }
        )
        removeValidationErrors(LoginItemValidationError.InvalidUrl(index))
        focusLastWebsiteState.update { false }
    }

    internal fun onAddWebsite() {
        onUserEditedContent()
        loginItemFormMutableState =
            loginItemFormState.copy(urls = sanitizeWebsites(loginItemFormState.urls) + "")
        focusLastWebsiteState.update { true }
    }

    internal fun onRemoveWebsite(index: Int) {
        onUserEditedContent()

        if (index < loginItemFormState.urls.size) {
            loginItemFormMutableState = loginItemFormState.copy(
                urls = loginItemFormState.urls.toMutableList()
                    .apply { removeAt(index) }
            )
            removeValidationErrors(LoginItemValidationError.InvalidUrl(index))
        }

        focusLastWebsiteState.update { false }
    }

    internal fun onNoteChange(value: String) {
        onUserEditedContent()
        loginItemFormMutableState = loginItemFormMutableState.copy(note = value)
    }

    internal fun onEmitSnackbarMessage(snackbarMessage: LoginSnackbarMessages) = viewModelScope.launch {
        snackbarDispatcher(snackbarMessage)
    }

    internal fun onAliasCreated(aliasItemFormState: AliasItemFormState) {
        onUserEditedContent()
        aliasLocalItemState.update { aliasItemFormState.toOption() }

        aliasItemFormState.aliasToBeCreated?.let { alias ->
            loginItemFormMutableState = loginItemFormMutableState.copy(email = alias)

            canUpdateUsernameState.update { false }
        }
    }

    internal fun clearDraftData() {
        draftRepository.delete<AliasItemFormState>(CreateAliasViewModel.KEY_DRAFT_ALIAS)
        attachmentsHandler.onClearAttachments()
    }

    @Suppress("ReturnCount")
    protected suspend fun validateItem(
        oldItem: Option<Item>,
        originalTotpCustomFields: List<UICustomFieldContent.Totp>
    ): Boolean {
        val websites = sanitizeWebsites(loginItemFormState.urls)
        val itemType = oldItem.map { it.itemType as ItemType.Login }
        val (originalTotpUri, editedTotpUri) = encryptionContextProvider.withEncryptionContext {
            itemType.map { decrypt(it.primaryTotp) }.value().orEmpty() to
                decrypt(loginItemFormState.primaryTotp.encrypted)
        }

        val sanitisedUri = totpManager.sanitiseToSave(originalTotpUri, editedTotpUri)
            .getOrElse { return showInvalidTOTP() }

        if (editedTotpUri.isNotBlank()) {
            val topCodeResult = runCatching { totpManager.observeCode(sanitisedUri).firstOrNull() }

            if (topCodeResult.isFailure) {
                return showInvalidTOTP()
            }
        }

        val totpHiddenState = encryptionContextProvider.withEncryptionContext {
            UIHiddenState.Revealed(encrypt(sanitisedUri), sanitisedUri)
        }

        val (customFields, hasCustomFieldErrors) = validateCustomFields(
            loginItemFormState.customFields,
            originalTotpCustomFields
        )
        if (hasCustomFieldErrors) {
            return false
        }

        loginItemFormMutableState = loginItemFormState.copy(
            urls = websites,
            primaryTotp = totpHiddenState,
            customFields = customFields
        )

        val loginItemValidationErrors = loginItemFormState.validate()
        if (loginItemValidationErrors.isNotEmpty()) {
            loginItemValidationErrorsState.update { loginItemValidationErrors }
            return false
        }
        return true
    }

    private fun removeValidationErrors(vararg errors: ValidationError) {
        loginItemValidationErrorsState.update { currentLoginValidationErrors ->
            currentLoginValidationErrors.toMutableSet().apply {
                errors.forEach { error -> remove(error) }
            }
        }
    }

    private suspend fun showInvalidTOTP(): Boolean {
        addValidationError(LoginItemValidationError.InvalidTotp)
        snackbarDispatcher(LoginSnackbarMessages.InvalidTotpError)
        return false
    }

    private suspend fun validateCustomFields(
        editedCustomFields: List<UICustomFieldContent>,
        originalTotpCustomFields: List<UICustomFieldContent.Totp>
    ): Pair<List<UICustomFieldContent>, Boolean> {
        var hasCustomFieldErrors = false
        val fields = editedCustomFields.mapIndexed { idx, field ->
            when (field) {
                is UICustomFieldContent.Hidden -> field
                is UICustomFieldContent.Text -> field
                is UICustomFieldContent.Totp -> {
                    val (validated, hasErrors) = validateTotpField(
                        field,
                        idx,
                        originalTotpCustomFields
                    )
                    if (hasErrors) {
                        PassLogger.i(TAG, "TOTP custom field on index $idx had errors")
                        hasCustomFieldErrors = true
                    }
                    validated
                }
                is UICustomFieldContent.Date ->
                    throw IllegalStateException("Date field not supported")
            }
        }

        return fields to hasCustomFieldErrors
    }

    @Suppress("ReturnCount", "LongMethod")
    private suspend fun validateTotpField(
        field: UICustomFieldContent.Totp,
        index: Int,
        originalCustomFields: List<UICustomFieldContent.Totp>
    ): Pair<UICustomFieldContent.Totp, Boolean> {
        val content = when (val hiddenState = field.value) {
            is UIHiddenState.Revealed -> hiddenState.clearText
            is UIHiddenState.Concealed -> {
                encryptionContextProvider.withEncryptionContext {
                    decrypt(hiddenState.encrypted)
                }
            }

            is UIHiddenState.Empty -> ""
        }

        if (content.isBlank()) {
            addValidationError(CustomFieldValidationError.EmptyField(None, index))
            return field to true
        }

        val originalUri = encryptionContextProvider.withEncryptionContext {
            val value = originalCustomFields.find { it.id == field.id }?.value
            if (value != null) {
                decrypt(value.encrypted)
            } else {
                ""
            }
        }
        val sanitisedUri = totpManager.sanitiseToSave(originalUri, content)
            .getOrElse { _ ->
                addValidationError(CustomFieldValidationError.InvalidTotp(None, index))
                return field to true
            }
        if (sanitisedUri.isNotBlank()) {
            totpManager.parse(sanitisedUri).getOrElse {
                addValidationError(CustomFieldValidationError.InvalidTotp(None, index))
                return field to true
            }

            val totpCodeResult = runCatching { totpManager.observeCode(sanitisedUri).firstOrNull() }
            if (totpCodeResult.isFailure) {
                addValidationError(CustomFieldValidationError.InvalidTotp(None, index))
                return field to true
            }
        }
        val encryptedSanitized = encryptionContextProvider.withEncryptionContext {
            encrypt(sanitisedUri)
        }

        return UICustomFieldContent.Totp(
            label = field.label,
            value = when (field.value) {
                is UIHiddenState.Revealed -> {
                    UIHiddenState.Revealed(
                        encrypted = encryptedSanitized,
                        clearText = sanitisedUri
                    )
                }

                is UIHiddenState.Concealed -> {
                    UIHiddenState.Concealed(encryptedSanitized)
                }

                is UIHiddenState.Empty -> UIHiddenState.Empty(encryptedSanitized)
            },
            id = field.id
        ) to false
    }

    private fun sanitizeWebsites(websites: List<String>): List<String> = websites.map { url ->
        if (url.isBlank()) {
            ""
        } else {
            UrlSanitizer.sanitize(url).fold(
                onSuccess = { it },
                onFailure = { url }
            )
        }
    }

    fun onDeleteLinkedApp(packageInfo: PackageInfoUi) {
        onUserEditedContent()
        loginItemFormMutableState = loginItemFormState.copy(
            packageInfoSet = loginItemFormState.packageInfoSet.minus(packageInfo)
        )
    }

    fun onPasteTotp() = viewModelScope.launch(Dispatchers.IO) {
        onUserEditedContent()
        clipboardManager.getClipboardContent()
            .onSuccess { clipboardContent ->
                val sanitisedContent = clipboardContent
                    .replace(" ", "")
                    .replace("\n", "")
                val encryptedContent = encryptionContextProvider.withEncryptionContext {
                    encrypt(sanitisedContent)
                }
                withContext(Dispatchers.Main) {
                    when (val field = focusedFieldFlow.value.value()) {
                        is LoginField.CustomField -> {
                            val customFieldTOTP = field.field.takeIf { it.type == CustomFieldType.Totp }
                            val customFields = loginItemFormState.customFields
                            if (customFieldTOTP != null && customFields.size - 1 >= customFieldTOTP.index) {
                                val updatedCustomFields = customFields.toMutableList()
                                    .mapIndexed { index, customFieldContent ->
                                        if (
                                            customFieldContent is UICustomFieldContent.Totp &&
                                            index == customFieldTOTP.index
                                        ) {
                                            customFieldContent.copy(
                                                value = UIHiddenState.Revealed(
                                                    encryptedContent,
                                                    sanitisedContent
                                                )
                                            )
                                        } else {
                                            customFieldContent
                                        }
                                    }
                                loginItemFormMutableState = loginItemFormState.copy(
                                    customFields = updatedCustomFields
                                )
                            }
                        }

                        else -> {
                            loginItemFormMutableState = loginItemFormState.copy(
                                primaryTotp = UIHiddenState.Revealed(
                                    encryptedContent,
                                    sanitisedContent
                                )
                            )
                        }
                    }
                }
            }
            .onFailure { PassLogger.d(TAG, it, "Failed on getting clipboard content") }
    }

    fun onRemoveAlias() {
        onUserEditedContent()
        aliasLocalItemState.update { None }
        draftRepository.delete<AliasItemFormState>(CreateAliasViewModel.KEY_DRAFT_ALIAS)

        loginItemFormMutableState = loginItemFormState.copy(email = "")
        canUpdateUsernameState.update { true }
    }

    fun onCustomFieldChange(index: Int, value: String) = viewModelScope.launch {
        if (index >= loginItemFormState.customFields.size) return@launch

        removeValidationErrors(
            CustomFieldValidationError.EmptyField(None, index),
            CustomFieldValidationError.InvalidTotp(None, index)
        )

        val customFields = loginItemFormState.customFields.toMutableList()

        val updated = encryptionContextProvider.withEncryptionContext {
            when (val field = customFields[index]) {
                is UICustomFieldContent.Hidden -> {
                    UICustomFieldContent.Hidden(
                        label = field.label,
                        value = UIHiddenState.Revealed(
                            encrypted = encrypt(value),
                            clearText = value
                        )
                    )
                }

                is UICustomFieldContent.Text -> UICustomFieldContent.Text(
                    label = field.label,
                    value = value
                )

                is UICustomFieldContent.Totp -> UICustomFieldContent.Totp(
                    label = field.label,
                    value = UIHiddenState.Revealed(
                        encrypted = encrypt(value),
                        clearText = value
                    ),
                    id = field.id
                )

                is UICustomFieldContent.Date ->
                    throw IllegalStateException("Date field not supported")
            }
        }

        customFields[index] = updated
        loginItemFormMutableState = loginItemFormState.copy(
            customFields = customFields.toPersistentList()
        )
    }

    protected fun onUserEditedContent() {
        if (hasUserEditedContentFlow.value) return
        hasUserEditedContentFlow.update { true }
    }

    private suspend fun observeGeneratedPassword() {
        draftRepository
            .get<String>(DRAFT_PASSWORD_KEY)
            .collect {
                if (it is Some) {
                    draftRepository.delete<String>(DRAFT_PASSWORD_KEY).value()
                        ?.let { encryptedPassword ->
                            val password = encryptionContextProvider.withEncryptionContext {
                                decrypt(encryptedPassword)
                            }
                            loginItemFormMutableState = loginItemFormState.copy(
                                password = UIHiddenState.Revealed(
                                    encrypted = encryptedPassword,
                                    clearText = password
                                ),
                                passwordStrength = passwordStrengthCalculator.calculateStrength(
                                    password
                                )
                            )
                        }
                }
            }
    }

    private suspend fun observeCustomField() {
        customFieldDraftRepository.observeCustomFieldEvents()
            .collect {
                onUserEditedContent()
                when (it) {
                    is DraftFormFieldEvent.FieldAdded -> onFieldAdded(it)
                    is DraftFormFieldEvent.FieldRemoved -> onFieldRemoved(it)
                    is DraftFormFieldEvent.FieldRenamed -> onFieldRenamed(it)
                }
            }
    }

    private fun onFieldRemoved(event: DraftFormFieldEvent.FieldRemoved) {
        val (_, index) = event
        loginItemFormMutableState = loginItemFormState.copy(
            customFields = loginItemFormState.customFields
                .toMutableList()
                .apply { removeAt(index) }
                .toPersistentList()
        )
    }

    private fun onFieldRenamed(event: DraftFormFieldEvent.FieldRenamed) {
        val (_, index, newLabel) = event
        val customFields = loginItemFormState.customFields.toMutableList()
        val updated = when (val field = customFields[index]) {
            is UICustomFieldContent.Hidden -> {
                UICustomFieldContent.Hidden(
                    label = newLabel,
                    value = field.value
                )
            }

            is UICustomFieldContent.Text -> UICustomFieldContent.Text(
                label = newLabel,
                value = field.value
            )

            is UICustomFieldContent.Totp -> UICustomFieldContent.Totp(
                label = newLabel,
                value = field.value,
                id = field.id
            )

            is UICustomFieldContent.Date -> throw IllegalStateException("Date field not supported")
        }
        customFields[index] = updated
        loginItemFormMutableState =
            loginItemFormState.copy(customFields = customFields.toPersistentList())

        when (loginItemFormState.customFields.getOrNull(index)) {
            is UICustomFieldContent.Hidden -> focusedFieldFlow.update {
                LoginField.CustomField(
                    field = CustomFieldIdentifier(
                        index = index,
                        type = CustomFieldType.Hidden
                    )
                ).some()
            }

            is UICustomFieldContent.Text -> focusedFieldFlow.update {
                LoginField.CustomField(
                    field = CustomFieldIdentifier(
                        index = index,
                        type = CustomFieldType.Text
                    )
                ).some()
            }

            is UICustomFieldContent.Totp -> focusedFieldFlow.update {
                LoginField.CustomField(
                    field = CustomFieldIdentifier(
                        index = index,
                        type = CustomFieldType.Totp
                    )
                ).some()
            }

            is UICustomFieldContent.Date -> throw IllegalStateException("Date field not supported")

            null -> {}
        }
    }

    private fun onFieldAdded(event: DraftFormFieldEvent.FieldAdded) {
        val (_, label, type) = event
        val field = encryptionContextProvider.withEncryptionContext {
            createCustomField(type, label, this)
        }
        loginItemFormMutableState = loginItemFormState.copy(
            customFields = loginItemFormState.customFields.toMutableList()
                .apply { add(field) }
                .toPersistentList()
        )
        val index = loginItemFormState.customFields.size - 1
        when (field) {
            is UICustomFieldContent.Hidden -> focusedFieldFlow.update {
                LoginField.CustomField(
                    field = CustomFieldIdentifier(
                        index = index,
                        type = CustomFieldType.Hidden
                    )
                ).some()
            }

            is UICustomFieldContent.Text -> focusedFieldFlow.update {
                LoginField.CustomField(
                    field = CustomFieldIdentifier(
                        index = index,
                        type = CustomFieldType.Text
                    )
                ).some()
            }

            is UICustomFieldContent.Totp -> focusedFieldFlow.update {
                LoginField.CustomField(
                    field = CustomFieldIdentifier(
                        index = index,
                        type = CustomFieldType.Totp
                    )
                ).some()
            }

            is UICustomFieldContent.Date ->
                throw IllegalStateException("Date field not supported in login")
        }
    }

    internal fun onFocusChange(field: LoginField, isFocused: Boolean) {
        when (field) {
            LoginField.Password -> updatePasswordOnFocusChange(isFocused)
            LoginField.PrimaryTotp -> updatePrimaryTotpOnFocusChange()
            is LoginField.CustomField -> when (field.field.type) {
                CustomFieldType.Hidden -> updateCustomFieldHiddenOnFocusChange(field.field, isFocused)
                else -> {}
            }
            LoginField.Email,
            LoginField.Username,
            LoginField.Title -> {
            }

        }
        if (isFocused) {
            focusedFieldFlow.update { field.some() }
        } else {
            focusedFieldFlow.update { None }
        }
    }

    internal fun onTooltipDismissed(tooltip: Tooltip) = viewModelScope.launch {
        runCatching { disableTooltip(tooltip) }
            .onFailure { error ->
                PassLogger.w(TAG, "There was an error disabling tooltip: $tooltip")
                PassLogger.w(TAG, error)
            }
    }

    internal fun onUsernameOrEmailManuallyExpanded() {
        loginItemFormMutableState = loginItemFormState.copy(isExpandedByUser = true)
    }

    private fun updateCustomFieldHiddenOnFocusChange(field: CustomFieldIdentifier, isFocused: Boolean) {
        val customFields = loginItemFormState.customFields.toMutableList()
        val customFieldContent: UICustomFieldContent.Hidden? = customFields.getOrNull(field.index)
            as? UICustomFieldContent.Hidden
        customFieldContent ?: return
        val hiddenValueByteArray = encryptionContextProvider.withEncryptionContext {
            decrypt(customFieldContent.value.encrypted.toEncryptedByteArray())
        }
        val hiddenFieldHiddenState = when {
            isFocused -> UIHiddenState.Revealed(
                encrypted = customFieldContent.value.encrypted,
                clearText = hiddenValueByteArray.decodeToString()
            )

            hiddenValueByteArray.isEmpty() -> UIHiddenState.Empty(customFieldContent.value.encrypted)
            else -> UIHiddenState.Concealed(customFieldContent.value.encrypted)
        }
        customFields[field.index] = customFieldContent.copy(
            value = hiddenFieldHiddenState
        )
        loginItemFormMutableState =
            loginItemFormState.copy(customFields = customFields.toPersistentList())
    }

    private fun updatePrimaryTotpOnFocusChange() {
        val primaryTotp = encryptionContextProvider.withEncryptionContext {
            UIHiddenState.Revealed(
                loginItemFormState.primaryTotp.encrypted,
                decrypt(loginItemFormState.primaryTotp.encrypted)
            )
        }
        loginItemFormMutableState = loginItemFormState.copy(primaryTotp = primaryTotp)
    }

    private fun updatePasswordOnFocusChange(isFocused: Boolean) {
        val passwordByteArray = encryptionContextProvider.withEncryptionContext {
            decrypt(loginItemFormState.password.encrypted.toEncryptedByteArray())
        }
        val passwordHiddenState = when {
            isFocused -> UIHiddenState.Revealed(
                loginItemFormState.password.encrypted,
                passwordByteArray.decodeToString()
            )

            passwordByteArray.isEmpty() -> UIHiddenState.Empty(loginItemFormState.password.encrypted)
            else -> UIHiddenState.Concealed(loginItemFormState.password.encrypted)
        }
        loginItemFormMutableState = loginItemFormState.copy(password = passwordHiddenState)
    }

    private suspend fun observeDisplayUsernameFieldPreference() {
        userPreferencesRepository.observeDisplayUsernameFieldPreference()
            .collect { displayUsernameFieldPreference ->
                loginItemFormMutableState = loginItemFormState.copy(
                    isExpandedByPreference = displayUsernameFieldPreference.value
                )
            }
    }

    private fun addValidationError(error: ValidationError) {
        loginItemValidationErrorsState.update { errors ->
            errors.toMutableSet().apply { add(error) }
        }
    }

    protected fun updatePrimaryTotpIfNeeded(
        navTotpUri: String?,
        navTotpIndex: Int,
        currentValue: LoginItemFormState
    ) = navTotpUri
        ?.takeIf { navTotpIndex == -1 }
        ?.let { decrypted ->
            val encrypted =
                encryptionContextProvider.withEncryptionContext { encrypt(decrypted) }
            UIHiddenState.Revealed(encrypted, decrypted)
        }
        ?: currentValue.primaryTotp

    protected fun updateCustomFieldsIfNeeded(
        navTotpUri: String?,
        navTotpIndex: Int,
        currentValue: LoginItemFormState
    ) = if (navTotpUri != null) {
        navTotpUri
            .takeIf { navTotpIndex >= 0 }
            ?.let { decrypted ->
                currentValue.customFields
                    .mapIndexed { index, customFieldContent ->
                        if (
                            navTotpIndex == index &&
                            customFieldContent is UICustomFieldContent.Totp
                        ) {
                            val encrypted = encryptionContextProvider.withEncryptionContext {
                                encrypt(decrypted)
                            }
                            val hiddenState = UIHiddenState.Revealed(encrypted, decrypted)
                            customFieldContent.copy(value = hiddenState)
                        } else {
                            customFieldContent
                        }
                    }
            }
            ?: currentValue.customFields
    } else {
        currentValue.customFields
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

    suspend fun isDALEnabled(): Boolean = userPreferencesRepository
        .observeUseDigitalAssetLinksPreference()
        .firstOrNull()
        ?.value()
        ?: false

    private companion object {

        private const val TAG = "BaseLoginViewModel"

    }

}

