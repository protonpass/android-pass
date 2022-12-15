package me.proton.pass.presentation.create.alias

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePairPreviewProvider
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.previewproviders.CreateAliasSectionPreviewParameter
import me.proton.pass.presentation.components.previewproviders.CreateAliasSectionPreviewProvider

@Composable
internal fun CreateAliasSection(
    modifier: Modifier = Modifier,
    state: AliasItem,
    canEdit: Boolean,
    onAliasRequiredError: Boolean,
    onInvalidAliasError: Boolean,
    onChange: (String) -> Unit,
    onSuffixClick: () -> Unit
) {
    Column(modifier) {
        AliasInput(
            value = state.alias,
            onChange = onChange,
            editable = canEdit,
            onAliasRequiredError = onAliasRequiredError,
            onInvalidAliasError = onInvalidAliasError
        )
        AliasSelector(
            state = state,
            modifier = Modifier.padding(top = 8.dp),
            onClick = onSuffixClick
        )
        if (state.aliasToBeCreated != null) {
            Row(modifier = Modifier.padding(top = 4.dp)) {
                val text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = ProtonTheme.colors.textWeak)) {
                        append(stringResource(R.string.alias_you_are_about_to_create_alias))
                    }
                    append(' ')
                    withStyle(style = SpanStyle(color = ProtonTheme.colors.brandNorm)) {
                        append(state.aliasToBeCreated)
                    }
                }
                Text(
                    text = text,
                    fontSize = 10.sp
                )
            }
        }
    }
}

class ThemedCreateAliasSectionPreviewProvider :
    ThemePairPreviewProvider<CreateAliasSectionPreviewParameter>(CreateAliasSectionPreviewProvider())

@Preview
@Composable
fun CreateAliasSectionPreview(
    @PreviewParameter(ThemedCreateAliasSectionPreviewProvider::class)
    input: Pair<Boolean, CreateAliasSectionPreviewParameter>
) {
    val param = input.second
    ProtonTheme(isDark = input.first) {
        Surface {
            CreateAliasSection(
                state = param.aliasItem,
                canEdit = param.canEdit,
                onAliasRequiredError = param.onAliasRequiredError,
                onInvalidAliasError = param.onInvalidAliasError,
                onChange = {},
                onSuffixClick = {}
            )
        }
    }
}
