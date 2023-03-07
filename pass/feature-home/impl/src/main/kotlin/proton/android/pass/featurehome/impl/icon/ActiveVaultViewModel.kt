package proton.android.pass.featurehome.impl.icon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.data.api.usecases.ObserveActiveShare
import proton.pass.domain.ShareColor
import proton.pass.domain.ShareIcon
import proton.pass.domain.ShareProperties
import javax.inject.Inject

@HiltViewModel
class ActiveVaultViewModel @Inject constructor(
    observeActiveShare: ObserveActiveShare
) : ViewModel() {

    val state: StateFlow<ActiveVaultState> = observeActiveShare()
        .map {
            ActiveVaultState(
                properties = ShareProperties( // Change when we store the properties in the share
                    shareColor = ShareColor.Purple,
                    shareIcon = ShareIcon.Vault
                )
            )
        }
        .stateIn(
            scope = viewModelScope,
            initialValue = ActiveVaultState.Initial,
            started = SharingStarted.WhileSubscribed(5_000L)
        )
}
