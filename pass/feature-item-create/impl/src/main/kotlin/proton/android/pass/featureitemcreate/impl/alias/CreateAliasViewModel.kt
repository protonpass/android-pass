package proton.android.pass.featureitemcreate.impl.alias

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.errors.CannotCreateMoreAliasesError
import proton.android.pass.data.api.repositories.DraftRepository
import proton.android.pass.data.api.usecases.CreateAlias
import proton.android.pass.data.api.usecases.ObserveAliasOptions
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.featureitemcreate.impl.ItemCreate
import proton.android.pass.featureitemcreate.impl.alias.AliasSnackbarMessage.AliasCreated
import proton.android.pass.featureitemcreate.impl.alias.AliasSnackbarMessage.ItemCreationError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewAlias
import javax.inject.Inject

@HiltViewModel
open class CreateAliasViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val createAlias: CreateAlias,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val telemetryManager: TelemetryManager,
    private val draftRepository: DraftRepository,
    observeAliasOptions: ObserveAliasOptions,
    observeVaults: ObserveVaultsWithItemCount,
    savedStateHandle: SavedStateHandle
) : BaseAliasViewModel(snackbarDispatcher, observeAliasOptions, observeVaults, savedStateHandle) {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e(TAG, throwable)
    }

    protected var titlePrefixInSync = true

    override fun onTitleChange(value: String) {
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
                    suffix = aliasItemState.value.selectedSuffix
                )
            )
        }
        aliasItemValidationErrorsState.update {
            it.toMutableSet()
                .apply { remove(AliasItemValidationErrors.BlankTitle) }
        }
    }

    override fun onPrefixChange(value: String) {
        if (value.contains(" ") || value.contains("\n")) return
        val prefix = value.take(AliasItem.MAX_PREFIX_LENGTH)
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

    fun createAlias(shareId: ShareId) = viewModelScope.launch(coroutineExceptionHandler) {
        val aliasItem = aliasUiState.value.aliasItem
        if (aliasItem.selectedSuffix == null) return@launch

        val mailboxes = aliasItem.mailboxes.filter { it.selected }.map { it.model }
        val aliasItemValidationErrors = aliasItem.validate(allowEmptyTitle = isDraft)
        if (aliasItemValidationErrors.isNotEmpty()) {
            aliasItemValidationErrorsState.update { aliasItemValidationErrors }
            return@launch
        }

        if (isDraft) {
            draftRepository.save(KEY_DRAFT_ALIAS, aliasItem)
            isAliasDraftSavedState.tryEmit(AliasDraftSavedState.Success(shareId, aliasItem))
        } else {
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
                .onSuccess { item ->
                    val generatedAlias =
                        getAliasToBeCreated(aliasItem.prefix, aliasSuffix) ?: ""
                    isAliasSavedState.update { AliasSavedState.Success(item.id, generatedAlias) }
                    snackbarDispatcher(AliasCreated)
                    telemetryManager.sendEvent(ItemCreate(EventItemType.Alias))
                }
                .onError { onCreateAliasError(it) }
        } else {
            PassLogger.i(TAG, "Empty User Id")
            snackbarDispatcher(ItemCreationError)
        }
    }

    private suspend fun onCreateAliasError(cause: Throwable?) {
        if (cause is CannotCreateMoreAliasesError) {
            snackbarDispatcher(AliasSnackbarMessage.CannotCreateMoreAliasesError)
        } else {
            val defaultMessage = "Create alias error"
            PassLogger.e(TAG, cause ?: Exception(defaultMessage), defaultMessage)
            snackbarDispatcher(ItemCreationError)
        }
    }

    companion object {
        private const val TAG = "CreateAliasViewModel"
    }
}
