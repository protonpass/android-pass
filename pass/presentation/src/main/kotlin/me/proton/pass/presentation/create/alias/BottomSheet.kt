package me.proton.pass.presentation.create.alias

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.domain.AliasSuffix
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetTitle
import me.proton.pass.presentation.components.common.bottomsheet.BottomSheetTitleButton

sealed interface AliasBottomSheetContent {
    object Suffix : AliasBottomSheetContent
    object Mailbox : AliasBottomSheetContent
}

@ExperimentalMaterialApi
@Composable
fun BottomSheetContents(
    modelState: AliasItem,
    contentType: AliasBottomSheetContent,
    onSuffixSelect: (AliasSuffix) -> Unit,
    onMailboxSelect: (AliasMailboxUiModel) -> Unit,
    onCloseBottomSheet: () -> Unit
) {
    Column {
        when (contentType) {
            is AliasBottomSheetContent.Mailbox -> {
                BottomSheetTitle(
                    title = R.string.alias_bottomsheet_mailboxes_title,
                    button = BottomSheetTitleButton(
                        title = R.string.action_apply,
                        onClick = onCloseBottomSheet,
                        enabled = modelState.isMailboxListApplicable

                    )
                )
                BottomSheetItemList(
                    items = modelState.mailboxes,
                    displayer = { it.model.email },
                    isChecked = { it.selected },
                    onSelect = onMailboxSelect
                )
            }
            is AliasBottomSheetContent.Suffix -> {
                BottomSheetTitle(title = R.string.alias_bottomsheet_suffix_title)
                BottomSheetItemList(
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


@Composable
private fun <T> BottomSheetItemList(
    items: List<T>,
    displayer: (T) -> String,
    isChecked: (T) -> Boolean,
    onSelect: (T) -> Unit
) {
    items.forEach { item ->
        BottomSheetItem(
            text = displayer(item),
            isChecked = isChecked(item)
        ) {
            onSelect(item)
        }
    }
}

@Composable
private fun BottomSheetItem(
    text: String,
    isChecked: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onClick() })
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier
                .weight(1.0f)
                .padding(vertical = 12.dp),
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.W400,
            color = ProtonTheme.colors.textNorm
        )
        if (isChecked) {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_checkmark),
                contentDescription = null,
                tint = ProtonTheme.colors.brandNorm
            )
        }
    }
}

@Preview
@Composable
fun BottomSheetTitlePreview() {
    ProtonTheme {
        Surface {
            BottomSheetTitle(
                title = R.string.alias_bottomsheet_mailboxes_title,
                button = BottomSheetTitleButton(
                    title = R.string.action_apply,
                    onClick = {},
                    enabled = false
                )
            )
        }
    }
}
