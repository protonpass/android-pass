package proton.android.pass.featuretrial.impl

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import proton.android.pass.data.api.usecases.GetUserPlan
import javax.inject.Inject

@HiltViewModel
class TrialViewModel @Inject constructor(
    getUserPlan: GetUserPlan
) : ViewModel() {

    val state: StateFlow<TrialUiState> = getUserPlan().map {
        TrialUiState(
            remainingTrialDays = 1
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = TrialUiState(remainingTrialDays = 0)
    )

}

@Stable
data class TrialUiState(
    val remainingTrialDays: Int
)
