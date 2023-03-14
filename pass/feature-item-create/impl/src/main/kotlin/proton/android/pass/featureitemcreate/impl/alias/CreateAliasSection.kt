package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePairPreviewProvider

@Composable
internal fun CreateAliasSection(
    modifier: Modifier = Modifier,
    state: AliasItem,
    canEdit: Boolean,
    canSelectSuffix: Boolean,
    onAliasRequiredError: Boolean,
    onInvalidAliasError: Boolean,
    onChange: (String) -> Unit,
    onSuffixClick: () -> Unit
) {
    Column(modifier) {
        AliasToBeCreated(
            prefix = state.prefix,
            suffix = state.selectedSuffix
        )
        AliasAdvancedOptions(
            enabled = canEdit,
            prefix = state.prefix,
            suffix = state.selectedSuffix,
            isError = onAliasRequiredError && onInvalidAliasError,
            canSelectSuffix = canSelectSuffix,
            onPrefixChanged = onChange,
            onSuffixClicked = onSuffixClick
        )
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
    PassTheme(isDark = input.first) {
        Surface {
            CreateAliasSection(
                state = param.aliasItem,
                canEdit = param.canEdit,
                canSelectSuffix = true,
                onAliasRequiredError = param.onAliasRequiredError,
                onInvalidAliasError = param.onInvalidAliasError,
                onChange = {},
                onSuffixClick = {}
            )
        }
    }
}
