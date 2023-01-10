package proton.android.pass.presentation.detail.alias

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.composecomponents.impl.container.RoundedCornersContainer
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.pass.domain.AliasMailbox
import me.proton.pass.presentation.R
import proton.android.pass.presentation.detail.DetailSectionSubtitle
import proton.android.pass.presentation.detail.DetailSectionTitle

@Composable
fun AliasMailboxesSection(
    modifier: Modifier = Modifier,
    mailboxes: List<AliasMailbox>
) {
    RoundedCornersContainer(
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            DetailSectionTitle(text = stringResource(R.string.alias_detail_field_mailboxes_title))
            Spacer(modifier = Modifier.height(8.dp))
            mailboxes.forEachIndexed { idx, mailbox ->
                if (idx > 0) {
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }
                DetailSectionSubtitle(text = mailbox.email)
            }
        }
    }
}

class ThemedAliasMailboxesPreviewProvider :
    ThemePairPreviewProvider<List<AliasMailbox>>(AliasMailboxesPreviewProvider())

@Preview
@Composable
fun AliasMailboxesSectionPreview(
    @PreviewParameter(ThemedAliasMailboxesPreviewProvider::class) input: Pair<Boolean, List<AliasMailbox>>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            AliasMailboxesSection(
                mailboxes = input.second
            )
        }
    }
}
