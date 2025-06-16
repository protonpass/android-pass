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
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.itemcreate.common.ValidationError
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldHandler
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldIdentifier
import proton.android.pass.features.itemcreate.common.formprocessor.FormProcessingResult
import proton.android.pass.features.itemcreate.common.formprocessor.LoginItemFormProcessor
import proton.android.pass.features.itemcreate.common.formprocessor.LoginItemFormProcessorType
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
    protected val customFieldHandler: CustomFieldHandler,
    private val featureFlagsRepository: FeatureFlagsPreferencesRepository,
    private val customFieldDraftRepository: CustomFieldDraftRepository,
    private val loginItemFormProcessor: LoginItemFormProcessorType,
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
        removeValidationErrors(LoginItemValidationError.InvalidPrimaryTotp)
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

    protected suspend fun isFormStateValid(
        originalPrimaryTotp: Option<UIHiddenState> = None,
        originalTotpCustomFields: List<UICustomFieldContent.Totp> = emptyList()
    ): Boolean {
        val result = encryptionContextProvider.withEncryptionContextSuspendable {
            loginItemFormProcessor.process(
                LoginItemFormProcessor.Input(
                    originalPrimaryTotp = originalPrimaryTotp,
                    originalCustomFields = originalTotpCustomFields,
                    formState = loginItemFormState
                ),
                ::decrypt,
                ::encrypt
            )
        }
        return when (result) {
            is FormProcessingResult.Error -> {
                loginItemValidationErrorsState.update { result.errors }
                false
            }
            is FormProcessingResult.Success -> {
                loginItemFormMutableState = result.sanitized
                true
            }
        }
    }

    private fun removeValidationErrors(vararg errors: ValidationError) {
        loginItemValidationErrorsState.update { currentLoginValidationErrors ->
            currentLoginValidationErrors.toMutableSet().apply {
                errors.forEach { error -> remove(error) }
            }
        }
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

    fun onCustomFieldChange(id: CustomFieldIdentifier, value: String) {
        removeValidationErrors(
            CustomFieldValidationError.EmptyField(index = id.index),
            CustomFieldValidationError.InvalidTotp(index = id.index)
        )

        val updated = customFieldHandler.onCustomFieldValueChanged(
            customFieldIdentifier = id,
            customFieldList = loginItemFormState.customFields,
            value = value
        )

        loginItemFormMutableState = loginItemFormState.copy(
            customFields = updated.toPersistentList()
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
        val updated = customFieldHandler.onCustomFieldRenamed(
            customFieldList = loginItemFormState.customFields,
            index = index,
            newLabel = newLabel
        )
        loginItemFormMutableState = loginItemFormState.copy(customFields = updated)
    }

    private fun onFieldAdded(event: DraftFormFieldEvent.FieldAdded) {
        val (_, label, type) = event
        val added = customFieldHandler.onCustomFieldAdded(label, type)
        loginItemFormMutableState = loginItemFormState.copy(
            customFields = loginItemFormState.customFields + added
        )
        val identifier = CustomFieldIdentifier(
            index = loginItemFormState.customFields.lastIndex,
            type = type
        )
        focusedFieldFlow.update { LoginField.CustomField(identifier).some() }
    }

    internal fun onFocusChange(field: LoginField, isFocused: Boolean) {
        when (field) {
            LoginField.Password -> updatePasswordOnFocusChange(isFocused)
            LoginField.PrimaryTotp -> updatePrimaryTotpOnFocusChange()
            is LoginField.CustomField -> updateCustomFieldOnFocusChange(field.field, isFocused)
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

    private fun updateCustomFieldOnFocusChange(field: CustomFieldIdentifier, isFocused: Boolean) {
        val customFields = customFieldHandler.onCustomFieldFocusedChanged(
            customFieldIdentifier = field,
            customFieldList = loginItemFormState.customFields,
            isFocused = isFocused
        )
        loginItemFormMutableState = loginItemFormState.copy(customFields = customFields)
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

