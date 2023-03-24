package proton.android.pass.featureitemdetail.impl

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import me.proton.core.accountmanager.domain.AccountManager
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.onError
import proton.android.pass.common.api.onSuccess
import proton.android.pass.common.api.some
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.repositories.ItemRepository
import proton.android.pass.featureitemdetail.impl.DetailSnackbarMessages.InitError
import proton.android.pass.featureitemdetail.impl.common.MoreInfoUiState
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.telemetry.api.EventItemType
import proton.android.pass.telemetry.api.TelemetryManager
import proton.pass.domain.Item
import proton.pass.domain.ItemId
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class ItemDetailViewModel @Inject constructor(
    private val accountManager: AccountManager,
    private val itemRepository: ItemRepository,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val encryptionContextProvider: EncryptionContextProvider,
    private val clock: Clock,
    private val telemetryManager: TelemetryManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val shareId: Option<ShareId> =
        Option.fromNullable(savedStateHandle.get<String>("shareId")?.let { ShareId(it) })
    private val itemId: Option<ItemId> =
        Option.fromNullable(savedStateHandle.get<String>("itemId")?.let { ItemId(it) })

    private val itemModelState: MutableStateFlow<Option<ItemModelUiState>> =
        MutableStateFlow(None)

    val uiState: StateFlow<ItemDetailScreenUiState> = itemModelState
        .map { itemModel ->
            val moreInfoUiState = when (itemModel) {
                None -> null
                is Some -> getMoreInfoUiState(itemModel.value.item)
            }
            ItemDetailScreenUiState(
                model = itemModel.value(),
                moreInfoUiState = moreInfoUiState
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ItemDetailScreenUiState.Initial
        )

    init {
        viewModelScope.launch {
            val userId = accountManager.getPrimaryUserId()
                .firstOrNull { userId -> userId != null }
            if (userId != null && shareId is Some && itemId is Some) {
                itemRepository.getById(userId, shareId.value, itemId.value)
                    .onSuccess { item ->
                        itemModelState.update {
                            encryptionContextProvider.withEncryptionContext {
                                ItemModelUiState(
                                    name = decrypt(item.title),
                                    item = item
                                ).some()
                            }
                        }
                        telemetryManager.sendEvent(ItemRead(EventItemType.from(item.itemType)))
                    }
                    .onError {
                        PassLogger.e(TAG, it, "Get by id error")
                        snackbarDispatcher(InitError)
                    }
            } else {
                val message = "Empty user/share/item Id"
                PassLogger.e(TAG, Exception(message), message)
                snackbarDispatcher(InitError)
            }
        }
    }

    private fun getMoreInfoUiState(item: Item): MoreInfoUiState = MoreInfoUiState(
        now = clock.now(),
        createdTime = item.createTime,
        lastAutofilled = item.lastAutofillTime,
        lastModified = item.modificationTime,
        numRevisions = item.revision
    )

    companion object {
        private const val TAG = "ItemDetailViewModel"
    }
}
