package proton.android.pass.autofill.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.notifications.api.SnackbarDispatcher
import proton.android.pass.notifications.api.SnackbarMessage
import javax.inject.Inject

@HiltViewModel
class SnackBarViewModel @Inject constructor(
    private val snackbarDispatcher: SnackbarDispatcher
) : ViewModel() {

    val state: StateFlow<Option<SnackbarMessage>> = snackbarDispatcher.snackbarMessage
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000L),
            initialValue = None
        )

    fun onSnackbarMessageDelivered() = viewModelScope.launch {
        snackbarDispatcher.snackbarMessageDelivered()
    }
}
