package me.proton.pass.presentation.create.alias

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.toImmutableList
import me.proton.android.pass.composecomponents.impl.bottomsheet.BottomSheetTitle
import me.proton.pass.presentation.R

@ExperimentalMaterialApi
@Composable
fun AliasBottomSheetContents(
    modifier: Modifier = Modifier,
    modelState: AliasItem,
    onSuffixSelect: (AliasSuffixUiModel) -> Unit
) {
    Column(modifier = modifier) {
        BottomSheetTitle(
            title = stringResource(id = R.string.alias_bottomsheet_suffix_title),
            showDivider = true
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
