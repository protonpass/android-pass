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

package proton.android.pass.featureitemcreate.impl.login

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
import proton.android.pass.commonrust.api.EmailValidator
import proton.android.pass.commonrust.api.passwords.strengths.PasswordStrengthCalculator
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.crypto.api.toEncryptedByteArray
import proton.android.pass.data.api.repositories.DRAFT_CUSTOM_FIELD_KEY
import proton.android.pass.data.api.repositories.DRAFT_CUSTOM_FIELD_TITLE_KEY
import proton.android.pass.data.api.repositories.DRAFT_PASSWORD_KEY
import proton.android.pass.data.api.repositories.DRAFT_REMOVE_CUSTOM_FIELD_KEY
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.url.UrlSanitizer
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.data.api.usecases.tooltips.DisableTooltip
import proton.android.pass.data.api.usecases.tooltips.ObserveTooltipEnabled
import proton.android.pass.domain.CustomFieldContent
import proton.android.pass.domain.Item
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.tooltips.Tooltip
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.OpenScanState
import proton.android.pass.featureitemcreate.impl.alias.AliasItemFormState
import proton.android.pass.featureitemcreate.impl.alias.CreateAliasViewModel
import proton.android.pass.featureitemcreate.impl.common.CustomFieldIndexTitle
import proton.android.pass.featureitemcreate.impl.common.UICustomFieldContent
import proton.android.pass.featureitemcreate.impl.common.UIHiddenState
import proton.android.pass.featureitemcreate.impl.common.attachments.AttachmentsHandler
import proton.android.pass.featureitemcreate.impl.login.LoginItemValidationErrors.CustomFieldValidationError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.totp.api.TotpManager

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
    observeCurrentUser: ObserveCurrentUser,
    observeUpgradeInfo: ObserveUpgradeInfo,
    observeTooltipEnabled: ObserveTooltipEnabled,
    featureFlagsRepository: FeatureFlagsPreferencesRepository,
    savedStateHandleProvider: SavedStateHandleProvider
) : ViewModel() {

    private val hasUserEditedContentFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)

    init {
        attachmentsHandler.observeNewAttachments(viewModelScope) { newUris ->
            if (newUris.isNotEmpty()) {
                onUserEditedContent()
                newUris.forEach { uri ->
                    attachmentsHandler.uploadNewAttachment(uri, viewModelScope)
                }
            }
        }
    }

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
            launch { observeNewCustomField() }
            launch { observeRemoveCustomField() }
            launch { observeRenameCustomField() }
            launch { observeDisplayUsernameFieldPreference() }
        }
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

    protected val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    protected val isItemSavedState: MutableStateFlow<ItemSavedState> =
        MutableStateFlow(ItemSavedState.Unknown)
    private val openScanState: MutableStateFlow<OpenScanState> =
        MutableStateFlow(OpenScanState.Unknown)

    private val eventsFlow: Flow<Events> = combine(isItemSavedState, openScanState, ::Events)

    private data class Events(
        val itemSavedState: ItemSavedState,
        val openScanState: OpenScanState
    )

    private val loginItemValidationErrorsState: MutableStateFlow<Set<LoginItemValidationErrors>> =
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
        attachmentsHandler.attachmentsFlow
    ) { loginItemValidationErrors, primaryEmail, aliasItemFormState, isLoading, totpUiState,
        upgradeInfoResult, userInteraction, isUsernameSplitTooltipEnabled,
        isFileAttachmentsEnabled, attachmentsState ->
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
        removeValidationErrors(LoginItemValidationErrors.BlankTitle)

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
                username = trimmedNewEmail
            )
        }.also { updatedLoginItemFormState ->
            loginItemFormMutableState = updatedLoginItemFormState
        }
    }

    internal fun onUsernameChanged(newUsername: String) {
        onUserEditedContent()

        loginItemFormMutableState = loginItemFormMutableState.copy(
            username = newUsername.trim()
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
        removeValidationErrors(LoginItemValidationErrors.InvalidTotp)
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
        removeValidationErrors(LoginItemValidationErrors.InvalidUrl(index))
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
            removeValidationErrors(LoginItemValidationErrors.InvalidUrl(index))
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

    internal fun onClose() {
        draftRepository.delete<AliasItemFormState>(CreateAliasViewModel.KEY_DRAFT_ALIAS)
        draftRepository.delete<CustomFieldContent>(DRAFT_CUSTOM_FIELD_KEY)
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

    private fun removeValidationErrors(vararg errors: LoginItemValidationErrors) {
        loginItemValidationErrorsState.update { currentLoginValidationErrors ->
            currentLoginValidationErrors.toMutableSet().apply {
                errors.forEach { error -> remove(error) }
            }
        }
    }

    private suspend fun showInvalidTOTP(): Boolean {
        addValidationError(LoginItemValidationErrors.InvalidTotp)
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
            addValidationError(CustomFieldValidationError.EmptyField(index))
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
                addValidationError(CustomFieldValidationError.InvalidTotp(index))
                return field to true
            }
        if (sanitisedUri.isNotBlank()) {
            totpManager.parse(sanitisedUri).getOrElse {
                addValidationError(CustomFieldValidationError.InvalidTotp(index))
                return field to true
            }

            val totpCodeResult = runCatching { totpManager.observeCode(sanitisedUri).firstOrNull() }
            if (totpCodeResult.isFailure) {
                addValidationError(CustomFieldValidationError.InvalidTotp(index))
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
                    when (focusedFieldFlow.value.value()) {
                        is LoginCustomField.CustomFieldTOTP -> {
                            val customFieldTOTP =
                                focusedFieldFlow.value.value() as? LoginCustomField.CustomFieldTOTP
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
            CustomFieldValidationError.EmptyField(index),
            CustomFieldValidationError.InvalidTotp(index)
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

    private suspend fun observeNewCustomField() {
        draftRepository
            .get<CustomFieldContent>(DRAFT_CUSTOM_FIELD_KEY)
            .collect {
                if (it is Some) {
                    draftRepository.delete<CustomFieldContent>(DRAFT_CUSTOM_FIELD_KEY).value()
                        ?.let { customField ->
                            loginItemFormMutableState = loginItemFormState.copy(
                                customFields = loginItemFormState.customFields.toMutableList()
                                    .apply { add(UICustomFieldContent.from(customField)) }
                                    .toPersistentList()
                            )
                            val index = loginItemFormState.customFields.size - 1
                            when (customField) {
                                is CustomFieldContent.Hidden -> focusedFieldFlow.update {
                                    LoginCustomField.CustomFieldHidden(index).some()
                                }

                                is CustomFieldContent.Text -> focusedFieldFlow.update {
                                    LoginCustomField.CustomFieldText(index).some()
                                }

                                is CustomFieldContent.Totp -> focusedFieldFlow.update {
                                    LoginCustomField.CustomFieldTOTP(index).some()
                                }
                            }
                        }
                }
            }
    }

    internal fun onFocusChange(field: LoginField, isFocused: Boolean) {
        when (field) {
            LoginField.Password -> updatePasswordOnFocusChange(isFocused)
            LoginField.PrimaryTotp -> updatePrimaryTotpOnFocusChange()
            is LoginCustomField.CustomFieldHidden ->
                updateCustomFieldHiddenOnFocusChange(field, isFocused)

            LoginField.Email,
            LoginField.Username,
            is LoginCustomField.CustomFieldTOTP,
            is LoginCustomField.CustomFieldText,
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

    private fun updateCustomFieldHiddenOnFocusChange(field: LoginCustomField.CustomFieldHidden, isFocused: Boolean) {
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

    private suspend fun observeRemoveCustomField() {
        draftRepository
            .get<Int>(DRAFT_REMOVE_CUSTOM_FIELD_KEY)
            .collect {
                if (it is Some) {
                    draftRepository.delete<Int>(DRAFT_REMOVE_CUSTOM_FIELD_KEY)
                        .value()
                        ?.let { index -> removeCustomField(index) }
                }
            }
    }

    private suspend fun observeRenameCustomField() {
        draftRepository
            .get<CustomFieldIndexTitle>(DRAFT_CUSTOM_FIELD_TITLE_KEY)
            .collect {
                if (it is Some) {
                    draftRepository.delete<CustomFieldIndexTitle>(DRAFT_CUSTOM_FIELD_TITLE_KEY)
                        .value()
                        ?.let { customField ->
                            renameCustomField(customField)
                        }
                }
            }
    }

    private suspend fun observeDisplayUsernameFieldPreference() {
        userPreferencesRepository.observeDisplayUsernameFieldPreference()
            .collect { displayUsernameFieldPreference ->
                loginItemFormMutableState = loginItemFormState.copy(
                    isExpandedByPreference = displayUsernameFieldPreference.value
                )
            }
    }

    private fun renameCustomField(indexTitle: CustomFieldIndexTitle) {
        val customFields = loginItemFormState.customFields.toMutableList()
        val updated = when (val field = customFields[indexTitle.index]) {
            is UICustomFieldContent.Hidden -> {
                UICustomFieldContent.Hidden(
                    label = indexTitle.title,
                    value = field.value
                )
            }

            is UICustomFieldContent.Text -> UICustomFieldContent.Text(
                label = indexTitle.title,
                value = field.value
            )

            is UICustomFieldContent.Totp -> UICustomFieldContent.Totp(
                label = indexTitle.title,
                value = field.value,
                id = field.id
            )
        }
        customFields[indexTitle.index] = updated
        loginItemFormMutableState =
            loginItemFormState.copy(customFields = customFields.toPersistentList())

        when (loginItemFormState.customFields.getOrNull(indexTitle.index)) {
            is UICustomFieldContent.Hidden -> focusedFieldFlow.update {
                LoginCustomField.CustomFieldHidden(indexTitle.index).some()
            }

            is UICustomFieldContent.Text -> focusedFieldFlow.update {
                LoginCustomField.CustomFieldText(indexTitle.index).some()
            }

            is UICustomFieldContent.Totp -> focusedFieldFlow.update {
                LoginCustomField.CustomFieldTOTP(indexTitle.index).some()
            }

            null -> {}
        }
    }

    private fun removeCustomField(index: Int) = viewModelScope.launch {
        onUserEditedContent()
        loginItemFormMutableState = loginItemFormState.copy(
            customFields = loginItemFormState.customFields
                .toMutableList()
                .apply { removeAt(index) }
                .toPersistentList()
        )
    }

    private fun addValidationError(error: LoginItemValidationErrors) {
        loginItemValidationErrorsState.update { errors ->
            errors.toMutableSet().apply { add(error) }
        }
    }

    protected fun updatePrimaryTotpIfNeeded(
        navTotpUri: String?,
        navTotpIndex: Int?,
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

    override fun onCleared() {
        attachmentsHandler.clearAttachments()
        super.onCleared()
    }

    private companion object {

        private const val TAG = "BaseLoginViewModel"

    }

}

