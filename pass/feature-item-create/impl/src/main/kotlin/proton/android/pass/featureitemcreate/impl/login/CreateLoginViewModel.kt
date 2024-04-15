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
import proton.android.pass.commonrust.api.passwords.strengths.PasswordStrengthCalculator
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.commonuimodels.api.UIPasskeyContent
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.errors.AliasRateLimitError
import proton.android.pass.data.api.errors.CannotCreateMoreAliasesError
import proton.android.pass.data.api.errors.EmailNotValidatedError
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.usecases.CreateItem
import proton.android.pass.data.api.usecases.CreateItemAndAlias
import proton.android.pass.data.api.usecases.ObserveCurrentUser
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.defaultvault.ObserveDefaultVault
import proton.android.pass.domain.CustomField
import proton.android.pass.domain.ItemType
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.domain.entity.NewAlias
import proton.android.pass.featureitemcreate.impl.ItemCreate
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.MFACreated
import proton.android.pass.featureitemcreate.impl.alias.AliasItemFormState
import proton.android.pass.featureitemcreate.impl.alias.AliasMailboxUiModel
import proton.android.pass.featureitemcreate.impl.alias.CreateAliasViewModel
import proton.android.pass.featureitemcreate.impl.common.OptionShareIdSaver
import proton.android.pass.featureitemcreate.impl.common.ShareUiState
import proton.android.pass.featureitemcreate.impl.common.UIHiddenState
import proton.android.pass.featureitemcreate.impl.common.getShareUiStateFlow
import proton.android.pass.featureitemcreate.impl.login.LoginSnackbarMessages.AliasRateLimited
import proton.android.pass.featureitemcreate.impl.login.LoginSnackbarMessages.CannotCreateMoreAliases
import proton.android.pass.featureitemcreate.impl.login.LoginSnackbarMessages.EmailNotValidated
import proton.android.pass.featureitemcreate.impl.login.LoginSnackbarMessages.ItemCreationError
import proton.android.pass.featureitemcreate.impl.login.LoginSnackbarMessages.LoginCreated
import proton.android.pass.inappreview.api.InAppReviewTriggerMetrics
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.passkeys.api.GeneratePasskey
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import proton.android.pass.totp.api.TotpManager
import javax.inject.Inject

@Suppress("LongParameterList")
@HiltViewModel
class CreateLoginViewModel @Inject constructor(
    private val createItem: CreateItem,
    private val createItemAndAlias: CreateItemAndAlias,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val telemetryManager: TelemetryManager,
    private val draftRepository: DraftRepository,
    private val inAppReviewTriggerMetrics: InAppReviewTriggerMetrics,
    private val generatePasskey: GeneratePasskey,
    passwordStrengthCalculator: PasswordStrengthCalculator,
    accountManager: AccountManager,
    clipboardManager: ClipboardManager,
    totpManager: TotpManager,
    observeCurrentUser: ObserveCurrentUser,
    observeUpgradeInfo: ObserveUpgradeInfo,
    observeVaults: ObserveVaultsWithItemCount,
    savedStateHandleProvider: SavedStateHandleProvider,
    observeDefaultVault: ObserveDefaultVault
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
    savedStateHandleProvider = savedStateHandleProvider
) {
    private val navShareId: Option<ShareId> =
        savedStateHandleProvider.get().get<String>(CommonOptionalNavArgId.ShareId.key)
            .toOption()
            .map { ShareId(it) }
    private val initialUsername: Option<String> = savedStateHandleProvider.get()
        .get<String>(CreateLoginDefaultUsernameArg.key)
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

    val createLoginUiState: StateFlow<CreateLoginUiState> = combine(
        shareUiState,
        baseLoginUiState,
        createPasskeyStateFlow,
        ::CreateLoginUiState
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CreateLoginUiState.Initial
    )

    fun changeVault(shareId: ShareId) = viewModelScope.launch {
        selectedShareIdMutableState = Some(shareId)
    }

    @Suppress("ComplexMethod", "LongMethod")
    fun setInitialContents(initialContents: InitialCreateLoginUiState) {

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

        val username = when {
            initialContents.username != null -> initialContents.username
            initialContents.aliasItemFormState?.aliasToBeCreated != null ->
                initialContents.aliasItemFormState.aliasToBeCreated

            initialUsername is Some -> initialUsername.value
            else -> currentValue.username
        }

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
            websites.add(passkeyData.origin)
            createPasskeyStateFlow.update {
                CreatePasskeyState(
                    domain = passkeyData.domain,
                    username = initialContents.username ?: ""
                ).some()
            }
        }


        loginItemFormMutableState = loginItemFormState.copy(
            title = initialContents.title ?: currentValue.title,
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

    fun createItem() = viewModelScope.launch(coroutineExceptionHandler) {
        val shouldCreate = validateItem(None, emptyList())
        if (!shouldCreate) return@launch

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

        runCatching {
            createItemAndAlias(
                userId = userId,
                shareId = shareId,
                itemContents = loginItemFormState.toItemContents(),
                newAlias = NewAlias(
                    title = aliasItemFormState.title,
                    note = aliasItemFormState.note,
                    prefix = aliasItemFormState.prefix,
                    suffix = aliasItemFormState.selectedSuffix.toDomain(),
                    mailboxes = aliasItemFormState.mailboxes
                        .filter { it.selected }
                        .map { it.model }
                        .map(AliasMailboxUiModel::toDomain)
                )
            )
        }.onSuccess { item ->
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
            snackbarDispatcher(LoginCreated)
        }.onFailure {
            when (it) {
                is CannotCreateMoreAliasesError -> snackbarDispatcher(CannotCreateMoreAliases)
                is EmailNotValidatedError -> snackbarDispatcher(EmailNotValidated)
                is AliasRateLimitError -> snackbarDispatcher(AliasRateLimited)
                else -> snackbarDispatcher(ItemCreationError)
            }
            PassLogger.w(TAG, "Could not create item")
            PassLogger.w(TAG, it)
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
                itemContents = loginItemFormState.toItemContents()
            )
        }.onSuccess { item ->
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
            snackbarDispatcher(LoginCreated)
        }.onFailure {
            PassLogger.w(TAG, "Could not create item")
            PassLogger.w(TAG, it)
            snackbarDispatcher(ItemCreationError)
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

    companion object {
        private const val TAG = "CreateLoginViewModel"
    }
}
