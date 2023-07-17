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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
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
import proton.android.pass.composecomponents.impl.uievents.IsButtonEnabled
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.errors.AliasRateLimitError
import proton.android.pass.data.api.errors.CannotCreateMoreAliasesError
import proton.android.pass.data.api.errors.EmailNotValidatedError
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.usecases.CanPerformPaidAction
import proton.android.pass.data.api.usecases.CreateAlias
import proton.android.pass.data.api.usecases.ObserveAliasOptions
import proton.android.pass.data.api.usecases.ObserveUpgradeInfo
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.featureitemcreate.impl.ItemCreate
import proton.android.pass.featureitemcreate.impl.alias.AliasSnackbarMessage.AliasCreated
import proton.android.pass.featureitemcreate.impl.alias.AliasSnackbarMessage.ItemCreationError
import proton.android.pass.featureitemcreate.impl.common.ShareError
import proton.android.pass.featureitemcreate.impl.common.ShareUiState
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonOptionalNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.preferences.IncItemCreatedCount
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import proton.pass.domain.ShareId
import proton.pass.domain.VaultWithItemCount
import proton.pass.domain.entity.NewAlias
import javax.inject.Inject

@HiltViewModel
open class CreateAliasViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val createAlias: CreateAlias,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val telemetryManager: TelemetryManager,
    protected val draftRepository: DraftRepository,
    private val incItemCreatedCount: IncItemCreatedCount,
    observeAliasOptions: ObserveAliasOptions,
    observeVaults: ObserveVaultsWithItemCount,
    savedStateHandle: SavedStateHandle,
    observeUpgradeInfo: ObserveUpgradeInfo,
    canPerformPaidAction: CanPerformPaidAction
) : BaseAliasViewModel(snackbarDispatcher, savedStateHandle) {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    private val navShareId: Option<ShareId> =
        savedStateHandle.get<String>(CommonOptionalNavArgId.ShareId.key)
            .toOption()
            .map { ShareId(it) }

    private val selectedSuffixState: MutableStateFlow<Option<AliasSuffixUiModel>> =
        MutableStateFlow(None)

    private val navShareIdState: MutableStateFlow<Option<ShareId>> = MutableStateFlow(navShareId)

    private val selectedShareIdState: MutableStateFlow<Option<ShareId>> = MutableStateFlow(None)
    private val observeAllVaultsFlow: Flow<List<VaultWithItemCount>> =
        observeVaults().distinctUntilChanged()

    private val shareUiState: StateFlow<ShareUiState> = combine(
        navShareIdState,
        selectedShareIdState,
        observeAllVaultsFlow.asLoadingResult(),
        canPerformPaidAction().asLoadingResult()
    ) { navShareId, selectedShareId, allSharesResult, canDoPaidAction ->
        val allShares = when (allSharesResult) {
            is LoadingResult.Error -> return@combine ShareUiState.Error(ShareError.SharesNotAvailable)
            LoadingResult.Loading -> return@combine ShareUiState.Loading
            is LoadingResult.Success -> allSharesResult.data
        }

        val canSwitchVaults = when (canDoPaidAction) {
            is LoadingResult.Error -> return@combine ShareUiState.Error(ShareError.UpgradeInfoNotAvailable)
            LoadingResult.Loading -> return@combine ShareUiState.Loading
            is LoadingResult.Success -> canDoPaidAction.data
        }

        if (allShares.isEmpty()) {
            return@combine ShareUiState.Error(ShareError.EmptyShareList)
        }
        val selectedVault = if (!canSwitchVaults) {
            val primaryVault = allShares.firstOrNull { it.vault.isPrimary }
            if (primaryVault == null) {
                PassLogger.w(TAG, "No primary vault found")
                return@combine ShareUiState.Error(ShareError.NoPrimaryVault)
            }
            primaryVault
        } else {
            allShares
                .firstOrNull { it.vault.shareId == selectedShareId.value() }
                ?: allShares.firstOrNull { it.vault.shareId == navShareId.value() }
                ?: allShares.firstOrNull { it.vault.isPrimary }
                ?: allShares.firstOrNull()
                ?: return@combine ShareUiState.Error(ShareError.EmptyShareList)
        }
        ShareUiState.Success(
            vaultList = allShares,
            currentVault = selectedVault
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ShareUiState.NotInitialised
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
                    PassLogger.w(TAG, it.exception, "Error loading AliasOptions")
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
    ) { aliasUiState, shareUiState, aliasOptionsResult, selectedMailboxes, selectedSuffix, upgradeInfoResult ->
        val hasReachedAliasLimit = upgradeInfoResult.getOrNull()?.hasReachedAliasLimit() ?: false
        val canUpgrade = upgradeInfoResult.getOrNull()?.isUpgradeAvailable ?: false
        val aliasItem = when (aliasOptionsResult) {
            is LoadingResult.Error -> aliasUiState.aliasItem
            LoadingResult.Loading -> aliasUiState.aliasItem
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

                val mailboxTitle = getMailboxTitle(mailboxes)

                val suffix = if (
                    selectedSuffix is Some &&
                    aliasOptions.suffixes.contains(selectedSuffix.value)
                ) {
                    selectedSuffix.value
                } else {
                    aliasOptions.suffixes.first()
                        .also { selectedSuffixState.update { it } }
                }
                val aliasToBeCreated = if (aliasUiState.aliasItem.prefix.isNotBlank()) {
                    getAliasToBeCreated(aliasUiState.aliasItem.prefix, suffix)
                } else {
                    aliasUiState.aliasItem.aliasToBeCreated
                }
                aliasUiState.aliasItem.copy(
                    aliasOptions = aliasOptions,
                    selectedSuffix = suffix,
                    mailboxes = mailboxes,
                    mailboxTitle = mailboxTitle,
                    aliasToBeCreated = aliasToBeCreated
                )
            }
        }

        CreateAliasUiState(
            baseAliasUiState = aliasUiState.copy(
                aliasItem = aliasItem,
                hasReachedAliasLimit = hasReachedAliasLimit,
                canUpgrade = canUpgrade,
                isLoadingState = IsLoadingState.from(
                    aliasUiState.isLoadingState is IsLoadingState.Loading ||
                        upgradeInfoResult is LoadingResult.Loading
                )
            ),
            shareUiState = shareUiState
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CreateAliasUiState.Initial
        )

    protected var titlePrefixInSync = true

    override fun onTitleChange(value: String) {
        onUserEditedContent()
        aliasItemState.update { aliasItem ->
            val prefix = if (titlePrefixInSync) {
                AliasUtils.formatAlias(value)
            } else {
                aliasItem.prefix
            }.take(AliasItem.MAX_PREFIX_LENGTH)
            aliasItem.copy(
                title = value,
                prefix = prefix,
                aliasToBeCreated = getAliasToBeCreated(
                    alias = prefix,
                    suffix = selectedSuffixState.value.value()
                )
            )
        }
        aliasItemValidationErrorsState.update {
            it.toMutableSet()
                .apply { remove(AliasItemValidationErrors.BlankTitle) }
        }
    }

    fun onPrefixChange(value: String) {
        if (value.contains(" ") || value.contains("\n")) return

        val prefix = AliasUtils.formatAlias(value.take(AliasItem.MAX_PREFIX_LENGTH))
        if (prefix == aliasItemState.value.prefix) return

        onUserEditedContent()
        aliasItemState.update {
            it.copy(
                prefix = prefix,
                aliasToBeCreated = getAliasToBeCreated(
                    alias = prefix,
                    suffix = aliasItemState.value.selectedSuffix
                )
            )
        }
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
        val aliasItem = createAliasUiState.value.baseAliasUiState.aliasItem
        if (aliasItem.selectedSuffix == null) {
            PassLogger.w(TAG, "Cannot create alias as SelectedSuffix is null")
            return@launch
        }

        val mailboxes = aliasItem.mailboxes.filter { it.selected }.map { it.model }
        val aliasItemValidationErrors = aliasItem.validate(allowEmptyTitle = isDraft)
        if (aliasItemValidationErrors.isNotEmpty()) {
            PassLogger.w(TAG, "Cannot create alias as there are validation errors: $aliasItemValidationErrors")
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
        aliasItem: AliasItem,
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
                        title = aliasItem.title,
                        note = aliasItem.note,
                        prefix = aliasItem.prefix,
                        suffix = aliasSuffix.toDomain(),
                        mailboxes = mailboxes.map(AliasMailboxUiModel::toDomain)
                    )
                )
            }.onSuccess { item ->
                incItemCreatedCount()
                val generatedAlias = getAliasToBeCreated(aliasItem.prefix, aliasSuffix) ?: ""
                isAliasSavedState.update {
                    AliasSavedState.Success(item.id, generatedAlias)
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
                val defaultMessage = "Create alias error"
                PassLogger.e(TAG, cause ?: Exception(defaultMessage), defaultMessage)
                ItemCreationError
            }
        }
        snackbarDispatcher(message)
    }

    fun changeVault(shareId: ShareId) = viewModelScope.launch {
        onUserEditedContent()
        isLoadingState.update { IsLoadingState.Loading }
        selectedShareIdState.update { shareId.toOption() }
    }

    companion object {
        private const val TAG = "CreateAliasViewModel"
        const val KEY_DRAFT_ALIAS = "draft_alias"
    }
}
