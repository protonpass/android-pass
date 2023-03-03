package proton.android.pass.composecomponents.impl.buttons

import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.R

@Composable
fun PassFloatingActionButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FloatingActionButton(
        modifier = modifier,
        backgroundColor = PassTheme.colors.accentPurpleWeakest.compositeOver(PassTheme.colors.backgroundNorm),
        contentColor = PassTheme.colors.accentPurpleNorm,
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_plus),
            contentDescription = stringResource(R.string.add_item_icon_content_description),
        )
    }
}

@Preview
@Composable
fun PassFloatingActionButtonPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            PassFloatingActionButton(
                onClick = {}
            )
        }
    }
}
