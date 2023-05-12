package proton.android.pass.featurepassword.impl.bottomsheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.sp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider

@Composable
fun GeneratePasswordToggleRow(
    modifier: Modifier = Modifier,
    text: String,
    value: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = PassTheme.colors.textNorm,
            style = PassTypography.body3Regular,
            fontSize = 16.sp
        )
        Switch(
            checked = value,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PassTheme.colors.loginInteractionNormMajor1,
            ),
            onCheckedChange = onChange
        )
    }
}

@Preview
@Composable
fun GeneratePasswordToggleRowPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            GeneratePasswordToggleRow(
                text = "Some preference",
                value = input.second,
                onChange = {}
            )
        }
    }
}
