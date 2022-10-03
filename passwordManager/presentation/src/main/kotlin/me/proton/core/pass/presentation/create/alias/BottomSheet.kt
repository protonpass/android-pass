package me.proton.core.pass.presentation.create.alias

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.domain.AliasMailbox
import me.proton.core.pass.domain.AliasSuffix
import me.proton.core.pass.presentation.R

enum class AliasBottomSheetContent {
    Suffix,
    Mailbox
}

@ExperimentalMaterialApi
@Composable
fun BottomSheetContents(
    modelState: AliasItem,
    contentType: AliasBottomSheetContent,
    onSuffixSelect: (AliasSuffix) -> Unit,
    onMailboxSelect: (AliasMailbox) -> Unit
) {

    Column {
        when (contentType) {
            AliasBottomSheetContent.Mailbox -> {
                BottomSheetTitle(title = R.string.alias_bottomsheet_mailbox_title)
                BottomSheetItemList(
                    items = modelState.aliasOptions.mailboxes,
                    displayer = { it.email },
                    onSelect = onMailboxSelect
                )
            }
            AliasBottomSheetContent.Suffix -> {
                BottomSheetTitle(title = R.string.alias_bottomsheet_suffix_title)
                BottomSheetItemList(
                    items = modelState.aliasOptions.suffixes,
                    displayer = { it.suffix },
                    onSelect = onSuffixSelect
                )
            }
        }
    }
}

@Composable
private fun BottomSheetTitle(
    @StringRes title: Int
) {
    Text(
        text = stringResource(title),
        fontSize = 16.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
    Divider(modifier = Modifier.fillMaxWidth())
}

@Composable
private fun <T> BottomSheetItemList(
    items: List<T>,
    displayer: (T) -> String,
    onSelect: (T) -> Unit
) {
    items.forEach { item ->
        BottomSheetItem(text = displayer(item)) {
            onSelect(item)
        }
    }
}

@Composable
private fun BottomSheetItem(
    text: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { onClick() })
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 20.dp),
            fontWeight = FontWeight.W400,
            color = ProtonTheme.colors.textNorm
        )
    }
}
