package proton.android.pass.composecomponents.impl.item

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonuimodels.api.ItemUiModel
import proton.android.pass.composecomponents.impl.R

@Composable
fun ActionableItemRow(
    modifier: Modifier = Modifier,
    item: ItemUiModel,
    vaultIcon: Int? = null,
    highlight: String = "",
    showMenuIcon: Boolean,
    onItemClick: (ItemUiModel) -> Unit = {},
    onItemMenuClick: (ItemUiModel) -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onItemClick(item) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ItemRowContents(
            modifier = Modifier.weight(1f),
            item = item,
            highlight = highlight,
            vaultIcon = vaultIcon
        )
        if (showMenuIcon) {
            IconButton(
                onClick = { onItemMenuClick(item) },
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.ic_three_dots_vertical_24),
                    contentDescription = stringResource(id = R.string.action_content_description_menu),
                    tint = PassTheme.colors.textWeak
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
    PassTheme(isDark = input.first) {
        Surface {
            ActionableItemRow(
                item = input.second,
                showMenuIcon = true,
                vaultIcon = null
            )
        }
    }
}

@Preview
@Composable
fun ActionableItemRowPreviewWithoutMenuIcon(
    @PreviewParameter(ThemeAndItemUiModelProvider::class) input: Pair<Boolean, ItemUiModel>
) {
    PassTheme(isDark = input.first) {
        Surface {
            ActionableItemRow(
                item = input.second,
                showMenuIcon = false,
                vaultIcon = null
            )
        }
    }
}

@Preview
@Composable
fun ActionableItemRowPreviewWithVaultIcon(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            ActionableItemRow(
                item = ItemUiModelPreviewProvider().values.first(),
                showMenuIcon = false,
                vaultIcon = R.drawable.ic_bookmark_small
            )
        }
    }
}
