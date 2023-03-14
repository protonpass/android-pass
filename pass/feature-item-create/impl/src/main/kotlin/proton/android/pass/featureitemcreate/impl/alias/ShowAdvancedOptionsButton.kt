package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.featureitemcreate.impl.R

@Composable
fun ShowAdvancedOptionsButton(
    modifier: Modifier = Modifier,
    currentValue: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_cog_wheel),
            contentDescription = null,
            tint = PassTheme.colors.textWeak
        )

        val text = if (currentValue) {
            stringResource(R.string.field_alias_hide_advanced_options)
        } else {
            stringResource(R.string.field_alias_show_advanced_options)
        }
        Text(
            text = text,
            color = PassTheme.colors.textWeak,
            style = PassTypography.body3Regular
        )
    }

}

@Preview
@Composable
fun ShowAdvancedOptionsButtonPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            ShowAdvancedOptionsButton(currentValue = input.second, onClick = {})
        }
    }
}
