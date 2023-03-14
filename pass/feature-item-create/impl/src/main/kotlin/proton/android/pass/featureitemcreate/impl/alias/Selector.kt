package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonui.api.applyIf

@Composable
internal fun Selector(
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    TextField(
        readOnly = true,
        enabled = false,
        value = text,
        onValueChange = {},
        trailingIcon = {
            if (enabled) {
                Icon(
                    painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_chevron_down),
                    contentDescription = null,
                    tint = ProtonTheme.colors.iconNorm
                )
            }
        },
        shape = shape,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .applyIf(enabled, ifTrue = { clickable { onClick() } }),
        colors = TextFieldDefaults.textFieldColors(
            textColor = ProtonTheme.colors.textNorm,
            disabledTextColor = ProtonTheme.colors.textNorm,
            backgroundColor = ProtonTheme.colors.backgroundSecondary,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        )
    )
}

class ThemedSelectorPreviewProvider :
    ThemePairPreviewProvider<SelectorPreviewParameter>(SelectorPreviewProvider())

@Preview
@Composable
fun SelectorPreview(
    @PreviewParameter(ThemedSelectorPreviewProvider::class) input: Pair<Boolean, SelectorPreviewParameter>
) {
    PassTheme(isDark = input.first) {
        Surface {
            Selector(
                text = input.second.text,
                enabled = input.second.enabled,
                onClick = {}
            )
        }
    }
}
