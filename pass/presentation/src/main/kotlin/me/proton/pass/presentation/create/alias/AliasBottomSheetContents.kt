package me.proton.pass.presentation.create.alias

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import me.proton.pass.domain.AliasSuffix
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetTitle
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetTitleButton

@ExperimentalMaterialApi
@Composable
fun AliasBottomSheetContents(
    modelState: AliasItem,
    contentType: AliasBottomSheetType,
    onSuffixSelect: (AliasSuffix) -> Unit,
    onMailboxSelect: (AliasMailboxUiModel) -> Unit,
    onCloseBottomSheet: () -> Unit
) {
    Column {
        when (contentType) {
            is AliasBottomSheetType.Mailbox -> {
                BottomSheetTitle(
                    title = R.string.alias_bottomsheet_mailboxes_title,
                    button = BottomSheetTitleButton(
                        title = R.string.action_apply,
                        onClick = onCloseBottomSheet,
                        enabled = modelState.isMailboxListApplicable
                    ),
                    showDivider = true
                )
                AliasBottomSheetItemList(
                    items = modelState.mailboxes,
                    displayer = { it.model.email },
                    isChecked = { it.selected },
                    onSelect = onMailboxSelect
                )
            }
            is AliasBottomSheetType.Suffix -> {
                BottomSheetTitle(title = R.string.alias_bottomsheet_suffix_title, showDivider = true)
                AliasBottomSheetItemList(
                    items = modelState.aliasOptions.suffixes,
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
    }
}
