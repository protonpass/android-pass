package proton.android.pass.featurecreateitem.impl.login

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import me.proton.core.domain.entity.UserId
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.toOption
import proton.android.pass.commonuimodels.api.ShareUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.data.api.url.UrlSanitizer
import proton.android.pass.data.api.usecases.CreateAlias
import proton.android.pass.data.api.usecases.ObserveVaults
import proton.android.pass.featurecreateitem.impl.IsSentToTrashState
import proton.android.pass.featurecreateitem.impl.ItemSavedState
import proton.android.pass.featurecreateitem.impl.alias.AliasItem
import proton.android.pass.featurecreateitem.impl.alias.AliasMailboxUiModel
import proton.android.pass.featurecreateitem.impl.alias.AliasSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarMessageRepository
import proton.pass.domain.Item
import proton.pass.domain.ShareId
import proton.pass.domain.entity.NewAlias

abstract class BaseLoginViewModel(
    private val createAlias: CreateAlias,
    protected val accountManager: AccountManager,
    private val snackbarMessageRepository: SnackbarMessageRepository,
    observeVaults: ObserveVaults,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    protected val navShareId = savedStateHandle.get<String>(CommonNavArgId.ShareId.key)
        .toOption()
        .map { ShareId(it) }
    private val navShareIdState = MutableStateFlow(navShareId)
    private val selectedShareIdState: MutableStateFlow<Option<ShareId>> = MutableStateFlow(None)

    protected val loginItemState: MutableStateFlow<LoginItem> = MutableStateFlow(LoginItem.Empty)
    protected val aliasItemState: MutableStateFlow<Option<AliasItem>> = MutableStateFlow(None)
    protected val isLoadingState: MutableStateFlow<IsLoadingState> =
        MutableStateFlow(IsLoadingState.NotLoading)
    protected val isItemSavedState: MutableStateFlow<ItemSavedState> =
        MutableStateFlow(ItemSavedState.Unknown)
    protected val isItemSentToTrashState: MutableStateFlow<IsSentToTrashState> =
        MutableStateFlow(IsSentToTrashState.NotSent)
    private val loginItemValidationErrorsState: MutableStateFlow<Set<LoginItemValidationErrors>> =
        MutableStateFlow(emptySet())
    private val focusLastWebsiteState: MutableStateFlow<Boolean> = MutableStateFlow(false)
    protected val canUpdateUsernameState: MutableStateFlow<Boolean> = MutableStateFlow(true)

    private val observeAllSharesFlow = observeVaults()
        .map { shares ->
            when (shares) {
                LoadingResult.Loading -> emptyList()
                is LoadingResult.Error -> {
                    PassLogger.e(TAG, shares.exception, "Cannot retrieve all shares")
                    emptyList()
                }
                is LoadingResult.Success ->
                    shares.data
                        .map { ShareUiModel(it.shareId, it.name) }
            }
        }
        .distinctUntilChanged()

    private val sharesWrapperState = combine(
        navShareIdState,
        selectedShareIdState,
        observeAllSharesFlow
    ) { navShareId, selectedShareId, allShares ->
        val selectedShare = allShares
            .firstOrNull { it.id == selectedShareId.value() }
            ?: allShares.firstOrNull { it.id == navShareId.value() }
            ?: allShares.first()
        SharesWrapper(allShares, selectedShare)
    }

    private data class SharesWrapper(
        val shareList: List<ShareUiModel>,
        val currentShare: ShareUiModel
    )

    private val loginItemWrapperState = combine(
        loginItemState,
        loginItemValidationErrorsState,
        canUpdateUsernameState,
        isItemSentToTrashState
    ) { loginItem, loginItemValidationErrors, updateUsername, sentToTrash ->
        LoginItemWrapper(loginItem, loginItemValidationErrors, updateUsername, sentToTrash)
    }

    private data class LoginItemWrapper(
        val loginItem: LoginItem,
        val loginItemValidationErrors: Set<LoginItemValidationErrors>,
        val canUpdateUsername: Boolean,
        val itemSentToTrash: IsSentToTrashState
    )

    val loginUiState: StateFlow<CreateUpdateLoginUiState> = combine(
        sharesWrapperState,
        loginItemWrapperState,
        isLoadingState,
        isItemSavedState,
        focusLastWebsiteState
    ) { shareWrapper, loginItemWrapper, isLoading, isItemSaved, focusLastWebsite ->
        CreateUpdateLoginUiState(
            shareList = shareWrapper.shareList,
            selectedShareId = shareWrapper.currentShare,
            loginItem = loginItemWrapper.loginItem,
            validationErrors = loginItemWrapper.loginItemValidationErrors,
            isLoadingState = isLoading,
            isItemSaved = isItemSaved,
            focusLastWebsite = focusLastWebsite,
            canUpdateUsername = loginItemWrapper.canUpdateUsername,
            isItemSentToTrash = loginItemWrapper.itemSentToTrash
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CreateUpdateLoginUiState.Initial
        )

    fun onTitleChange(value: String) {
        loginItemState.update { it.copy(title = value) }
        loginItemValidationErrorsState.update {
            it.toMutableSet().apply { remove(LoginItemValidationErrors.BlankTitle) }
        }
    }

    fun onUsernameChange(value: String) {
        loginItemState.update { it.copy(username = value) }
    }

    fun onPasswordChange(value: String) {
        loginItemState.update { it.copy(password = value) }
    }

    fun onWebsiteChange(value: String, index: Int) {
        val newValue = value.replace(" ", "").replace("\n", "")
        loginItemState.update {
            it.copy(
                websiteAddresses = it.websiteAddresses.toMutableList()
                    .apply { this[index] = newValue }
            )
        }
        focusLastWebsiteState.update { false }
    }

    fun onAddWebsite() {
        loginItemState.update {
            val websites = sanitizeWebsites(it.websiteAddresses).toMutableList()
            websites.add("")

            it.copy(websiteAddresses = websites)
        }
        focusLastWebsiteState.update { true }
    }

    fun onRemoveWebsite(index: Int) {
        loginItemState.update {
            it.copy(
                websiteAddresses = it.websiteAddresses.toMutableList().apply { removeAt(index) }
            )
        }
        focusLastWebsiteState.update { false }
    }

    fun onNoteChange(value: String) {
        loginItemState.update { it.copy(note = value) }
    }

    fun onEmitSnackbarMessage(snackbarMessage: LoginSnackbarMessages) =
        viewModelScope.launch {
            snackbarMessageRepository.emitSnackbarMessage(snackbarMessage)
        }

    suspend fun performCreateAlias(
        userId: UserId,
        shareId: ShareId,
        aliasItem: AliasItem
    ): LoadingResult<Item> =
        if (aliasItem.selectedSuffix != null) {
            createAlias(
                userId = userId,
                shareId = shareId,
                newAlias = NewAlias(
                    title = aliasItem.title,
                    note = aliasItem.note,
                    prefix = aliasItem.alias,
                    suffix = aliasItem.selectedSuffix.toDomain(),
                    mailboxes = aliasItem.mailboxes
                        .filter { it.selected }
                        .map { it.model }
                        .map(AliasMailboxUiModel::toDomain)
                )
            )
        } else {
            val message = "Empty suffix on create alias"
            PassLogger.i(TAG, message)
            snackbarMessageRepository.emitSnackbarMessage(AliasSnackbarMessage.ItemCreationError)
            LoadingResult.Error(Exception(message))
        }

    protected fun validateItem(): Boolean {
        loginItemState.update {
            val websites = sanitizeWebsites(it.websiteAddresses)
            it.copy(websiteAddresses = websites)
        }
        val loginItem = loginItemState.value
        val loginItemValidationErrors = loginItem.validate()
        if (loginItemValidationErrors.isNotEmpty()) {
            loginItemValidationErrorsState.update { loginItemValidationErrors }
            return false
        }
        return true
    }

    private fun sanitizeWebsites(websites: List<String>): List<String> =
        websites.map { url ->
            if (url.isBlank()) {
                ""
            } else {
                when (val res = UrlSanitizer.sanitize(url)) {
                    is LoadingResult.Success -> res.data
                    else -> url
                }
            }
        }

    fun changeVault(shareId: ShareId) = viewModelScope.launch {
        selectedShareIdState.update { shareId.toOption() }
    }

    fun onDeleteTotp() {
        loginItemState.update { it.copy(primaryTotp = "") }
    }

    companion object {
        private const val TAG = "BaseLoginViewModel"
    }
}

