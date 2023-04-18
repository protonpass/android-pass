package proton.android.pass.featurehome.impl.onboardingtips

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultHighlight
import proton.android.pass.commonui.api.PassPalette
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton
import proton.android.pass.featurehome.impl.R

@Suppress("MagicNumber")
@Composable
fun AutofillCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {

    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = 10.dp
    ) {
        Box(
            modifier = Modifier
                .background(PassTheme.colors.aliasInteractionNormMajor1),
        ) {
            Row(
                modifier = Modifier.padding(16.dp, 32.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.home_autofill_banner_title),
                        style = ProtonTheme.typography.defaultHighlight,
                        color = PassTheme.colors.textInvert
                    )
                    Text(
                        text = stringResource(id = R.string.home_autofill_banner_text),
                        style = PassTypography.body3Regular,
                        color = PassTheme.colors.textInvert
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = stringResource(id = R.string.home_autofill_banner_settings),
                        style = ProtonTheme.typography.defaultHighlight,
                        color = PassTheme.colors.textInvert
                    )
                }
                Image(
                    modifier = Modifier.size(60.dp),
                    alignment = Alignment.CenterEnd,
                    painter = painterResource(id = R.drawable.spotlight_illustration),
                    contentDescription = stringResource(id = R.string.home_autofill_banner_image_content_description)
                )
            }

            SmallCrossIconButton(
                modifier = Modifier.align(Alignment.TopEnd),
                tint = PassPalette.White100,
                onClick = onDismiss
            )
        }
    }
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
