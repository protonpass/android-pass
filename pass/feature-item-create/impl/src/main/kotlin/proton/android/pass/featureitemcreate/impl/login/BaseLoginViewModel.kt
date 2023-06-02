package proton.android.pass.featureitemcreate.impl.login

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.DRAFT_CUSTOM_FIELD_KEY
import proton.android.pass.data.api.repositories.DRAFT_CUSTOM_FIELD_TITLE_KEY
import proton.android.pass.data.api.repositories.DRAFT_PASSWORD_KEY
import proton.android.pass.data.api.repositories.DRAFT_REMOVE_CUSTOM_FIELD_KEY
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.url.UrlSanitizer
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.UpgradeInfo
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.OpenScanState
import proton.android.pass.featureitemcreate.impl.alias.AliasItem
import proton.android.pass.featureitemcreate.impl.alias.CreateAliasViewModel
import proton.android.pass.featureitemcreate.impl.common.CustomFieldIndexTitle
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.FeatureFlag
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.totp.api.TotpManager
import proton.pass.domain.CustomFieldContent
import proton.pass.domain.HiddenState
import proton.pass.domain.ItemContents
import proton.pass.domain.PlanType

@Suppress("TooManyFunctions", "LargeClass")
abstract class BaseLoginViewModel(
    protected val accountManager: AccountManager,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val clipboardManager: ClipboardManager,
    private val totpManager: TotpManager,
    private val draftRepository: DraftRepository,
    private val encryptionContextProvider: EncryptionContextProvider,
    observeCurrentUser: ObserveCurrentUser,
    observeUpgradeInfo: ObserveUpgradeInfo,
    ffRepo: FeatureFlagsPreferencesRepository,
) : ViewModel() {

    private val hasUserEditedContentFlow: MutableStateFlow<Boolean> = MutableStateFlow(false)
    protected val itemContentState: MutableStateFlow<ItemContents.Login> =
        MutableStateFlow(
            encryptionContextProvider.withEncryptionContext {
                ItemContents.Login.create(
                    HiddenState.Empty(encrypt("")),
                    HiddenState.Empty(encrypt(""))
                )
            }
        )
    protected val aliasLocalItemState: MutableStateFlow<Option<AliasItem>> = MutableStateFlow(None)
    private val aliasDraftState: Flow<Option<AliasItem>> = draftRepository
        .get(CreateAliasViewModel.KEY_DRAFT_ALIAS)
    private val focusedFieldFlow: MutableStateFlow<Option<LoginField>> = MutableStateFlow(None)

    init {
        viewModelScope.launch {
            launch { observeGeneratedPassword() }
            launch { observeNewCustomField() }
            launch { observeRemoveCustomField() }
            launch { observeRenameCustomField() }
        }
    }

    private val aliasState: Flow<Option<AliasItem>> = combine(
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

    data class Events(
        val itemSavedState: ItemSavedState,
        val openScanState: OpenScanState,
    )

    private val loginItemValidationErrorsState: MutableStateFlow<Set<LoginItemValidationErrors>> =
        MutableStateFlow(emptySet())
    private val focusLastWebsiteState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    protected val canUpdateUsernameState: MutableStateFlow<Boolean> = MutableStateFlow(true)

    protected val upgradeInfoFlow: Flow<UpgradeInfo> = observeUpgradeInfo().distinctUntilChanged()

    private val loginAliasItemWrapperState = combine(
        itemContentState,
        loginItemValidationErrorsState,
        canUpdateUsernameState,
        observeCurrentUser().map { it.email },
        aliasState,
        ::LoginAliasItemWrapper
    )

    private data class LoginAliasItemWrapper(
        val content: ItemContents.Login,
        val loginItemValidationErrors: Set<LoginItemValidationErrors>,
        val canUpdateUsername: Boolean,
        val primaryEmail: String?,
        val aliasItem: Option<AliasItem>
    )

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

    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    val baseLoginUiState: StateFlow<BaseLoginUiState> = combineN(
        loginAliasItemWrapperState,
        isLoadingState,
        eventsFlow,
        focusLastWebsiteState,
        hasUserEditedContentFlow,
        totpUiStateFlow,
        upgradeInfoFlow.asLoadingResult(),
        ffRepo.get<Boolean>(FeatureFlag.CUSTOM_FIELDS_ENABLED),
        focusedFieldFlow
    ) { loginItemWrapper, isLoading, events,
        focusLastWebsite, hasUserEditedContent,
        totpUiState, upgradeInfoResult, customFieldsEnabled, focusedField ->

        val customFieldsState = if (!customFieldsEnabled) {
            CustomFieldsState.Disabled
        } else {
            val plan = upgradeInfoResult.getOrNull()?.plan
            when (plan?.planType) {
                is PlanType.Paid, is PlanType.Trial -> {
                    CustomFieldsState.Enabled(
                        customFields = loginItemWrapper.content.customFields,
                        isLimited = false
                    )
                }

                else -> {
                    if (loginItemWrapper.content.customFields.isNotEmpty()) {
                        CustomFieldsState.Enabled(
                            customFields = loginItemWrapper.content.customFields,
                            isLimited = true
                        )
                    } else {
                        CustomFieldsState.Disabled
                    }
                }
            }
        }

        BaseLoginUiState(
            contents = loginItemWrapper.content,
            validationErrors = loginItemWrapper.loginItemValidationErrors.toPersistentSet(),
            isLoadingState = isLoading,
            isItemSaved = events.itemSavedState,
            openScanState = events.openScanState,
            focusLastWebsite = focusLastWebsite,
            canUpdateUsername = loginItemWrapper.canUpdateUsername,
            primaryEmail = loginItemWrapper.primaryEmail,
            aliasItem = loginItemWrapper.aliasItem.value(),
            hasUserEditedContent = hasUserEditedContent,
            hasReachedAliasLimit = upgradeInfoResult.getOrNull()?.hasReachedAliasLimit() ?: false,
            totpUiState = totpUiState,
            customFieldsState = customFieldsState,
            focusedField = focusedField.value()
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BaseLoginUiState.create(
                HiddenState.Empty(""),
                HiddenState.Empty("")
            )
        )

    fun onTitleChange(value: String) {
        onUserEditedContent()
        itemContentState.update { it.copy(title = value) }
        loginItemValidationErrorsState.update {
            it.toMutableSet().apply { remove(LoginItemValidationErrors.BlankTitle) }
        }

        val aliasItem = aliasLocalItemState.value
        if (aliasItem is Some) {
            aliasLocalItemState.update { aliasItem.value.copy(title = value).toOption() }
        }
    }

    fun onUsernameChange(value: String) {
        onUserEditedContent()
        itemContentState.update { it.copy(username = value) }
    }

    fun onPasswordChange(value: String) {
        onUserEditedContent()
        encryptionContextProvider.withEncryptionContext {
            itemContentState.update {
                it.copy(password = HiddenState.Revealed(encrypt(value), value))
            }
        }
    }

    fun onTotpChange(value: String) {
        onUserEditedContent()
        val newValue = value.replace(" ", "").replace("\n", "")
        encryptionContextProvider.withEncryptionContext {
            itemContentState.update {
                it.copy(primaryTotp = HiddenState.Revealed(encrypt(newValue), newValue))
            }
        }
        loginItemValidationErrorsState.update {
            it.toMutableSet().apply { remove(LoginItemValidationErrors.InvalidTotp) }
        }
    }

    fun onWebsiteChange(value: String, index: Int) {
        onUserEditedContent()
        val newValue = value.replace(" ", "").replace("\n", "")
        itemContentState.update {
            it.copy(
                urls = it.urls.toMutableList()
                    .apply { this[index] = newValue }
            )
        }
        loginItemValidationErrorsState.update {
            it.toMutableSet().apply { remove(LoginItemValidationErrors.InvalidUrl(index)) }
        }
        focusLastWebsiteState.update { false }
    }

    fun onAddWebsite() {
        onUserEditedContent()
        itemContentState.update {
            val websites = sanitizeWebsites(it.urls).toMutableList()
            websites.add("")

            it.copy(urls = websites)
        }
        focusLastWebsiteState.update { true }
    }

    fun onRemoveWebsite(index: Int) {
        onUserEditedContent()
        itemContentState.update {
            it.copy(urls = it.urls.toMutableList().apply { removeAt(index) })
        }
        loginItemValidationErrorsState.update {
            it.toMutableSet().apply { remove(LoginItemValidationErrors.InvalidUrl(index)) }
        }
        focusLastWebsiteState.update { false }
    }

    fun onNoteChange(value: String) {
        onUserEditedContent()
        itemContentState.update { it.copy(note = value) }
    }

    fun onEmitSnackbarMessage(snackbarMessage: LoginSnackbarMessages) =
        viewModelScope.launch {
            snackbarDispatcher(snackbarMessage)
        }

    fun onAliasCreated(aliasItem: AliasItem) {
        onUserEditedContent()
        aliasLocalItemState.update { aliasItem.toOption() }
        val alias = aliasItem.aliasToBeCreated
        if (alias != null) {
            itemContentState.update { it.copy(username = alias) }
            canUpdateUsernameState.update { false }
        }
    }

    fun onClose() {
        draftRepository.delete<AliasItem>(CreateAliasViewModel.KEY_DRAFT_ALIAS)
        draftRepository.delete<CustomFieldContent>(DRAFT_CUSTOM_FIELD_KEY)
    }

    @Suppress("ReturnCount")
    protected suspend fun validateItem(): Boolean {
        itemContentState.update { state ->
            val websites = sanitizeWebsites(state.urls)
            val decryptedTotp = encryptionContextProvider.withEncryptionContext {
                decrypt(state.primaryTotp.encrypted)
            }
            val sanitisedPrimaryTotp = if (decryptedTotp.isNotBlank()) {
                sanitizeOTP(decryptedTotp)
                    .fold(
                        onSuccess = { it },
                        onFailure = {
                            addValidationError(LoginItemValidationErrors.InvalidTotp)
                            snackbarDispatcher(LoginSnackbarMessages.InvalidTotpError)
                            return false
                        }
                    )
            } else {
                ""
            }
            val totpHiddenState = encryptionContextProvider.withEncryptionContext {
                HiddenState.Revealed(encrypt(sanitisedPrimaryTotp), sanitisedPrimaryTotp)
            }

            val (customFields, hasCustomFieldErrors) = validateCustomFields(state.customFields)
            if (hasCustomFieldErrors) {
                return false
            }

            state.copy(urls = websites, primaryTotp = totpHiddenState, customFields = customFields)
        }

        val loginItem = itemContentState.value
        val loginItemValidationErrors = loginItem.validate()
        if (loginItemValidationErrors.isNotEmpty()) {
            loginItemValidationErrorsState.update { loginItemValidationErrors }
            return false
        }
        return true
    }

    private fun validateCustomFields(customFields: List<CustomFieldContent>): Pair<List<CustomFieldContent>, Boolean> {
        var hasCustomFieldErrors = false
        val fields = customFields.mapIndexed { idx, field ->
            when (field) {
                is CustomFieldContent.Hidden -> field
                is CustomFieldContent.Text -> field
                is CustomFieldContent.Totp -> {
                    val (validated, hasErrors) = validateTotpField(field, idx)
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

    private fun validateTotpField(
        field: CustomFieldContent.Totp,
        index: Int
    ): Pair<CustomFieldContent.Totp, Boolean> {
        val content = when (val hiddenState = field.value) {
            is HiddenState.Revealed -> hiddenState.clearText
            is HiddenState.Concealed -> {
                encryptionContextProvider.withEncryptionContext {
                    decrypt(hiddenState.encrypted)
                }
            }

            is HiddenState.Empty -> ""
        }

        if (content.isBlank()) {
            addValidationError(
                LoginItemValidationErrors.CustomFieldValidationError.EmptyField(index)
            )
            return field to true
        }

        val sanitized = sanitizeOTP(content)
            .fold(
                onSuccess = { it },
                onFailure = {
                    addValidationError(
                        LoginItemValidationErrors.CustomFieldValidationError.InvalidTotp(
                            index = index
                        )
                    )
                    return field to true
                }
            )

        val encryptedSanitized = encryptionContextProvider.withEncryptionContext {
            encrypt(sanitized)
        }

        return CustomFieldContent.Totp(
            label = field.label,
            value = when (field.value) {
                is HiddenState.Revealed -> {
                    HiddenState.Revealed(
                        encrypted = encryptedSanitized,
                        clearText = sanitized
                    )
                }

                is HiddenState.Concealed -> {
                    HiddenState.Concealed(encryptedSanitized)
                }

                is HiddenState.Empty -> HiddenState.Empty(encryptedSanitized)
            }
        ) to false
    }

    private fun sanitizeWebsites(websites: List<String>): List<String> =
        websites.map { url ->
            if (url.isBlank()) {
                ""
            } else {
                UrlSanitizer.sanitize(url).fold(
                    onSuccess = { it },
                    onFailure = { url }
                )
            }
        }

    private fun sanitizeOTP(otp: String): Result<String> {
        val isUri = otp.contains("://")
        return totpManager.parse(otp)
            .fold(
                onSuccess = { spec ->
                    val uri = totpManager.generateUri(spec)
                    Result.success(uri)
                },
                onFailure = {
                    // If is an URI, we require it to be valid. Otherwise we interpret it as secret
                    if (isUri) {
                        Result.failure(it)
                    } else {
                        totpManager.generateUriWithDefaults(otp)
                    }
                }
            )
    }

    fun onDeleteLinkedApp(packageInfo: PackageInfoUi) {
        onUserEditedContent()
        itemContentState.update {
            it.copy(packageInfoSet = it.packageInfoSet.minus(packageInfo.toPackageInfo()))
        }
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
                            val customFields = itemContentState.value.customFields
                            if (customFieldTOTP != null && customFields.size - 1 >= customFieldTOTP.index) {
                                val updatedCustomFields = customFields.toMutableList()
                                    .mapIndexed { index, customFieldContent ->
                                        if (
                                            customFieldContent is CustomFieldContent.Totp &&
                                            index == customFieldTOTP.index
                                        ) {
                                            customFieldContent.copy(
                                                value = HiddenState.Revealed(
                                                    encryptedContent,
                                                    sanitisedContent
                                                )
                                            )
                                        } else {
                                            customFieldContent
                                        }
                                    }
                                itemContentState.update {
                                    it.copy(customFields = updatedCustomFields)
                                }
                            }
                        }

                        else -> itemContentState.update {
                            it.copy(
                                primaryTotp = HiddenState.Revealed(
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
        draftRepository.delete<AliasItem>(CreateAliasViewModel.KEY_DRAFT_ALIAS)

        itemContentState.update { it.copy(username = "") }
        canUpdateUsernameState.update { true }
    }

    fun onCustomFieldChange(index: Int, value: String) = viewModelScope.launch {
        loginItemValidationErrorsState.update {
            it.toMutableSet().apply {
                remove(LoginItemValidationErrors.CustomFieldValidationError.EmptyField(index))
                remove(LoginItemValidationErrors.CustomFieldValidationError.InvalidTotp(index))
            }
        }

        itemContentState.update {
            val customFields = it.customFields.toMutableList()

            val updated = encryptionContextProvider.withEncryptionContext {
                when (val field = customFields[index]) {
                    is CustomFieldContent.Hidden -> {
                        CustomFieldContent.Hidden(
                            label = field.label,
                            value = HiddenState.Revealed(
                                encrypted = encrypt(value),
                                clearText = value
                            )
                        )
                    }

                    is CustomFieldContent.Text -> CustomFieldContent.Text(
                        label = field.label,
                        value = value
                    )

                    is CustomFieldContent.Totp -> CustomFieldContent.Totp(
                        label = field.label,
                        value = HiddenState.Revealed(
                            encrypted = encrypt(value),
                            clearText = value
                        )
                    )
                }
            }

            customFields[index] = updated
            it.copy(customFields = customFields.toPersistentList())
        }
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
                            itemContentState.update { content ->
                                encryptionContextProvider.withEncryptionContext {
                                    content.copy(
                                        password = HiddenState.Revealed(
                                            encryptedPassword,
                                            decrypt(encryptedPassword)
                                        )
                                    )
                                }
                            }
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
                            itemContentState.update { loginItem ->
                                val customFields = loginItem.customFields.toMutableList()
                                customFields.add(customField)
                                loginItem.copy(customFields = customFields.toPersistentList())
                            }
                            val index = itemContentState.value.customFields.size - 1
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

    fun onFocusChange(field: LoginField, isFocused: Boolean) {
        when (field) {
            LoginField.Password -> {
                itemContentState.update {
                    val password = encryptionContextProvider.withEncryptionContext {
                        decrypt(it.password.encrypted)
                    }
                    val passwordHiddenState = when {
                        isFocused -> HiddenState.Revealed(it.password.encrypted, password)
                        password.isBlank() -> HiddenState.Empty(it.password.encrypted)
                        else -> HiddenState.Concealed(it.password.encrypted)
                    }
                    it.copy(password = passwordHiddenState)
                }
            }

            LoginField.PrimaryTotp -> {
                itemContentState.update {
                    val primaryTotp = encryptionContextProvider.withEncryptionContext {
                        HiddenState.Revealed(
                            it.primaryTotp.encrypted,
                            decrypt(it.primaryTotp.encrypted)
                        )
                    }
                    it.copy(primaryTotp = primaryTotp)
                }
            }

            LoginField.Username,
            is LoginCustomField.CustomFieldHidden,
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

    private fun renameCustomField(indexTitle: CustomFieldIndexTitle) {
        itemContentState.update { loginItem ->
            val customFields = loginItem.customFields.toMutableList()
            val updated = when (val field = customFields[indexTitle.index]) {
                is CustomFieldContent.Hidden -> {
                    CustomFieldContent.Hidden(
                        label = indexTitle.title,
                        value = field.value
                    )
                }

                is CustomFieldContent.Text -> CustomFieldContent.Text(
                    label = indexTitle.title,
                    value = field.value
                )

                is CustomFieldContent.Totp -> CustomFieldContent.Totp(
                    label = indexTitle.title,
                    value = field.value
                )
            }
            customFields[indexTitle.index] = updated
            loginItem.copy(customFields = customFields.toPersistentList())
        }

        when (itemContentState.value.customFields.getOrNull(indexTitle.index)) {
            is CustomFieldContent.Hidden -> focusedFieldFlow.update {
                LoginCustomField.CustomFieldHidden(indexTitle.index).some()
            }

            is CustomFieldContent.Text -> focusedFieldFlow.update {
                LoginCustomField.CustomFieldText(indexTitle.index).some()
            }

            is CustomFieldContent.Totp -> focusedFieldFlow.update {
                LoginCustomField.CustomFieldTOTP(indexTitle.index).some()
            }

            null -> {}
        }
    }

    private fun removeCustomField(index: Int) = viewModelScope.launch {
        onUserEditedContent()
        itemContentState.update {
            val customFields = it.customFields.toMutableList()
            customFields.removeAt(index)
            it.copy(customFields = customFields.toPersistentList())
        }
    }

    private fun addValidationError(error: LoginItemValidationErrors) {
        loginItemValidationErrorsState.update { errors ->
            errors.toMutableSet().apply { add(error) }
        }
    }

    protected fun updatePrimaryTotpIfNeeded(
        navTotpUri: String?,
        navTotpIndex: Int?,
        currentValue: ItemContents.Login
    ) = navTotpUri
        ?.takeIf { navTotpIndex == -1 }
        ?.let { decrypted ->
            val encrypted =
                encryptionContextProvider.withEncryptionContext { encrypt(decrypted) }
            HiddenState.Revealed(encrypted, decrypted)
        }
        ?: currentValue.primaryTotp

    protected fun updateCustomFieldsIfNeeded(
        navTotpUri: String?,
        navTotpIndex: Int,
        currentValue: ItemContents.Login
    ) = if (navTotpUri != null) {
        navTotpUri
            .takeIf { navTotpIndex >= 0 }
            ?.let { decrypted ->
                currentValue.customFields
                    .mapIndexed { index, customFieldContent ->
                        if (
                            navTotpIndex == index &&
                            customFieldContent is CustomFieldContent.Totp
                        ) {
                            val encrypted = encryptionContextProvider.withEncryptionContext {
                                encrypt(decrypted)
                            }
                            val hiddenState = HiddenState.Revealed(encrypted, decrypted)
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

    companion object {
        private const val TAG = "BaseLoginViewModel"
    }
}

