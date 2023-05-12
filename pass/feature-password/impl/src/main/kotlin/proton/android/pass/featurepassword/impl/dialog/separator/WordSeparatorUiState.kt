package proton.android.pass.featurepassword.impl.dialog.separator

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.common.api.None
import proton.android.pass.common.api.Option
import proton.android.pass.password.api.PasswordGenerator

sealed interface WordSeparatorUiEvent {
    object Unknown : WordSeparatorUiEvent
    object Close : WordSeparatorUiEvent
}

@Immutable
data class WordSeparatorUiState(
    val options: PersistentList<PasswordGenerator.WordSeparator>,
    val selected: Option<PasswordGenerator.WordSeparator>,
    val event: WordSeparatorUiEvent
) {
    companion object {
        val Initial = WordSeparatorUiState(
            options = PasswordGenerator.WordSeparator.values().toList().toPersistentList(),
            selected = None,
            event = WordSeparatorUiEvent.Unknown
        )
    }
}
