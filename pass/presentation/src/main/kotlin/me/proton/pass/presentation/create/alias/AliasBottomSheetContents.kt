package me.proton.pass.presentation.create.alias

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.collections.immutable.toImmutableList
import me.proton.pass.domain.AliasSuffix
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetTitle

@ExperimentalMaterialApi
@Composable
fun AliasBottomSheetContents(
    modifier: Modifier = Modifier,
    modelState: AliasItem,
    onSuffixSelect: (AliasSuffix) -> Unit
) {
    Column(modifier = modifier) {
        BottomSheetTitle(title = R.string.alias_bottomsheet_suffix_title, showDivider = true)
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
