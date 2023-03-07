package proton.android.pass.featurehome.impl.icon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Some
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.usecases.ObserveActiveShare
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareProperties
import proton_pass_vault_v1.VaultV1
import javax.inject.Inject

@HiltViewModel
class ActiveVaultViewModel @Inject constructor(
    observeActiveShare: ObserveActiveShare,
    private val encryptionContextProvider: EncryptionContextProvider
): ViewModel() {

    val state: StateFlow<ActiveVaultState> = observeActiveShare()
        .map {
            val name = when (val content = it.content) {
                None -> ""
                is Some -> {
                    val decrypted = encryptionContextProvider.withEncryptionContext { decrypt(content.value) }
                    val parsed = VaultV1.Vault.parseFrom(decrypted)
                    parsed.name
                }
            }

            ActiveVaultState(
                name = name,
                properties = ShareProperties( // Change when we store the properties in the share
                    shareColor = ShareColor.Purple,
                    shareIcon = ShareIcon.House
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            initialValue = ActiveVaultState.Initial,
            started = SharingStarted.WhileSubscribed(5_000L)
        )
}
