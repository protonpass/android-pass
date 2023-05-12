package proton.android.pass.featurevault.impl.bottomsheet.select

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import proton.android.pass.common.api.LoadingResult
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.common.api.Some
import proton.android.pass.common.api.asLoadingResult
import proton.android.pass.common.api.toOption
import proton.android.pass.data.api.usecases.ObserveVaultsWithItemCount
import proton.android.pass.log.api.PassLogger
import proton.pass.domain.ShareId
import javax.inject.Inject

@HiltViewModel
class SelectVaultViewModel @Inject constructor(
    observeVaultsWithItemCount: ObserveVaultsWithItemCount,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val selected: Option<ShareId> = savedStateHandle
        .get<String>(SelectedVaultOptionalArg.key)
        .toOption()
        .map { ShareId(it) }

    private val eventFlow = MutableStateFlow<SelectVaultUiEvent>(SelectVaultUiEvent.Unknown)

    val state: StateFlow<SelectVaultUiState> = combine(
        eventFlow,
        observeVaultsWithItemCount().asLoadingResult()
    ) { eventState, vaultsResult ->

        val vaults = when (vaultsResult) {
            LoadingResult.Loading -> emptyList()
            is LoadingResult.Success -> vaultsResult.data
            is LoadingResult.Error -> {
                PassLogger.w(TAG, vaultsResult.exception, "Error observing vaults")
                eventFlow.update { SelectVaultUiEvent.Close }
                emptyList()
            }
        }

        val selectedVault = when (selected) {
            None -> None
            is Some -> {
                val vault = vaults.firstOrNull { it.vault.shareId == selected.value }
                vault.toOption()
            }
        }

        SelectVaultUiState(
            vaults = vaults,
            selected = selectedVault,
            event = eventState
        )

    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = SelectVaultUiState.Initial
    )

    companion object {
        private const val TAG = "SelectVaultViewModel"
    }
}
