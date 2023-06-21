package proton.android.pass.featureitemcreate.impl.creditcard

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.some
import proton.android.pass.commonui.api.SavedStateHandleProvider
import proton.android.pass.commonui.api.require
import proton.android.pass.commonui.api.toItemContents
import proton.android.pass.commonui.api.toUiModel
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.GetItemById
import proton.android.pass.data.api.usecases.UpdateItem
import proton.android.pass.featureitemcreate.impl.ItemSavedState
import proton.android.pass.featureitemcreate.impl.ItemUpdate
import proton.android.pass.featureitemcreate.impl.creditcard.CreditCardSnackbarMessage.InitError
import proton.android.pass.log.api.PassLogger
import proton.android.pass.navigation.api.CommonNavArgId
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import proton.pass.domain.Item
import proton.pass.domain.ItemContents
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class UpdateCreditCardViewModel @Inject constructor(
    private val getItemById: GetItemById,
    private val updateItem: UpdateItem,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val accountManager: AccountManager,
    private val telemetryManager: TelemetryManager,
    savedStateHandle: SavedStateHandleProvider,
) : BaseCreditCardViewModel(
    encryptionContextProvider = encryptionContextProvider
) {
    private val navShareId: ShareId =
        ShareId(savedStateHandle.get().require(CommonNavArgId.ShareId.key))
    private val navItemId: ItemId =
        ItemId(savedStateHandle.get().require(CommonNavArgId.ItemId.key))
    private val navShareIdState: MutableStateFlow<ShareId> = MutableStateFlow(navShareId)
    private val itemState: MutableStateFlow<Option<Item>> = MutableStateFlow(None)

    init {
        viewModelScope.launch {
            setupInitialState()
        }
    }

    val state: StateFlow<UpdateCreditCardUiState> = combine(
        navShareIdState,
        baseState,
        UpdateCreditCardUiState::Success
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UpdateCreditCardUiState.NotInitialised
    )

    private suspend fun setupInitialState() {
        isLoadingState.update { IsLoadingState.Loading }
        runCatching { getItemById(navShareId, navItemId).first() }
            .onSuccess { item ->
                itemState.update { item.some() }
                val itemContents = encryptionContextProvider.withEncryptionContext {
                    item.toItemContents(this)
                }
                itemContentState.update { itemContents as ItemContents.CreditCard }
            }
            .onFailure {
                PassLogger.w(TAG, it, "Error getting item by id")
                snackbarDispatcher(InitError)
            }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    fun update() = viewModelScope.launch {
        val canUpdate = validateItem()
        if (!canUpdate) {
            PassLogger.i(TAG, "Cannot update credit card")
            return@launch
        }
        isLoadingState.update { IsLoadingState.Loading }
        runCatching {
            val userId = accountManager.getPrimaryUserId().first()
                ?: throw IllegalStateException("User id is null")
            val item = itemState.value.value() ?: throw IllegalStateException("Item is null")
            updateItem(
                userId = userId,
                shareId = navShareId,
                item = item,
                contents = itemContentState.value
            )
        }.onSuccess { item ->
            PassLogger.i(TAG, "Credit card successfully updated")
            isItemSavedState.update {
                ItemSavedState.Success(
                    itemId = item.id,
                    item = encryptionContextProvider.withEncryptionContext { item.toUiModel(this) }
                )
            }
            snackbarDispatcher(CreditCardSnackbarMessage.ItemUpdated)
            telemetryManager.sendEvent(ItemUpdate(EventItemType.CreditCard))
        }.onFailure {
            PassLogger.e(TAG, it, "Update credit card error")
            snackbarDispatcher(CreditCardSnackbarMessage.ItemUpdateError)
        }
        isLoadingState.update { IsLoadingState.NotLoading }
    }

    companion object {
        private const val TAG = "UpdateCreditCardViewModel"
    }
}
