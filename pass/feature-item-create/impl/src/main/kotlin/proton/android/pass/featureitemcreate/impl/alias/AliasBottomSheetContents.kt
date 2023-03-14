package proton.android.pass.featureitemcreate.impl.alias

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.toImmutableList
import proton.android.pass.commonui.api.PassTheme
import proton.android.pass.commonui.api.ThemePreviewProvider
import proton.android.pass.commonui.api.bottomSheetPadding
import proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetTitle
import proton.android.pass.featureitemcreate.impl.R

@ExperimentalMaterialApi
@Composable
fun AliasBottomSheetContents(
    modifier: Modifier = Modifier,
    modelState: AliasItem,
    onSuffixSelect: (AliasSuffixUiModel) -> Unit
) {
    Column(
        modifier = modifier.bottomSheetPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BottomSheetTitle(
            title = stringResource(id = R.string.alias_bottomsheet_suffix_title)
        )
        AliasBottomSheetItemList(
            items = modelState.aliasOptions.suffixes.toImmutableList(),
            displayer = { it.suffix },
            isChecked = {
                if (modelState.selectedSuffix != null) {
                    modelState.selectedSuffix.suffix == it.suffix
                } else {
                    false
                }
            },
            onSelect = onSuffixSelect
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Preview
@Composable
fun AliasBottomSheetContentsPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    val suffix = AliasSuffixUiModel(
        suffix = ".random@alias.suffix",
        signedSuffix = "",
        isCustom = false,
        domain = "alias.suffix"
    )
    PassTheme(isDark = isDark) {
        Surface {
            AliasBottomSheetContents(
                modelState = AliasItem(
                    selectedSuffix = suffix,
                    aliasOptions = AliasOptionsUiModel(
                        suffixes = listOf(
                            suffix,
                            AliasSuffixUiModel(
                                suffix = ".other@alias.suffix.that.is.very.long.and.does.not.fit",
                                signedSuffix = "",
                                isCustom = false,
                                domain = "alias.suffix"
                            ),
                        ),
                        mailboxes = emptyList()
                    )
                ),
                onSuffixSelect = {}
            )
        }
    }
}
