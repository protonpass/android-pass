package proton.android.pass.featureitemcreate.impl.login.customfields

import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.buttons.CircleIconButton
import proton.android.pass.featureitemcreate.impl.R
import me.proton.core.presentation.R as CoreR

@Composable
fun CustomFieldOptionsButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    CircleIconButton(
        modifier = modifier,
        backgroundColor = PassTheme.colors.loginInteractionNormMinor1,
        onClick = onClick
    ) {
        Icon(
            painter = painterResource(CoreR.drawable.ic_proton_three_dots_vertical),
            contentDescription = stringResource(R.string.custom_field_options_content_description),
            tint = PassTheme.colors.loginInteractionNormMajor2
        )
    }
}

@Preview
@Composable
fun CustomFieldOptionsButtonPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            CustomFieldOptionsButton(onClick = {})
        }
    }
}
