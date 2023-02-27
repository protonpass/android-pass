package proton.android.pass.featurecreateitem.impl.alias.suffixes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassColors
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.featurecreateitem.impl.alias.AliasSuffixUiModel


@Composable
fun SelectSuffixItemRow(
    modifier: Modifier = Modifier,
    item: AliasSuffixUiModel,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .padding(vertical = 8.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        RadioButton(
            selected = isSelected,
            onClick = null,
            colors = RadioButtonDefaults.colors(
                selectedColor = PassColors.Dark.accentGreenNorm
            )
        )
        Text(text = item.suffix)
    }
}

@Preview
@Composable
fun SelectSuffixItemRowPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            SelectSuffixItemRow(
                item = AliasSuffixUiModel(
                    suffix = ".some@suffix.test",
                    signedSuffix = "",
                    isCustom = false,
                    domain = ""
                ),
                isSelected = input.second,
                onSelect = {}
            )
        }
    }
}
