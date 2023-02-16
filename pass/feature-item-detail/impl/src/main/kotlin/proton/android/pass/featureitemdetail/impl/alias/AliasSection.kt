package proton.android.pass.featureitemdetail.impl.alias

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Divider
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.pass.domain.AliasMailbox

@Composable
fun AliasSection(
    modifier: Modifier = Modifier,
    model: AliasUiModel?,
    isLoading: Boolean,
    onCopyAlias: (String) -> Unit
) {
    RoundedCornersColumn(
        modifier = modifier.fillMaxWidth()
    ) {
        AliasAddressRow(
            alias = model?.alias ?: "",
            onCopyAlias = { onCopyAlias(it) }
        )
        Divider()
        AliasMailboxesRow(
            mailboxes = model?.mailboxes ?: emptyList(),
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
    ProtonTheme(isDark = input.first) {
        Surface {
            AliasSection(
                model = AliasUiModel(
                    title = "",
                    alias = "myalias@myalias.com",
                    mailboxes = input.second,
                    note = ""
                ),
                isLoading = false
            ) {}
        }
    }
}
