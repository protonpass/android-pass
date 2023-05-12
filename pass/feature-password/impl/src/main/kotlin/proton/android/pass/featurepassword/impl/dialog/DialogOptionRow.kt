package proton.android.pass.featurepassword.impl.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider

@Composable
fun DialogOptionRow(
    modifier: Modifier = Modifier,
    value: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            colors = RadioButtonDefaults.colors(
                selectedColor = PassTheme.colors.loginInteractionNormMajor1
            ),
            onClick = { onClick() }
        )
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = value,
            color = PassTheme.colors.textNorm
        )
    }
}

@Preview
@Composable
fun DialogOptionRowPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            DialogOptionRow(
                value = "Option",
                isSelected = input.second,
                onClick = {}
            )
        }
    }
}
