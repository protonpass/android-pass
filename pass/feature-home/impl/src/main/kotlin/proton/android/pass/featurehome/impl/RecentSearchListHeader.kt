package proton.android.pass.featurehome.impl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.captionStrongNorm
import me.proton.core.compose.theme.captionWeak
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.PassTypography
import proton.android.pass.commonui.api.ThemePreviewProvider

@Composable
fun RecentSearchListHeader(
    modifier: Modifier = Modifier,
    itemCount: Int?,
    onClearRecentSearchClick: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.padding(16.dp, 0.dp, 0.dp, 0.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(R.string.recent_search_header_count),
                style = PassTypography.body3Bold
            )
            Text(
                text = itemCount?.let { "($it)" } ?: "",
                style = ProtonTheme.typography.captionWeak
            )
        }
        Button(
            elevation = null,
            colors = ButtonDefaults.buttonColors(Color.Transparent),
            onClick = onClearRecentSearchClick
        ) {
            Text(
                text = stringResource(R.string.recent_search_clear),
                style = ProtonTheme.typography.captionStrongNorm,
                color = PassTheme.colors.interactionNorm
            )
        }
    }
}

@Preview
@Composable
fun RecentSearchListHeaderPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    PassTheme(isDark = isDark) {
        Surface {
            RecentSearchListHeader(itemCount = 53, onClearRecentSearchClick = {})
        }
    }
}
