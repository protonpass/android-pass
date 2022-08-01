package me.proton.android.pass.ui.detail.login

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.mapLatest
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.decrypt
import me.proton.core.pass.domain.Item
import me.proton.core.pass.domain.ItemType
import javax.inject.Inject

@HiltViewModel
class LoginDetailViewModel @Inject constructor(
    private val cryptoContext: CryptoContext
) : ViewModel() {

    private val itemFlow: MutableStateFlow<Item?> = MutableStateFlow(null)

    val initialViewState = getInitialState()
    val viewState: Flow<LoginUiModel> = itemFlow.mapLatest { getUiModel(it) }

    fun setItem(item: Item) {
        if (itemFlow.value == null) {
            itemFlow.value = item
        }
    }

    private fun getUiModel(item: Item?): LoginUiModel {
        if (item == null) return getInitialState()

        val itemContents = item.itemType as ItemType.Login
        return LoginUiModel(
            title = item.title.decrypt(cryptoContext.keyStoreCrypto),
            username = itemContents.username,
            password = itemContents.password.decrypt(cryptoContext.keyStoreCrypto),
            websites = emptyList(),
            note = ""
        )
    }

    private fun getInitialState(): LoginUiModel =
        LoginUiModel(
            title = "",
            username = "",
            password = "",
            websites = emptyList(),
            note = ""
        )

    data class LoginUiModel(
        val title: String,
        val username: String,
        val password: String, // TODO: Keep this encrypted
        val websites: List<String>,
        val note: String
    )
}
