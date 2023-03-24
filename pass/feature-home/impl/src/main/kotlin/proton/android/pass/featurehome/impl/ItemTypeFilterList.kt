package proton.android.pass.featurehome.impl

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmall
import me.proton.core.compose.theme.overlineWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.buttons.CircleButton
import proton.android.pass.featurehome.impl.HomeItemTypeSelection.Aliases
import proton.android.pass.featurehome.impl.HomeItemTypeSelection.AllItems
import proton.android.pass.featurehome.impl.HomeItemTypeSelection.Logins
import proton.android.pass.featurehome.impl.HomeItemTypeSelection.Notes
import me.proton.core.presentation.R as CoreR

@Composable
fun ItemTypeFilterList(
    modifier: Modifier = Modifier,
    selected: HomeItemTypeSelection,
    loginCount: Int,
    aliasCount: Int,
    noteCount: Int,
    onItemTypeClick: (HomeItemTypeSelection) -> Unit
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        ItemTypeButton(
            AllItems,
            selected == AllItems,
            loginCount + aliasCount + noteCount,
            onItemTypeClick
        )
        ItemTypeButton(Logins, selected == Logins, loginCount, onItemTypeClick)
        ItemTypeButton(Aliases, selected == Aliases, aliasCount, onItemTypeClick)
        ItemTypeButton(Notes, selected == Notes, noteCount, onItemTypeClick)
    }
}

@Composable
private fun ItemTypeButton(
    homeItemTypeSelection: HomeItemTypeSelection,
    isSelected: Boolean,
    count: Int,
    onItemTypeClick: (HomeItemTypeSelection) -> Unit
) {
    CircleButton(
        contentPadding = PaddingValues(12.dp, 0.dp),
        color = if (isSelected) {
            PassTheme.colors.interactionNorm
        } else {
            PassTheme.colors.interactionNormMinor1
        },
        onClick = { onItemTypeClick(homeItemTypeSelection) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                modifier = Modifier.height(16.dp),
                painter = when (homeItemTypeSelection) {
                    AllItems -> painterResource(CoreR.drawable.ic_proton_list_bullets)
                    Logins -> painterResource(CoreR.drawable.ic_proton_user)
                    Aliases -> painterResource(CoreR.drawable.ic_proton_alias)
                    Notes -> painterResource(CoreR.drawable.ic_proton_file_lines)
                },
                contentDescription = stringResource(R.string.item_type_filter_list_icon_content_description),
                tint = if (isSelected) {
                    PassTheme.colors.textNorm
                } else {
                    PassTheme.colors.textWeak
                }
            )
            Text(
                text = when (homeItemTypeSelection) {
                    AllItems -> stringResource(R.string.item_type_filter_all)
                    Logins -> stringResource(R.string.item_type_filter_login)
                    Aliases -> stringResource(R.string.item_type_filter_alias)
                    Notes -> stringResource(R.string.item_type_filter_note)
                },
                style = ProtonTheme.typography.defaultSmall,
            )
            Text(
                text = "$count",
                style = ProtonTheme.typography.overlineWeak
            )
        }
    }
}

@Preview
@Composable
fun ItemTypeFilterListPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            ItemTypeFilterList(
                selected = Logins,
                loginCount = 2,
                aliasCount = 4,
                noteCount = 1,
                onItemTypeClick = {}
            )
        }
    }
}
