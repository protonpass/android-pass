package me.proton.android.pass.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import me.proton.android.pass.log.PassLogger
import me.proton.core.crypto.common.context.CryptoContext
import me.proton.core.crypto.common.keystore.encrypt
import me.proton.core.pass.domain.Share
import me.proton.core.pass.domain.entity.NewVault
import me.proton.core.pass.domain.usecases.CreateVault
import me.proton.core.pass.domain.usecases.ObserveCurrentUser
import me.proton.core.pass.domain.usecases.ObserveShares
import me.proton.core.pass.domain.usecases.RefreshShares
import me.proton.core.user.domain.entity.User
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    observeCurrentUser: ObserveCurrentUser,
    observeShares: ObserveShares,
    createVault: CreateVault,
    refreshShares: RefreshShares,
    cryptoContext: CryptoContext
) : ViewModel() {

    private val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        PassLogger.e("AppViewModel", throwable)
    }

    init {
        viewModelScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            val currentUser: User = observeCurrentUser().firstOrNull() ?: return@launch
            refreshShares(currentUser.userId)
            val shares: List<Share> = observeShares(currentUser.userId).firstOrNull() ?: emptyList()
            if (shares.isEmpty()) {
                createVault(
                    userId = currentUser.userId,
                    vault = NewVault(
                        name = "Personal".encrypt(cryptoContext.keyStoreCrypto),
                        description = "Personal vault".encrypt(cryptoContext.keyStoreCrypto)
                    )
                )
            }
        }
    }
}
