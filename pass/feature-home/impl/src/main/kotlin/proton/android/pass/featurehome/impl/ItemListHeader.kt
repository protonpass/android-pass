package proton.android.pass.featurehome.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ItemListHeader(
    sortingType: SortingType,
    showSearchResults: Boolean,
    isProcessingSearch: Boolean,
    itemCount: Int?,
    onSortingOptionsClick: () -> Unit
) {
    if (!isProcessingSearch) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ItemCount(
                Modifier.padding(16.dp, 0.dp, 0.dp, 0.dp),
                showSearchResults,
                itemCount
            )
            SortingButton(
                sortingType = sortingType,
                onSortingOptionsClick = onSortingOptionsClick
            )
        }
    }
}
