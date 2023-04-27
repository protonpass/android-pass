package proton.android.pass.composecomponents.impl.item.header

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.R

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ItemCount(
    modifier: Modifier = Modifier,
    showSearchResults: Boolean,
    itemCount: Int?
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = if (showSearchResults) itemCount?.let { "$it" }
                ?: "0" else stringResource(R.string.item_list_header_all_search_results),
            style = PassTypography.body3Bold
        )
        Text(
            text = if (showSearchResults) {
                pluralStringResource(R.plurals.item_list_header_results, itemCount ?: 0)
            } else {
                itemCount?.let { "($it)" } ?: ""
            },
            style = ProtonTheme.typography.captionWeak
        )
    }
}

class ItemCountPreviewProvider : PreviewParameterProvider<ItemCountParameter> {
    override val values: Sequence<ItemCountParameter>
        get() = sequence {
            for (showSearchResults in listOf(false, true)) {
                for (itemCount in listOf(null, 0, 1)) {
                    yield(
                        ItemCountParameter(
                            showSearchResults = showSearchResults,
                            itemCount = itemCount
                        )
                    )
                }
            }
        }
}

data class ItemCountParameter(
    val showSearchResults: Boolean,
    val itemCount: Int?
)

class ThemedItemCountPreviewProvider :
    ThemePairPreviewProvider<ItemCountParameter>(ItemCountPreviewProvider())

@Preview
@Composable
fun ItemCountPreview(
    @PreviewParameter(ThemedItemCountPreviewProvider::class) input: Pair<Boolean, ItemCountParameter>
) {
    PassTheme(isDark = input.first) {
        Surface {
            ItemCount(
                showSearchResults = input.second.showSearchResults,
                itemCount = input.second.itemCount
            )
        }
    }
}
