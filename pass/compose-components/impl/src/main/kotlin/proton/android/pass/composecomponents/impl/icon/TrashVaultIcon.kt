package proton.android.pass.composecomponents.impl.icon

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.PassTheme

@Composable
fun TrashVaultIcon(
    modifier: Modifier = Modifier,
) {
    VaultIcon(
        modifier = modifier,
        backgroundColor = PassTheme.colors.textDisabled,
        iconColor = PassTheme.colors.textWeak,
        icon = me.proton.core.presentation.R.drawable.ic_proton_trash
    )
}
