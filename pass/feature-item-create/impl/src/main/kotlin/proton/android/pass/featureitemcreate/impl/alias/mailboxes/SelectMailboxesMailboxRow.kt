package proton.android.pass.featureitemcreate.impl.alias.mailboxes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Checkbox
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.LocalMinimumTouchTargetEnforcement
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonDimens
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.featureitemcreate.impl.alias.AliasMailboxUiModel
import proton.android.pass.featureitemcreate.impl.alias.SelectedAliasMailboxUiModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SelectMailboxesMailboxRow(
    modifier: Modifier = Modifier,
    item: SelectedAliasMailboxUiModel,
    onToggle: () -> Unit
) {
    Row(
        modifier = modifier
            .clickable { onToggle() }
            .padding(vertical = 12.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
            Checkbox(
                checked = item.selected,
                onCheckedChange = {}
            )
        }

        Spacer(modifier = Modifier.width(ProtonDimens.DefaultSpacing))
        Text(
            modifier = Modifier.weight(1.0f),
            text = item.model.email,
            style = ProtonTheme.typography.default,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview
@Composable
fun SelectMailboxesMailboxRowPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    PassTheme(isDark = input.first) {
        Surface {
            SelectMailboxesMailboxRow(
                item = SelectedAliasMailboxUiModel(
                    model = AliasMailboxUiModel(id = 1, email = "some.test@email.local"),
                    selected = input.second
                ),
                onToggle = {}
            )
        }
    }
}
