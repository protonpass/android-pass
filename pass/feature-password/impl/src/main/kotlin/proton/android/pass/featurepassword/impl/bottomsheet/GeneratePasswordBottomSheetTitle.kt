package proton.android.pass.featurepassword.impl.bottomsheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.sp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.R
import proton.android.pass.composecomponents.impl.buttons.CircleIconButton

@Composable
fun GeneratePasswordBottomSheetTitle(
    modifier: Modifier = Modifier,
    onRegenerate: () -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = stringResource(id = R.string.bottomsheet_generate_password_title),
            style = PassTypography.body3Bold,
            fontSize = 16.sp
        )
        CircleIconButton(
            modifier = Modifier.align(Alignment.CenterEnd),
            backgroundColor = PassTheme.colors.loginInteractionNormMinor1,
            onClick = { onRegenerate() }
        ) {
            Icon(
                painter = painterResource(me.proton.core.presentation.compose.R.drawable.ic_proton_arrows_rotate),
                contentDescription = stringResource(R.string.regenerate_password_icon_content_description),
                tint = PassTheme.colors.loginInteractionNormMajor2
            )
        }
    }
}

@Preview
@Composable
fun GeneratePasswordBottomSheetTitlePreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            GeneratePasswordBottomSheetTitle(onRegenerate = {})
        }
    }
}
