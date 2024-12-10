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

package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.SavedStateHandleSaveableApi
import androidx.lifecycle.viewmodel.compose.saveable
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
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
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.combineN
import proton.android.pass.common.api.getOrNull
import proton.android.pass.common.api.toOption
import proton.android.pass.commonrust.api.AliasPrefixValidator
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.errors.AliasRateLimitError
import proton.android.pass.data.api.errors.CannotCreateMoreAliasesError
import proton.android.pass.data.api.errors.EmailNotValidatedError
import proton.android.pass.data.api.repositories.DraftAttachmentRepository
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.repositories.MetadataResolver
import proton.android.pass.data.api.usecases.CreateAlias
import proton.android.pass.data.api.usecases.ObserveAliasOptions
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.data.api.usecases.attachments.ClearAttachments
import proton.android.pass.data.api.usecases.attachments.UploadAttachment
import proton.android.pass.data.api.usecases.defaultvault.ObserveDefaultVault
import proton.android.pass.domain.ShareId
import proton.android.pass.domain.VaultWithItemCount
import proton.android.pass.domain.entity.NewAlias
import proton.android.pass.featureitemcreate.impl.ItemCreate
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.alias.AliasSnackbarMessage.AliasCreated
import proton.android.pass.featureitemcreate.impl.alias.AliasSnackbarMessage.ItemCreationError
import proton.android.pass.featureitemcreate.impl.common.OptionShareIdSaver
import proton.android.pass.featureitemcreate.impl.common.ShareUiState
import proton.android.pass.featureitemcreate.impl.common.getShareUiStateFlow
import proton.android.pass.inappreview.api.InAppReviewTriggerMetrics
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.FeatureFlagsPreferencesRepository
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
    private val aliasPrefixValidator: AliasPrefixValidator,
    observeAliasOptions: ObserveAliasOptions,
    observeVaults: ObserveVaultsWithItemCount,
    observeUpgradeInfo: ObserveUpgradeInfo,
    observeDefaultVault: ObserveDefaultVault,
    clearAttachments: ClearAttachments,
    uploadAttachment: UploadAttachment,
    draftAttachmentRepository: DraftAttachmentRepository,
    metadataResolver: MetadataResolver,
    featureFlagsRepository: FeatureFlagsPreferencesRepository,
    savedStateHandleProvider: SavedStateHandleProvider
) : BaseAliasViewModel(
    clearAttachments = clearAttachments,
    uploadAttachment = uploadAttachment,
    draftAttachmentRepository = draftAttachmentRepository,
    metadataResolver = metadataResolver,
    snackbarDispatcher = snackbarDispatcher,
    featureFlagsRepository = featureFlagsRepository,
    savedStateHandleProvider = savedStateHandleProvider
) {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.w(TAG, throwable)
    }

    private val navShareId: Option<ShareId> =
        savedStateHandleProvider.get().get<String>(CommonOptionalNavArgId.ShareId.key)
            .toOption()
            .map { ShareId(it) }

    private val selectedSuffixState: MutableStateFlow<Option<AliasSuffixUiModel>> =
        MutableStateFlow(None)

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
        .flatMapLatest { state ->
            when (state) {
                is ShareUiState.Error -> flowOf(LoadingResult.Error(RuntimeException()))
                ShareUiState.Loading -> flowOf(LoadingResult.Loading)
                ShareUiState.NotInitialised -> flowOf(LoadingResult.Loading)
                is ShareUiState.Success -> observeAliasOptions(state.currentVault.vault.shareId)
                    .map(::AliasOptionsUiModel)
                    .asLoadingResult()
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

    val createAliasUiState: StateFlow<CreateAliasUiState> = combineN(
        baseAliasUiState,
        shareUiState,
        aliasOptionsState,
        selectedMailboxListState,
        selectedSuffixState,
        observeUpgradeInfo().asLoadingResult()
    ) { aliasUiState, shareUiState, aliasOptionsResult, selectedMailboxes,
        selectedSuffix, upgradeInfoResult ->
        val hasReachedAliasLimit = upgradeInfoResult.getOrNull()?.hasReachedAliasLimit() ?: false
        val canUpgrade = upgradeInfoResult.getOrNull()?.isUpgradeAvailable ?: false

        aliasItemFormMutableState = when (aliasOptionsResult) {
            is LoadingResult.Error -> aliasItemFormState
            LoadingResult.Loading -> aliasItemFormState
            is LoadingResult.Success -> {
                val aliasOptions = aliasOptionsResult.data

                val mailboxes = aliasOptions.mailboxes
                    .map { model ->
                        SelectedAliasMailboxUiModel(
                            model = model,
                            selected = selectedMailboxes.contains(model.id)
                        )
                    }
                    .toMutableList()
                if (mailboxes.none { it.selected } && mailboxes.isNotEmpty()) {
                    val mailbox = mailboxes.removeAt(0)
                    mailboxes.add(0, mailbox.copy(selected = true))
                        .also { selectedMailboxListState.update { listOf(mailbox.model.id) } }
                }

                val suffix = if (
                    selectedSuffix is Some &&
                    aliasOptions.suffixes.contains(selectedSuffix.value)
                ) {
                    selectedSuffix.value
                } else {
                    aliasOptions.suffixes.first()
                        .also { selectedSuffixState.update { it } }
                }
                val aliasToBeCreated = if (aliasItemFormState.prefix.isNotBlank()) {
                    getAliasToBeCreated(aliasItemFormState.prefix, suffix)
                } else {
                    aliasItemFormState.aliasToBeCreated
                }
                aliasItemFormState.copy(
                    aliasOptions = aliasOptions,
                    selectedSuffix = suffix,
                    mailboxes = mailboxes,
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
                suffix = selectedSuffixState.value.value()
            )
        )

        aliasItemValidationErrorsState.update {
            it.toMutableSet()
                .apply {
                    remove(AliasItemValidationErrors.BlankTitle)
                    remove(AliasItemValidationErrors.InvalidAliasContent)
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
                    remove(AliasItemValidationErrors.BlankPrefix)
                    remove(AliasItemValidationErrors.InvalidAliasContent)
                }
        }
        titlePrefixInSync = false
    }

    fun onSuffixChange(suffix: AliasSuffixUiModel) {
        onUserEditedContent()
        selectedSuffixState.update { suffix.toOption() }
    }

    fun createAlias(shareId: ShareId) = viewModelScope.launch(coroutineExceptionHandler) {
        val aliasItem = aliasItemFormState
        if (aliasItem.selectedSuffix == null) {
            PassLogger.w(TAG, "Cannot create alias as SelectedSuffix is null")
            return@launch
        }

        val mailboxes = aliasItem.mailboxes.filter { it.selected }.map { it.model }
        val aliasItemValidationErrors = aliasItem.validate(
            allowEmptyTitle = isDraft,
            aliasPrefixValidator = aliasPrefixValidator
        )
        if (aliasItemValidationErrors.isNotEmpty()) {
            PassLogger.w(
                TAG,
                "Cannot create alias as there are validation errors: $aliasItemValidationErrors"
            )
            aliasItemValidationErrorsState.update { aliasItemValidationErrors }
            return@launch
        }

        if (isDraft) {
            PassLogger.d(TAG, "Creating draft alias")
            draftRepository.save(KEY_DRAFT_ALIAS, aliasItem)
            isAliasDraftSavedState.tryEmit(AliasDraftSavedState.Success(shareId, aliasItem))
        } else {
            PassLogger.d(TAG, "Performing create alias")
            isLoadingState.update { IsLoadingState.Loading }
            performCreateAlias(shareId, aliasItem, aliasItem.selectedSuffix, mailboxes)
            isLoadingState.update { IsLoadingState.NotLoading }
        }
    }

    private suspend fun performCreateAlias(
        shareId: ShareId,
        aliasItemFormState: AliasItemFormState,
        aliasSuffix: AliasSuffixUiModel,
        mailboxes: List<AliasMailboxUiModel>
    ) {
        val userId = accountManager.getPrimaryUserId().first { userId -> userId != null }
        if (userId != null) {
            runCatching {
                createAlias(
                    userId = userId,
                    shareId = shareId,
                    newAlias = NewAlias(
                        title = aliasItemFormState.title,
                        note = aliasItemFormState.note,
                        prefix = aliasItemFormState.prefix,
                        aliasName = aliasItemFormState.senderName,
                        suffix = aliasSuffix.toDomain(),
                        mailboxes = mailboxes.map(AliasMailboxUiModel::toDomain)
                    )
                )
            }.onSuccess { item ->
                inAppReviewTriggerMetrics.incrementItemCreatedCount()
                val itemUiModel = encryptionContextProvider.withEncryptionContext {
                    item.toUiModel(this)
                }
                isItemSavedState.update {
                    ItemSavedState.Success(item.id, itemUiModel)
                }
                snackbarDispatcher(AliasCreated)
                telemetryManager.sendEvent(ItemCreate(EventItemType.Alias))
            }.onFailure { onCreateAliasError(it) }
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

    companion object {
        private const val TAG = "CreateAliasViewModel"
        const val KEY_DRAFT_ALIAS = "draft_alias"
    }
}
