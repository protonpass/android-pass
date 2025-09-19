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

package proton.android.pass.features.itemcreate.alias

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.clipboard.api.ClipboardManager
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.toOption
import proton.android.pass.commonpresentation.api.attachments.AttachmentsHandler
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.errors.AliasRateLimitError
import proton.android.pass.data.api.errors.CannotCreateMoreAliasesError
import proton.android.pass.data.api.errors.EmailNotValidatedError
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.CreateAlias
import proton.android.pass.data.api.usecases.ObserveAliasOptions
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.attachments.LinkAttachmentsToItem
import proton.android.pass.data.api.usecases.defaultvault.ObserveDefaultVault
import proton.android.pass.domain.AliasOptions
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.domain.entity.NewAlias
import proton.android.pass.features.itemcreate.ItemCreate
import proton.android.pass.features.itemcreate.ItemSavedState
import proton.android.pass.features.itemcreate.alias.AliasSnackbarMessage.AliasCreated
import proton.android.pass.features.itemcreate.alias.AliasSnackbarMessage.ItemCreationError
import proton.android.pass.features.itemcreate.alias.AliasSnackbarMessage.ItemLinkAttachmentsError
import proton.android.pass.features.itemcreate.alias.draftrepositories.MailboxDraftRepository
import proton.android.pass.features.itemcreate.alias.draftrepositories.SuffixDraftRepository
import proton.android.pass.features.itemcreate.common.AliasItemValidationError
import proton.android.pass.features.itemcreate.common.CommonFieldValidationError
import proton.android.pass.features.itemcreate.common.CustomFieldDraftRepository
import proton.android.pass.features.itemcreate.common.OptionShareIdSaver
import proton.android.pass.features.itemcreate.common.ShareUiState
import proton.android.pass.features.itemcreate.common.customfields.CustomFieldHandler
import proton.android.pass.features.itemcreate.common.formprocessor.AliasItemFormProcessorType
import proton.android.pass.features.itemcreate.common.getShareUiStateFlow
import proton.android.pass.inappreview.api.InAppReviewTriggerMetrics
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.UserPreferencesRepository
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import javax.inject.Inject

@Suppress("LongParameterList")
@HiltViewModel
open class CreateAliasViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val createAlias: CreateAlias,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val telemetryManager: TelemetryManager,
    protected val draftRepository: DraftRepository,
    private val inAppReviewTriggerMetrics: InAppReviewTriggerMetrics,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val linkAttachmentsToItem: LinkAttachmentsToItem,
    private val mailboxDraftRepository: MailboxDraftRepository,
    private val suffixDraftRepository: SuffixDraftRepository,
    userPreferencesRepository: UserPreferencesRepository,
    observeAliasOptions: ObserveAliasOptions,
    observeVaults: ObserveVaultsWithItemCount,
    observeUpgradeInfo: ObserveUpgradeInfo,
    observeDefaultVault: ObserveDefaultVault,
    attachmentsHandler: AttachmentsHandler,
    customFieldHandler: CustomFieldHandler,
    customFieldDraftRepository: CustomFieldDraftRepository,
    canPerformPaidAction: CanPerformPaidAction,
    aliasItemFormProcessor: AliasItemFormProcessorType,
    clipboardManager: ClipboardManager,
    savedStateHandleProvider: SavedStateHandleProvider
) : BaseAliasViewModel(
    mailboxDraftRepository = mailboxDraftRepository,
    suffixDraftRepository = suffixDraftRepository,
    userPreferencesRepository = userPreferencesRepository,
    attachmentsHandler = attachmentsHandler,
    snackbarDispatcher = snackbarDispatcher,
    customFieldHandler = customFieldHandler,
    customFieldDraftRepository = customFieldDraftRepository,
    canPerformPaidAction = canPerformPaidAction,
    aliasItemFormProcessor = aliasItemFormProcessor,
    clipboardManager = clipboardManager,
    encryptionContextProvider = encryptionContextProvider,
    savedStateHandleProvider = savedStateHandleProvider
) {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.w(TAG, throwable)
    }

    private val navShareId: Option<ShareId> =
        savedStateHandleProvider.get().get<String>(CommonOptionalNavArgId.ShareId.key)
            .toOption()
            .map { ShareId(it) }

    @OptIn(SavedStateHandleSaveableApi::class)
    private var selectedShareIdMutableState: Option<ShareId> by savedStateHandleProvider.get()
        .saveable(stateSaver = OptionShareIdSaver) { mutableStateOf(None) }
    private val selectedShareIdState: StateFlow<Option<ShareId>> =
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
        viewModelScope = viewModelScope,
        observeDefaultVaultFlow = observeDefaultVault().asLoadingResult(),
        tag = TAG
    )

    private val aliasOptionsState: Flow<LoadingResult<AliasOptionsUiModel>> = shareUiState
        .map { state ->
            when (state) {
                is ShareUiState.Success -> state.currentVault.vault.shareId to state
                else -> null to state
            }
        }
        .distinctUntilChangedBy { it.first }
        .flatMapLatest { (shareId, state) ->
            when (state) {
                is ShareUiState.Error -> flowOf(LoadingResult.Error(RuntimeException()))
                ShareUiState.Loading, ShareUiState.NotInitialised -> flowOf(LoadingResult.Loading)
                is ShareUiState.Success ->
                    if (shareId == null) {
                        flowOf(LoadingResult.Loading)
                    } else {
                        observeAliasOptions(shareId)
                            .distinctUntilChanged()
                            .onEach(::setupMailboxesAndSuffixes)
                            .map(::AliasOptionsUiModel)
                            .asLoadingResult()
                    }
            }
        }
        .onEach {
            when (it) {
                is LoadingResult.Error -> {
                    PassLogger.w(TAG, "Error loading AliasOptions")
                    PassLogger.w(TAG, it.exception)
                    isLoadingState.update { IsLoadingState.NotLoading }
                    snackbarDispatcher(AliasSnackbarMessage.CannotRetrieveAliasOptions)
                    mutableCloseScreenEventFlow.update { CloseScreenEvent.Close }
                }

                LoadingResult.Loading -> isLoadingState.update { IsLoadingState.Loading }
                is LoadingResult.Success -> {
                    isLoadingState.update { IsLoadingState.NotLoading }
                    isApplyButtonEnabledState.update { IsButtonEnabled.Enabled }
                }
            }
        }
        .distinctUntilChanged()

    internal val createAliasUiState: StateFlow<CreateAliasUiState> = combineN(
        baseAliasUiState,
        shareUiState,
        aliasOptionsState,
        mailboxDraftRepository.getSelectedMailboxFlow(),
        suffixDraftRepository.getSelectedSuffixFlow(),
        observeUpgradeInfo().asLoadingResult()
    ) { aliasUiState, shareUiState, aliasOptionsResult, selectedMailboxes,
        selectedSuffix, upgradeInfoResult ->
        val hasReachedAliasLimit = upgradeInfoResult.getOrNull()?.hasReachedAliasLimit() ?: false
        val canUpgrade = upgradeInfoResult.getOrNull()?.isUpgradeAvailable ?: false

        aliasItemFormMutableState = when (aliasOptionsResult) {
            is LoadingResult.Error -> aliasItemFormState
            LoadingResult.Loading -> aliasItemFormState
            is LoadingResult.Success -> {
                val suffixUiModel = selectedSuffix.map(::AliasSuffixUiModel)
                val mailboxes = selectedMailboxes.map(::AliasMailboxUiModel).toSet()
                val aliasToBeCreated = if (aliasItemFormState.prefix.isNotBlank()) {
                    getAliasToBeCreated(aliasItemFormState.prefix, suffixUiModel.value())
                } else {
                    aliasItemFormState.aliasToBeCreated
                }
                aliasItemFormState.copy(
                    aliasOptions = aliasOptionsResult.data,
                    selectedSuffix = suffixUiModel.value(),
                    selectedMailboxes = mailboxes,
                    aliasToBeCreated = aliasToBeCreated
                )
            }
        }

        CreateAliasUiState(
            baseAliasUiState = aliasUiState.copy(
                hasReachedAliasLimit = hasReachedAliasLimit,
                canUpgrade = canUpgrade,
                isLoadingState = IsLoadingState.from(
                    aliasUiState.isLoadingState is IsLoadingState.Loading ||
                        upgradeInfoResult is LoadingResult.Loading
                )
            ),
            shareUiState = shareUiState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CreateAliasUiState.Initial
    )

    protected var titlePrefixInSync = true

    override fun onTitleChange(value: String) {
        onUserEditedContent()
        val prefix = if (titlePrefixInSync) {
            AliasUtils.formatAlias(value)
        } else {
            aliasItemFormMutableState.prefix
        }.take(AliasItemFormState.MAX_PREFIX_LENGTH)
        aliasItemFormMutableState = aliasItemFormMutableState.copy(
            title = value,
            prefix = prefix,
            aliasToBeCreated = getAliasToBeCreated(
                alias = prefix,
                suffix = aliasItemFormMutableState.selectedSuffix
            )
        )

        aliasItemValidationErrorsState.update {
            it.toMutableSet()
                .apply {
                    remove(CommonFieldValidationError.BlankTitle)
                    remove(AliasItemValidationError.InvalidAliasContent)
                }
        }
    }

    fun onPrefixChange(value: String) {
        if (value.contains(" ") || value.contains("\n")) return

        val prefix = AliasUtils.formatAlias(value.take(AliasItemFormState.MAX_PREFIX_LENGTH))
        if (prefix == aliasItemFormMutableState.prefix) return

        onUserEditedContent()
        aliasItemFormMutableState = aliasItemFormMutableState.copy(
            prefix = prefix,
            aliasToBeCreated = getAliasToBeCreated(
                alias = prefix,
                suffix = aliasItemFormMutableState.selectedSuffix
            )
        )
        aliasItemValidationErrorsState.update {
            it.toMutableSet()
                .apply {
                    remove(AliasItemValidationError.BlankPrefix)
                    remove(AliasItemValidationError.InvalidAliasContent)
                }
        }
        titlePrefixInSync = false
    }

    fun createAlias(shareId: ShareId) = viewModelScope.launch(coroutineExceptionHandler) {
        val aliasItem = aliasItemFormState
        if (aliasItem.selectedSuffix == null) {
            PassLogger.w(TAG, "Cannot create alias as SelectedSuffix is null")
            return@launch
        }

        if (!isFormStateValid()) return@launch

        if (isDraft) {
            PassLogger.d(TAG, "Creating draft alias")
            draftRepository.save(KEY_DRAFT_ALIAS, aliasItem)
            isAliasDraftSavedState.tryEmit(AliasDraftSavedState.Success(shareId, aliasItem))
        } else {
            PassLogger.d(TAG, "Performing create alias")
            isLoadingState.update { IsLoadingState.Loading }
            performCreateAlias(
                shareId,
                aliasItem,
                aliasItem.selectedSuffix,
                aliasItem.selectedMailboxes
            )
            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    private suspend fun performCreateAlias(
        shareId: ShareId,
        aliasItemFormState: AliasItemFormState,
        aliasSuffix: AliasSuffixUiModel,
        mailboxes: Set<AliasMailboxUiModel>
    ) {
        val userId = accountManager.getPrimaryUserId().first { userId -> userId != null }
        if (userId != null) {
            runCatching {
                createAlias(
                    userId = userId,
                    shareId = shareId,
                    newAlias = NewAlias(
                        contents = aliasItemFormState.toItemContents(),
                        prefix = aliasItemFormState.prefix,
                        aliasName = aliasItemFormState.senderName,
                        suffix = aliasSuffix.toDomain(),
                        mailboxes = mailboxes.map(AliasMailboxUiModel::toDomain)
                    )
                )
            }
                .onFailure { onCreateAliasError(it) }
                .onSuccess { item ->
                    snackbarDispatcher(AliasCreated)
                    runCatching {
                        linkAttachmentsToItem(item.shareId, item.id, item.revision)
                    }.onFailure {
                        PassLogger.w(TAG, "Link attachment error")
                        PassLogger.w(TAG, it)
                        snackbarDispatcher(ItemLinkAttachmentsError)
                    }

                    inAppReviewTriggerMetrics.incrementItemCreatedCount()
                    val itemUiModel = encryptionContextProvider.withEncryptionContext {
                        item.toUiModel(this)
                    }
                    isItemSavedState.update {
                        ItemSavedState.Success(item.id, itemUiModel)
                    }
                    telemetryManager.sendEvent(ItemCreate(EventItemType.Alias))
                }
        } else {
            PassLogger.i(TAG, "Empty User Id")
            snackbarDispatcher(ItemCreationError)
        }
    }

    private suspend fun onCreateAliasError(cause: Throwable?) {
        val message = when (cause) {
            is CannotCreateMoreAliasesError -> AliasSnackbarMessage.CannotCreateMoreAliasesError
            is EmailNotValidatedError -> AliasSnackbarMessage.EmailNotValidatedError
            is AliasRateLimitError -> AliasSnackbarMessage.AliasRateLimited
            else -> {
                PassLogger.w(TAG, "Create alias error")
                cause?.let {
                    PassLogger.w(TAG, it)
                }
                ItemCreationError
            }
        }
        snackbarDispatcher(message)
    }

    fun changeVault(shareId: ShareId) = viewModelScope.launch {
        onUserEditedContent()
        isLoadingState.update { IsLoadingState.Loading }
        selectedShareIdMutableState = Some(shareId)
    }

    private fun setupMailboxesAndSuffixes(aliasOptions: AliasOptions) {
        mailboxDraftRepository.clearMailboxes()
        mailboxDraftRepository.addMailboxes(aliasOptions.mailboxes.toSet())
        aliasOptions.mailboxes.firstOrNull()?.let { mailbox ->
            mailboxDraftRepository.toggleMailboxById(mailbox.id)
        }
        suffixDraftRepository.clearSuffixes()
        suffixDraftRepository.addSuffixes(aliasOptions.suffixes.toSet())
        aliasOptions.suffixes.firstOrNull()?.let { suffix ->
            suffixDraftRepository.selectSuffixById(suffix.suffix)
        }
    }

    companion object {
        private const val TAG = "CreateAliasViewModel"
        const val KEY_DRAFT_ALIAS = "draft_alias"
    }
}
