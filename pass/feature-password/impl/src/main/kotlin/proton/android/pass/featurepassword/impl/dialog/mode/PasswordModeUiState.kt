package proton.android.pass.featurepassword.impl.dialog.mode

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.preferences.PasswordGenerationMode

sealed interface PasswordModeUiEvent {
    object Unknown : PasswordModeUiEvent
    object Close : PasswordModeUiEvent
}

@Immutable
data class PasswordModeUiState(
    val options: PersistentList<PasswordGenerationMode>,
    val selected: Option<PasswordGenerationMode>,
    val event: PasswordModeUiEvent
) {
    companion object {
        val Initial = PasswordModeUiState(
            options = PasswordGenerationMode.values().toList().toPersistentList(),
            selected = None,
            event = PasswordModeUiEvent.Unknown
        )
    }
}
