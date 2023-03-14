package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.composecomponents.impl.container.roundedContainer
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.composecomponents.impl.icon.ForwardIcon
import proton.android.pass.composecomponents.impl.item.placeholder
import proton.android.pass.featureitemcreate.impl.R

@Composable
fun MailboxSection(
    modifier: Modifier = Modifier,
    mailboxes: List<SelectedAliasMailboxUiModel>,
    isEditAllowed: Boolean,
    isLoading: Boolean,
    onMailboxClick: () -> Unit
) {
    val selectedMailboxes = mailboxes.filter { it.selected }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .roundedContainer(ProtonTheme.colors.separatorNorm)
            .clickable(enabled = isEditAllowed, onClick = onMailboxClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ForwardIcon()
        Column(
            modifier = Modifier.weight(1f),
        ) {
            ProtonTextFieldLabel(text = stringResource(R.string.field_mailboxes_title))
            if (isLoading) {
                Text(modifier = Modifier.fillMaxWidth().placeholder(), text = "")
            } else {
                selectedMailboxes.forEach { mailbox ->
                    Text(text = mailbox.model.email)
                }
            }

        }
        if (isEditAllowed) {
            Icon(
                painter = painterResource(id = me.proton.core.presentation.R.drawable.ic_proton_chevron_down),
                contentDescription = null,
            )
        }
    }
}

@Preview
@Composable
fun MailboxSectionPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            MailboxSection(
                isLoading = false,
                mailboxes = listOf(
                    SelectedAliasMailboxUiModel(
                        model = AliasMailboxUiModel(
                            id = 1,
                            email = "prefix@suffix.test"
                        ),
                        selected = true
                    )
                ),
                isEditAllowed = input.second,
                onMailboxClick = {}
            )
        }
    }
}
