package proton.android.pass.featurehome.impl.onboardingtips

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.featurehome.impl.R

@Composable
fun AutofillCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    SpotlightCard(
        modifier = modifier,
        backgroundColor = PassTheme.colors.aliasInteractionNormMajor1,
        title = stringResource(id = R.string.home_autofill_banner_title),
        body = stringResource(id = R.string.home_autofill_banner_text),
        buttonText = stringResource(id = R.string.home_autofill_banner_settings),
        image = {
            Image(
                modifier = Modifier.size(60.dp),
                alignment = Alignment.CenterEnd,
                painter = painterResource(id = R.drawable.spotlight_illustration),
                contentDescription = stringResource(id = R.string.home_autofill_banner_image_content_description)
            )
        },
        onClick = onClick,
        onDismiss = onDismiss
    )
}

@Preview
@Composable
fun AutofillCardContentPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            AutofillCard(onClick = {}, onDismiss = {})
        }
    }
}
