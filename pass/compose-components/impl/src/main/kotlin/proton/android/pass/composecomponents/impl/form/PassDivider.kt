package proton.android.pass.composecomponents.impl.form

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import proton.android.pass.commonui.api.PassTheme

@Composable
fun PassDivider(
    modifier: Modifier = Modifier
) {
    Divider(modifier = modifier.fillMaxWidth(), color = PassTheme.colors.inputBorderNorm)
}
