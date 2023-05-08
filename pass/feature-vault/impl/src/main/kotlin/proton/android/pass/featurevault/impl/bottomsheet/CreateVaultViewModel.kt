package proton.android.pass.featurevault.impl.bottomsheet

import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.crypto.api.context.EncryptionContextProvider
import proton.android.pass.data.api.errors.CannotCreateMoreVaultsError
import proton.android.pass.data.api.usecases.CreateVault
import proton.android.pass.data.api.usecases.GetUpgradeInfo
import proton.android.pass.featurevault.impl.VaultSnackbarMessage
import proton.android.pass.log.api.PassLogger
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.pass.domain.entity.NewVault
import javax.inject.Inject

@HiltViewModel
class CreateVaultViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher,
    private val createVault: CreateVault,
    private val encryptionContextProvider: EncryptionContextProvider,
    getUpgradeInfo: GetUpgradeInfo,
) : BaseVaultViewModel() {

    val createState: StateFlow<CreateVaultUiState> = combine(
        state,
        getUpgradeInfo().asLoadingResult()
    ) { baseState, upgrade ->
        val (isLoading, showUpgradeButton) = when (upgrade) {
            is LoadingResult.Success -> {
                val upgradeData = upgrade.data
                val vaultLimitReached = upgradeData.totalVaults >= upgradeData.vaultLimit
                val showUpgrade = upgradeData.isUpgradeAvailable && vaultLimitReached
                baseState.isLoading to showUpgrade
            }

            LoadingResult.Loading -> IsLoadingState.Loading to false
            is LoadingResult.Error -> {
                PassLogger.w(TAG, upgrade.exception, "Get upgrade info failed")
                baseState.isLoading to false
            }
        }

        CreateVaultUiState(
            base = baseState.copy(isLoading = isLoading),
            displayNeedUpgrade = showUpgradeButton
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = CreateVaultUiState.Initial
    )

    fun onCreateClick() = viewModelScope.launch {
        if (formFlow.value.name.isBlank()) return@launch

        isLoadingFlow.update { IsLoadingState.Loading }

        val form = formFlow.value
        val body = encryptionContextProvider.withEncryptionContext {
            NewVault(
                name = encrypt(form.name),
                description = encrypt(""),
                icon = form.icon,
                color = form.color
            )
        }

        PassLogger.d(TAG, "Sending Create Vault request")
        runCatching { createVault(vault = body) }
            .onSuccess {
                PassLogger.d(TAG, "Vault created successfully")
                snackbarDispatcher(VaultSnackbarMessage.CreateVaultSuccess)
                isVaultCreated.update { IsVaultCreatedEvent.Created }
            }
            .onFailure {
                val message = if (it is CannotCreateMoreVaultsError) {
                    VaultSnackbarMessage.CannotCreateMoreVaultsError
                } else {
                    PassLogger.w(TAG, it, "Create vault failed")
                    VaultSnackbarMessage.CreateVaultError
                }
                snackbarDispatcher(message)
            }

        isLoadingFlow.update { IsLoadingState.NotLoading }
    }


    companion object {
        private const val TAG = "CreateVaultViewModel"
    }
}
