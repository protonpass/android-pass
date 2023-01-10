package proton.android.pass.presentation.detail.login

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import me.proton.pass.presentation.R
import proton.android.pass.presentation.detail.DetailSectionTitle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WebsiteSection(
    modifier: Modifier = Modifier,
    websites: ImmutableList<String>,
    onWebsiteClicked: (String) -> Unit,
    onWebsiteLongClicked: (String) -> Unit
) {
    Column(modifier = modifier.fillMaxWidth()) {
        DetailSectionTitle(text = stringResource(R.string.field_websites))
        Spacer(modifier = Modifier.height(8.dp))

        websites.forEach { website ->
            Text(
                modifier = Modifier
                    .combinedClickable(
                        onClick = { onWebsiteClicked(website) },
                        onLongClick = { onWebsiteLongClicked(website) }
                    )
                    .padding(vertical = 8.dp),
                text = website,
                color = ProtonTheme.colors.interactionNorm,
                fontSize = 16.sp,
                fontWeight = FontWeight.W400
            )
        }
    }
}

class ThemedWebsiteSectionPreviewProvider :
    ThemePairPreviewProvider<List<String>>(WebsiteProvider())

@Preview
@Composable
fun WebsitesSectionPreview(
    @PreviewParameter(ThemedWebsiteSectionPreviewProvider::class) input: Pair<Boolean, List<String>>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            WebsiteSection(
                websites = input.second.toImmutableList(),
                onWebsiteClicked = {},
                onWebsiteLongClicked = {}
            )
        }
    }
}
