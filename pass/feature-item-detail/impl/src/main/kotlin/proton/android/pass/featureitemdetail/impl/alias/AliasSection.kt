package proton.android.pass.featureitemdetail.impl.alias

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.pass.domain.AliasMailbox

@Composable
fun AliasSection(
    modifier: Modifier = Modifier,
    alias: String,
    mailboxes: PersistentList<AliasMailbox>,
    isLoading: Boolean,
    onCopyAlias: (String) -> Unit
) {
    RoundedCornersColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        AliasAddressRow(
            alias = alias,
            onCopyAlias = { onCopyAlias(it) }
        )
        if (!mailboxes.isEmpty() || isLoading) {
            Divider()
        }
        AliasMailboxesRow(
            mailboxes = mailboxes,
            isLoading = isLoading
        )
    }
}

class ThemedAliasMailboxesPreviewProvider :
    ThemePairPreviewProvider<List<AliasMailbox>>(AliasMailboxesPreviewProvider())

@Preview
@Composable
fun AliasSectionPreview(
    @PreviewParameter(ThemedAliasMailboxesPreviewProvider::class) input: Pair<Boolean, List<AliasMailbox>>
) {
    PassTheme(isDark = input.first) {
        Surface {
            AliasSection(
                alias = "myalias@myalias.com",
                mailboxes = input.second.toPersistentList(),
                isLoading = false
            ) {}
        }
    }
}
