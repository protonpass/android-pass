package proton.android.pass.composecomponents.impl.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.R
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider

@Composable
fun ActionableItemRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    highlight: String = "",
    showMenuIcon: Boolean,
    onItemClick: (ItemUiModel) -> Unit = {},
    onItemMenuClick: (ItemUiModel) -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick(item) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ItemRowContents(
            item = item,
            highlight = highlight,
            modifier = Modifier.weight(1f)
        )
        if (showMenuIcon) {
            Spacer(modifier = Modifier.width(16.dp))
            IconButton(
                onClick = { onItemMenuClick(item) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    ImageVector.vectorResource(R.drawable.ic_three_dots_vertical_24),
                    contentDescription = stringResource(id = R.string.action_content_description_menu)
                )
            }
        }
    }
}

class ThemeAndItemUiModelProvider :
    ThemePairPreviewProvider<ItemUiModel>(ItemUiModelPreviewProvider())

@Preview
@Composable
fun ActionableItemRowPreviewWithMenuIcon(
    @PreviewParameter(ThemeAndItemUiModelProvider::class) input: Pair<Boolean, ItemUiModel>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            ActionableItemRow(
                item = input.second,
                showMenuIcon = true
            )
        }
    }
}

@Preview
@Composable
fun ActionableItemRowPreviewWithoutMenuIcon(
    @PreviewParameter(ThemeAndItemUiModelProvider::class) input: Pair<Boolean, ItemUiModel>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            ActionableItemRow(
                item = input.second,
                showMenuIcon = false
            )
        }
    }
}
