package proton.android.pass.featurecreateitem.impl.alias

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import proton.android.pass.commonui.api.ThemePairPreviewProvider
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextFieldLabel
import proton.android.pass.featurecreateitem.impl.R

@Composable
internal fun DisplayAliasSection(
    modifier: Modifier = Modifier,
    state: AliasItem
) {
    ProtonTextField(
        label = { ProtonTextFieldLabel(text = stringResource(id = R.string.field_alias_title)) },
        value = state.aliasToBeCreated ?: "",
        onChange = {},
        editable = false,
        modifier = modifier.padding(top = 8.dp),
        textStyle = ProtonTheme.typography.default(false)
    )
}

class ThemedDisplayAliasPreviewProvider :
    ThemePairPreviewProvider<AliasItemParameter>(AliasItemPreviewProvider())

@Preview
@Composable
fun DisplayAliasSectionPreview(
    @PreviewParameter(ThemedDisplayAliasPreviewProvider::class) input: Pair<Boolean, AliasItemParameter>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            DisplayAliasSection(state = input.second.item)
        }
    }
}
