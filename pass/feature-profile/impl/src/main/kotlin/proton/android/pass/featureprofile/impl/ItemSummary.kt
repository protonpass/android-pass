package proton.android.pass.featureprofile.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultSmallWeak
import me.proton.core.compose.theme.headline
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.composecomponents.impl.item.icon.AliasIcon
import proton.android.pass.composecomponents.impl.item.icon.LoginIcon
import proton.android.pass.composecomponents.impl.item.icon.MFAIcon
import proton.android.pass.composecomponents.impl.item.icon.NoteIcon

@Composable
fun ItemSummary(
    modifier: Modifier = Modifier,
    itemSummaryUiState: ItemSummaryUiState
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ItemTypeBox(type = SummaryItemType.Logins, count = itemSummaryUiState.loginCount)
            ItemTypeBox(type = SummaryItemType.Notes, count = itemSummaryUiState.notesCount)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ItemTypeBox(
                type = SummaryItemType.Alias,
                count = itemSummaryUiState.aliasCount,
                remaining = itemSummaryUiState.aliasLeft
            )
            ItemTypeBox(
                type = SummaryItemType.MFA,
                count = itemSummaryUiState.mfaCount,
                remaining = itemSummaryUiState.mfaLeft
            )
        }
    }
}


@Composable
fun RowScope.ItemTypeBox(
    modifier: Modifier = Modifier,
    type: SummaryItemType,
    count: Int,
    remaining: Int? = null
) {
    Row(
        modifier = modifier
            .weight(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(PassTheme.colors.interactionNormMinor1)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when (type) {
                SummaryItemType.Logins -> {
                    LoginIcon()
                    Text(
                        text = stringResource(R.string.itemtype_summary_login),
                        style = PassTypography.body3Regular
                    )
                }
                SummaryItemType.Notes -> {
                    NoteIcon()
                    Text(
                        text = stringResource(R.string.itemtype_summary_notes),
                        style = PassTypography.body3Regular
                    )
                }
                SummaryItemType.Alias -> {
                    AliasIcon()
                    Text(
                        text = stringResource(R.string.itemtype_summary_alias),
                        style = PassTypography.body3Regular
                    )
                }
                SummaryItemType.MFA -> {
                    MFAIcon()
                    Text(
                        text = stringResource(R.string.itemtype_summary_2fa),
                        style = PassTypography.body3Regular
                    )
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = count.toString(),
                textAlign = TextAlign.End,
                style = ProtonTheme.typography.headline
            )
            remaining?.let {
                Text(
                    text = stringResource(id = R.string.itemtype_summary_left, it),
                    textAlign = TextAlign.End,
                    style = ProtonTheme.typography.defaultSmallWeak
                )
            }
        }
    }
}

enum class SummaryItemType {
    Logins, Notes, Alias, MFA
}

@Preview
@Composable
fun ItemSummaryPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            ItemSummary(itemSummaryUiState = ItemSummaryUiState(aliasLeft = 1))
        }
    }
}
