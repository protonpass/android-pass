package proton.android.pass.featurehome.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider

@Composable
fun ItemListHeader(
    modifier: Modifier = Modifier,
    sortingType: SortingType,
    showSearchResults: Boolean,
    itemCount: Int?,
    onSortingOptionsClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ItemCount(
            modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 0.dp),
            showSearchResults = showSearchResults,
            itemCount = itemCount
        )
        SortingButton(
            sortingType = sortingType,
            onSortingOptionsClick = onSortingOptionsClick
        )
    }
}

@Preview
@Composable
fun ItemListHeaderPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            ItemListHeader(
                sortingType = SortingType.TitleAsc,
                showSearchResults = input.second,
                itemCount = 56,
                onSortingOptionsClick = {}
            )
        }
    }
}
