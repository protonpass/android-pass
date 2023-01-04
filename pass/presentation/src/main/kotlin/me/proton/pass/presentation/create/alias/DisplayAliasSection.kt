package me.proton.pass.presentation.create.alias

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.android.pass.composecomponents.impl.form.ProtonFormInput
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePairPreviewProvider
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.previewproviders.AliasItemParameter
import me.proton.pass.presentation.components.previewproviders.AliasItemPreviewProvider

@Composable
internal fun DisplayAliasSection(
    modifier: Modifier = Modifier,
    state: AliasItem
) {
    ProtonFormInput(
        title = stringResource(id = R.string.field_alias_title),
        value = state.aliasToBeCreated ?: "",
        onChange = {},
        editable = false,
        modifier = modifier.padding(top = 8.dp)
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
