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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.some
import proton.android.pass.common.api.toOption
import proton.android.pass.commonpresentation.api.attachments.AttachmentsHandler
import proton.android.pass.commonrust.api.EmailValidator
import proton.android.pass.commonrust.api.passwords.strengths.PasswordStrengthCalculator
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonuimodels.api.PackageInfoUi
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.errors.AliasRateLimitError
import proton.android.pass.data.api.errors.CannotCreateMoreAliasesError
import proton.android.pass.data.api.errors.EmailNotValidatedError
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.usecases.CreateItem
import proton.android.pass.data.api.usecases.CreateLoginAndAlias
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.attachments.LinkAttachmentsToItem
import proton.android.pass.data.api.usecases.defaultvault.ObserveDefaultVault
import proton.android.pass.data.api.usecases.tooltips.DisableTooltip
import proton.android.pass.data.api.usecases.tooltips.ObserveTooltipEnabled
import proton.android.pass.data.api.work.WorkerItem
import proton.android.pass.data.api.work.WorkerLauncher
import proton.android.pass.domain.CustomField
import proton.android.pass.domain.HiddenState
import proton.android.pass.domain.ItemContents
import proton.android.pass.domain.ItemId
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.domain.entity.NewAlias
import proton.android.pass.domain.toItemContents
import proton.android.pass.features.itemcreate.ItemCreate
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.MFACreated
import proton.android.pass.features.itemcreate.R
import proton.android.pass.features.itemcreate.alias.AliasItemFormState
import proton.android.pass.features.itemcreate.alias.AliasMailboxUiModel
import proton.android.pass.features.itemcreate.alias.CreateAliasViewModel
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepository
import proton.android.pass.features.itemcreate.common.OptionShareIdSaver
import proton.android.pass.features.itemcreate.common.ShareUiState
import proton.android.pass.features.itemcreate.common.UICustomFieldContent
import proton.android.pass.features.itemcreate.common.UIHiddenState
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldHandler
import proton.android.pass.features.itemcreate.common.formprocessor.LoginItemFormProcessorType
import proton.android.pass.features.itemcreate.common.getShareUiStateFlow
import proton.android.pass.features.itemcreate.login.LoginSnackbarMessages.AliasRateLimited
import proton.android.pass.features.itemcreate.login.LoginSnackbarMessages.CannotCreateMoreAliases
import proton.android.pass.features.itemcreate.login.LoginSnackbarMessages.EmailNotValidated
import proton.android.pass.features.itemcreate.login.LoginSnackbarMessages.ItemCreationError
import proton.android.pass.features.itemcreate.login.LoginSnackbarMessages.ItemLinkAttachmentsError
import proton.android.pass.features.itemcreate.login.LoginSnackbarMessages.LoginCreated
import proton.android.pass.inappreview.api.InAppReviewTriggerMetrics
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.passkeys.api.GeneratePasskey
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import proton.android.pass.totp.api.TotpManager
import javax.inject.Inject

@Suppress("LongParameterList")
@HiltViewModel
class CreateLoginViewModel @Inject constructor(
    private val createItem: CreateItem,
    private val createLoginAndAlias: CreateLoginAndAlias,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val telemetryManager: TelemetryManager,
    private val draftRepository: DraftRepository,
    private val inAppReviewTriggerMetrics: InAppReviewTriggerMetrics,
    private val generatePasskey: GeneratePasskey,
    private val workerLauncher: WorkerLauncher,
    private val linkAttachmentsToItem: LinkAttachmentsToItem,
    private val getItemById: GetItemById,
    featureFlagsRepository: FeatureFlagsPreferencesRepository,
    passwordStrengthCalculator: PasswordStrengthCalculator,
    accountManager: AccountManager,
    clipboardManager: ClipboardManager,
    totpManager: TotpManager,
    observeCurrentUser: ObserveCurrentUser,
    observeUpgradeInfo: ObserveUpgradeInfo,
    observeVaults: ObserveVaultsWithItemCount,
    observeDefaultVault: ObserveDefaultVault,
    emailValidator: EmailValidator,
    observeTooltipEnabled: ObserveTooltipEnabled,
    disableTooltip: DisableTooltip,
    attachmentsHandler: AttachmentsHandler,
    customFieldHandler: CustomFieldHandler,
    userPreferencesRepository: UserPreferencesRepository,
    customFieldDraftRepository: CustomFieldDraftRepository,
    loginItemFormProcessor: LoginItemFormProcessorType,
    savedStateHandleProvider: SavedStateHandleProvider
) : BaseLoginViewModel(
    accountManager = accountManager,
    snackbarDispatcher = snackbarDispatcher,
    clipboardManager = clipboardManager,
    totpManager = totpManager,
    observeCurrentUser = observeCurrentUser,
    observeUpgradeInfo = observeUpgradeInfo,
    draftRepository = draftRepository,
    encryptionContextProvider = encryptionContextProvider,
    passwordStrengthCalculator = passwordStrengthCalculator,
    emailValidator = emailValidator,
    observeTooltipEnabled = observeTooltipEnabled,
    disableTooltip = disableTooltip,
    userPreferencesRepository = userPreferencesRepository,
    attachmentsHandler = attachmentsHandler,
    customFieldHandler = customFieldHandler,
    featureFlagsRepository = featureFlagsRepository,
    customFieldDraftRepository = customFieldDraftRepository,
    loginItemFormProcessor = loginItemFormProcessor,
    savedStateHandleProvider = savedStateHandleProvider
) {
    private val navShareId: Option<ShareId> = savedStateHandleProvider.get()
        .get<String>(CommonOptionalNavArgId.ShareId.key)
        .toOption()
        .map(::ShareId)

    private val navItemId: Option<ItemId> = savedStateHandleProvider.get()
        .get<String>(CommonOptionalNavArgId.ItemId.key)
        .toOption()
        .map(::ItemId)

    private val initialEmail: Option<String> = savedStateHandleProvider.get()
        .get<String>(CreateLoginDefaultEmailArg.key)
        .toOption()

    @OptIn(SavedStateHandleSaveableApi::class)
    private var _generatePasskeyData: Option<GeneratePasskeyData> by savedStateHandleProvider.get()
        .saveable(stateSaver = GeneratePasskeyDataStateSaver) { mutableStateOf(None) }

    private val createPasskeyStateFlow: MutableStateFlow<Option<CreatePasskeyState>> =
        MutableStateFlow(None)

    private val generatePasskeyData: Option<GeneratePasskeyData> get() = _generatePasskeyData

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.w(TAG, throwable)
    }

    @OptIn(SavedStateHandleSaveableApi::class)
    private var selectedShareIdMutableState: Option<ShareId> by savedStateHandleProvider.get()
        .saveable(stateSaver = OptionShareIdSaver) { mutableStateOf(None) }
    private val selectedShareIdState: Flow<Option<ShareId>> =
        snapshotFlow { selectedShareIdMutableState }
            .filterNotNull()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = None
            )

    private val observeAllVaultsFlow: Flow<List<VaultWithItemCount>> =
        observeVaults().distinctUntilChanged()

    private val shareUiState: StateFlow<ShareUiState> = getShareUiStateFlow(
        navShareIdState = flowOf(navShareId),
        selectedShareIdState = selectedShareIdState,
        observeAllVaultsFlow = observeAllVaultsFlow.asLoadingResult(),
        observeDefaultVaultFlow = observeDefaultVault().asLoadingResult(),
        viewModelScope = viewModelScope,
        tag = TAG
    )

    internal val createLoginUiState: StateFlow<CreateLoginUiState> = combine(
        shareUiState,
        baseLoginUiState,
        createPasskeyStateFlow,
        ::CreateLoginUiState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CreateLoginUiState.Initial
    )

    internal fun changeVault(shareId: ShareId) {
        selectedShareIdMutableState = Some(shareId)
    }

    internal suspend fun duplicateContents(context: Context) {
        val shareId = navShareId.value() ?: return
        val itemId = navItemId.value() ?: return
        val item = getItemById(shareId = shareId, itemId = itemId)

        val currentValue = loginItemFormState
        encryptionContextProvider.withEncryptionContextSuspendable {
            val itemContents = item.toItemContents<ItemContents.Login> { decrypt(it) }
            val customFields = itemContents.customFields.map(UICustomFieldContent.Companion::from)
            val passwordHiddenState = when (val hiddenState = itemContents.password) {
                is HiddenState.Empty -> UIHiddenState.Empty(hiddenState.encrypted)
                is HiddenState.Concealed,
                is HiddenState.Revealed -> UIHiddenState.Concealed(hiddenState.encrypted)
            }
            loginItemFormMutableState = currentValue.copy(
                title = context.getString(R.string.title_duplicate, decrypt(item.title)),
                note = decrypt(item.note),
                email = itemContents.itemEmail,
                username = itemContents.itemUsername,
                password = passwordHiddenState,
                passwordStrength = passwordStrengthCalculator.calculateStrength(
                    password = decrypt(itemContents.password.encrypted)
                ),
                urls = itemContents.urls.ifEmpty { listOf("") },
                packageInfoSet = itemContents.packageInfoSet.map { PackageInfoUi(it) }.toSet(),
                primaryTotp = UIHiddenState.Revealed(
                    encrypted = itemContents.primaryTotp.encrypted,
                    clearText = decrypt(itemContents.primaryTotp.encrypted)
                ),
                customFields = customFieldHandler.sanitiseForEditingCustomFields(customFields),
                passkeys = itemContents.passkeys.map { UIPasskeyContent.from(it) }
            )
        }
    }

    @Suppress("ComplexMethod", "LongMethod")
    internal fun setInitialContents(initialContents: InitialCreateLoginUiState) {
        val currentValue = loginItemFormState
        val websites = currentValue.urls.toMutableList()

        if (initialContents.url != null) {
            // Check if we are in the initial state, and if so, clear the list
            if (websites.size == 1 && websites.first().isEmpty()) {
                websites.clear()
                websites.add(initialContents.url)
            } else if (!websites.contains(initialContents.url)) {
                websites.add(initialContents.url)
            }
        }
        aliasLocalItemState.update { initialContents.aliasItemFormState.toOption() }

        val email = when {
            initialContents.email != null -> initialContents.email
            initialContents.aliasItemFormState?.aliasToBeCreated != null ->
                initialContents.aliasItemFormState.aliasToBeCreated

            initialEmail is Some -> initialEmail.value
            else -> currentValue.email
        }

        val username = initialContents.username ?: currentValue.username

        if (initialContents.aliasItemFormState?.aliasToBeCreated?.isNotEmpty() == true) {
            canUpdateUsernameState.update { false }
        }

        val packageInfoSet = if (initialContents.packageInfoUi != null) {
            currentValue.packageInfoSet.toMutableSet()
                .apply { add(initialContents.packageInfoUi) }
                .toImmutableSet()
        } else {
            currentValue.packageInfoSet
        }

        val password = initialContents.password
            ?.let { password ->
                encryptionContextProvider.withEncryptionContext {
                    UIHiddenState.Concealed(encrypt(password))
                }
            }
            ?: currentValue.password
        val primaryTotp = updatePrimaryTotpIfNeeded(
            navTotpUri = initialContents.navTotpUri,
            navTotpIndex = initialContents.navTotpIndex,
            currentValue = currentValue
        )
        val customFields = updateCustomFieldsIfNeeded(
            navTotpUri = initialContents.navTotpUri,
            navTotpIndex = initialContents.navTotpIndex,
            currentValue = currentValue
        )

        initialContents.passkeyData?.let { passkeyData ->
            _generatePasskeyData = GeneratePasskeyData(
                origin = passkeyData.origin,
                request = passkeyData.request
            ).some()
            if (!websites.contains(passkeyData.origin)) {
                websites.add(passkeyData.origin)
            }
            createPasskeyStateFlow.update {
                CreatePasskeyState(
                    domain = passkeyData.domain,
                    username = initialContents.username.orEmpty()
                ).some()
            }
        }

        loginItemFormMutableState = loginItemFormState.copy(
            title = initialContents.title ?: currentValue.title,
            email = email,
            username = username,
            password = password,
            passwordStrength = currentValue.passwordStrength,
            urls = websites,
            packageInfoSet = packageInfoSet,
            primaryTotp = primaryTotp,
            customFields = customFields,
            passkeys = emptyList()
        )
    }

    internal fun createItem() = viewModelScope.launch(coroutineExceptionHandler) {
        if (!isFormStateValid()) return@launch
        isLoadingState.update { IsLoadingState.Loading }
        val vault = when (val state = shareUiState.value) {
            is ShareUiState.Error -> null
            ShareUiState.Loading -> null
            ShareUiState.NotInitialised -> null
            is ShareUiState.Success -> state.currentVault
        }
        val userId = accountManager.getPrimaryUserId()
            .firstOrNull { userId -> userId != null }

        val generatedPasskey = when (val data = generatePasskeyData) {
            None -> None
            is Some -> {
                PassLogger.i(TAG, "Generating passkey for [origin=${data.value.origin}]")
                runCatching {
                    val generatedPasskey = generatePasskey(
                        url = data.value.origin,
                        request = data.value.request
                    )
                    loginItemFormMutableState = loginItemFormMutableState.copy(
                        passkeyToBeGenerated = UIPasskeyContent.from(generatedPasskey.passkey)
                    )
                    generatedPasskey.some()
                }.getOrElse {
                    PassLogger.w(TAG, "Error generating passkey")
                    PassLogger.w(TAG, it)
                    snackbarDispatcher(ItemCreationError)
                    isLoadingState.update { IsLoadingState.NotLoading }
                    return@launch
                }
            }
        }

        if (userId != null && vault != null) {
            val aliasItemOption = aliasLocalItemState.value
            if (aliasItemOption is Some) {
                performCreateItemAndAlias(
                    userId = userId,
                    shareId = vault.vault.shareId,
                    aliasItemFormState = aliasItemOption.value,
                    passkeyResponse = generatedPasskey.map { it.response }
                )
            } else {
                performCreateItem(
                    userId = userId,
                    shareId = vault.vault.shareId,
                    passkeyResponse = generatedPasskey.map { it.response }
                )
            }
        } else {
            snackbarDispatcher(ItemCreationError)
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    @Suppress("LongMethod")
    private suspend fun performCreateItemAndAlias(
        userId: UserId,
        shareId: ShareId,
        aliasItemFormState: AliasItemFormState,
        passkeyResponse: Option<String>
    ) {
        val selectedSuffix = aliasItemFormState.selectedSuffix
        if (selectedSuffix == null) {
            val message = "Empty suffix on create alias"
            PassLogger.w(TAG, message)
            snackbarDispatcher(ItemCreationError)
            return
        }

        val contents = loginItemFormState.toItemContents(emailValidator = emailValidator)
        runCatching {
            createLoginAndAlias(
                userId = userId,
                shareId = shareId,
                itemContents = contents,
                newAlias = NewAlias(
                    contents = aliasItemFormState.toItemContents(),
                    prefix = aliasItemFormState.prefix,
                    suffix = aliasItemFormState.selectedSuffix.toDomain(),
                    aliasName = aliasItemFormState.senderName,
                    mailboxes = aliasItemFormState.selectedMailboxes
                        .map(AliasMailboxUiModel::toDomain)
                )
            )
        }
            .onFailure {
                when (it) {
                    is CannotCreateMoreAliasesError -> snackbarDispatcher(CannotCreateMoreAliases)
                    is EmailNotValidatedError -> snackbarDispatcher(EmailNotValidated)
                    is AliasRateLimitError -> snackbarDispatcher(AliasRateLimited)
                    else -> snackbarDispatcher(ItemCreationError)
                }
                PassLogger.w(TAG, "Could not create item")
                PassLogger.w(TAG, it)
            }
            .onSuccess { item ->
                snackbarDispatcher(LoginCreated)
                runCatching {
                    linkAttachmentsToItem(item.shareId, item.id, item.revision)
                }.onFailure {
                    PassLogger.w(TAG, "Link attachment error")
                    PassLogger.w(TAG, it)
                    snackbarDispatcher(ItemLinkAttachmentsError)
                }
                launchUpdateAssetLinksWorker(contents.urls.toSet())
                inAppReviewTriggerMetrics.incrementItemCreatedCount()
                when (passkeyResponse) {
                    None -> {
                        isItemSavedState.update {
                            encryptionContextProvider.withEncryptionContext {
                                ItemSavedState.Success(
                                    item.id,
                                    item.toUiModel(this@withEncryptionContext)
                                )
                            }
                        }
                    }

                    is Some -> {
                        isItemSavedState.update {
                            ItemSavedState.SuccessWithPasskeyResponse(passkeyResponse.value)
                        }
                    }
                }

                telemetryManager.sendEvent(ItemCreate(EventItemType.Alias))
                telemetryManager.sendEvent(ItemCreate(EventItemType.Login))
                send2FACreatedTelemetryEvent(item.itemType as ItemType.Login)
                draftRepository.delete<AliasItemFormState>(CreateAliasViewModel.KEY_DRAFT_ALIAS)
            }
    }

    private suspend fun performCreateItem(
        userId: UserId,
        shareId: ShareId,
        passkeyResponse: Option<String>
    ) {
        runCatching {
            createItem(
                userId = userId,
                shareId = shareId,
                itemContents = loginItemFormState.toItemContents(emailValidator = emailValidator)
            )
        }
            .onFailure {
                PassLogger.w(TAG, "Could not create item")
                PassLogger.w(TAG, it)
                snackbarDispatcher(ItemCreationError)
            }
            .onSuccess { item ->
                snackbarDispatcher(LoginCreated)
                runCatching {
                    linkAttachmentsToItem(item.shareId, item.id, item.revision)
                }.onFailure {
                    PassLogger.w(TAG, "Link attachment error")
                    PassLogger.w(TAG, it)
                    snackbarDispatcher(ItemLinkAttachmentsError)
                }

                inAppReviewTriggerMetrics.incrementItemCreatedCount()

                when (passkeyResponse) {
                    None -> {
                        isItemSavedState.update {
                            encryptionContextProvider.withEncryptionContext {
                                ItemSavedState.Success(
                                    item.id,
                                    item.toUiModel(this@withEncryptionContext)
                                )
                            }
                        }
                    }

                    is Some -> {
                        isItemSavedState.update {
                            ItemSavedState.SuccessWithPasskeyResponse(passkeyResponse.value)
                        }
                    }
                }
                telemetryManager.sendEvent(ItemCreate(EventItemType.Login))
                send2FACreatedTelemetryEvent(item.itemType as ItemType.Login)
            }
    }

    private fun send2FACreatedTelemetryEvent(login: ItemType.Login) {
        if (login.customFields.any { it is CustomField.Totp }) {
            telemetryManager.sendEvent(MFACreated)
        } else {
            encryptionContextProvider.withEncryptionContext {
                if (decrypt(login.primaryTotp).isNotBlank()) {
                    telemetryManager.sendEvent(MFACreated)
                }
            }
        }
    }

    private suspend fun launchUpdateAssetLinksWorker(websites: Set<String>) {
        if (isDALEnabled()) {
            workerLauncher.launch(WorkerItem.SingleItemAssetLink(websites))
        }
    }

    private companion object {

        private const val TAG = "CreateLoginViewModel"

    }

}
