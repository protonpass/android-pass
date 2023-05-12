package proton.android.pass.featurepassword.impl.dialog.separator

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.ProtonDialogTitle
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.composecomponents.impl.dialogs.DialogCancelConfirmSection
import proton.android.pass.featurepassword.R
import proton.android.pass.password.api.PasswordGenerator

@Composable
fun WordSeparatorDialogContent(
    modifier: Modifier = Modifier,
    state: WordSeparatorUiState,
    onOptionSelected: (PasswordGenerator.WordSeparator) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Column(modifier = modifier) {
        ProtonDialogTitle(
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
            title = stringResource(R.string.word_separator)
        )

        WordSeparatorList(
            options = state.options,
            selected = state.selected,
            onSelected = onOptionSelected
        )

        DialogCancelConfirmSection(
            modifier = Modifier.padding(16.dp),
            color = PassTheme.colors.loginInteractionNormMajor1,
            onDismiss = onCancel,
            onConfirm = onConfirm
        )
    }
}
