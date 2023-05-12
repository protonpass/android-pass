package proton.android.pass.featureitemcreate.impl.alias.suffixes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.component.ProtonDialogTitle
import me.proton.core.compose.component.ProtonTextButton
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.defaultNorm
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.alias.AliasSuffixUiModel
import me.proton.core.presentation.compose.R as CoreR
import proton.android.pass.composecomponents.impl.R as ComponentsR

@Composable
fun SelectSuffixContent(
    modifier: Modifier = Modifier,
    suffixes: List<AliasSuffixUiModel>,
    canUpgrade: Boolean,
    selectedSuffix: AliasSuffixUiModel?,
    color: Color,
    onSuffixChanged: (AliasSuffixUiModel) -> Unit,
    onUpgrade: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(modifier = modifier) {
        ProtonDialogTitle(
            modifier = Modifier.padding(16.dp),
            title = stringResource(R.string.alias_bottomsheet_suffix_title)
        )
        var suffixState by remember { mutableStateOf(selectedSuffix ?: suffixes.firstOrNull()) }
        LazyColumn {
            items(items = suffixes, key = { it.suffix }) { item ->
                SelectSuffixItemRow(
                    suffix = item.suffix,
                    isSelected = suffixState?.suffix == item.suffix,
                    color = color,
                    onSelect = {
                        suffixState = item
                    }
                )
            }
        }
        if (canUpgrade) {
            Divider(color = PassTheme.colors.inputBorderNorm)
            Row(
                modifier = Modifier
                    .clickable(onClick = onUpgrade)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = stringResource(R.string.select_suffix_upgrade_for_custom_domains),
                    style = ProtonTheme.typography.defaultNorm,
                    color = PassTheme.colors.interactionNormMajor2
                )
                Icon(
                    modifier = Modifier.size(16.dp),
                    painter = painterResource(CoreR.drawable.ic_proton_arrow_out_square),
                    contentDescription = stringResource(ComponentsR.string.upgrade_icon_content_description),
                    tint = PassTheme.colors.interactionNormMajor2
                )
            }
            Divider(color = PassTheme.colors.inputBorderNorm)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            ProtonTextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.alias_mailbox_dialog_cancel_button),
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            ProtonTextButton(
                onClick = { suffixState?.let { onSuffixChanged(it) } }
            ) {
                Text(
                    text = stringResource(R.string.alias_mailbox_dialog_confirm_button),
                    color = color,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

}

@Preview
@Composable
fun SelectSuffixContentPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    val selected = AliasSuffixUiModel(
        suffix = ".some@suffix.test",
        signedSuffix = "",
        isCustom = false,
        domain = ""
    )
    PassTheme(isDark = input.first) {
        Surface {
            SelectSuffixContent(
                suffixes = listOf(
                    selected,
                    AliasSuffixUiModel(
                        suffix = ".other@random.suffix",
                        signedSuffix = "",
                        isCustom = false,
                        domain = ""
                    )
                ),
                canUpgrade = input.second,
                selectedSuffix = selected,
                color = PassTheme.colors.loginInteractionNorm,
                onSuffixChanged = {},
                onDismiss = {},
                onUpgrade = {}
            )
        }
    }
}
