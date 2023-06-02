package proton.android.pass.featurehome.impl.onboardingtips

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultHighlightNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.form.SmallCrossIconButton

@Suppress("MagicNumber")
@Composable
fun SpotlightCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    title: String,
    body: String,
    buttonText: String,
    image: (@Composable () -> Unit)? = null,
    onClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = 10.dp
    ) {
        Box(
            modifier = Modifier.background(backgroundColor),
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
                        text = title,
                        style = ProtonTheme.typography.defaultHighlightNorm,
                        color = PassTheme.colors.textInvert
                    )
                    Text(
                        text = body,
                        style = PassTypography.body3Regular,
                        color = PassTheme.colors.textInvert
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = buttonText,
                        style = ProtonTheme.typography.defaultHighlightNorm,
                        color = PassTheme.colors.textInvert
                    )
                }
                image?.invoke()
            }

            SmallCrossIconButton(
                modifier = Modifier.align(Alignment.TopEnd),
                tint = PassTheme.colors.textInvert,
                onClick = onDismiss
            )
        }
    }
}

@Preview
@Composable
fun SpotlightCardPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            SpotlightCard(
                backgroundColor = PassTheme.colors.loginInteractionNorm,
                title = "A sample card",
                body = "A sample body",
                buttonText = "Click me",
                onClick = {},
                onDismiss = {}
            )
        }
    }
}
