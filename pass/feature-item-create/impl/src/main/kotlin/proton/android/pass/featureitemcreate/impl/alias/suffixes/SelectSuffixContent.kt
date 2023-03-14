package proton.android.pass.featureitemcreate.impl.alias.suffixes

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.featureitemcreate.impl.alias.AliasSuffixUiModel

@Composable
fun SelectSuffixContent(
    modifier: Modifier = Modifier,
    suffixes: List<AliasSuffixUiModel>,
    selectedSuffix: AliasSuffixUiModel?,
    color: Color,
    onSuffixChanged: (AliasSuffixUiModel) -> Unit,
) {
    val selected = selectedSuffix?.suffix
    LazyColumn(modifier = modifier) {
        items(items = suffixes, key = { it.suffix }) { item ->
            SelectSuffixItemRow(
                suffix = item.suffix,
                isSelected = selected == item.suffix,
                color = color,
                onSelect = { onSuffixChanged(item) }
            )
        }
    }
}

@Preview
@Composable
fun SelectSuffixContentPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    val selected = AliasSuffixUiModel(
        suffix = ".some@suffix.test",
        signedSuffix = "",
        isCustom = false,
        domain = ""
    )
    PassTheme(isDark = isDark) {
        Surface {
            SelectSuffixContent(
                suffixes = listOf(
                    selected,
                    AliasSuffixUiModel(
                        suffix = ".other@random.suffix",
                        signedSuffix = "",
                        isCustom = false,
                        domain = ""
                    )
                ),
                selectedSuffix = selected,
                color = PassTheme.colors.accentPurpleNorm,
                onSuffixChanged = {}
            )
        }
    }
}
