package me.proton.pass.presentation.home.bottomsheet

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.compose.theme.default
import me.proton.core.presentation.R
import me.proton.pass.commonui.api.ThemePairPreviewProvider

@Composable
fun SortingBottomSheetItem(
    modifier: Modifier = Modifier,
    text: String,
    isChecked: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = { onClick() })
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isChecked) {
            Icon(
                painter = painterResource(R.drawable.ic_proton_checkmark),
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(16.dp))
        } else {
            Spacer(modifier = Modifier.width(40.dp))
        }
        Text(
            modifier = Modifier
                .weight(1.0f),
            text = text,
            style = ProtonTheme.typography.default
        )
    }
}

class ThemedSortingBottomSheetItemProvider :
    ThemePairPreviewProvider<SortingBottomSheetItemParameter>(
        SortingBottomSheetItemProvider()
    )

@Preview
@Composable
fun SortingBottomSheetItemPreview(
    @PreviewParameter(ThemedSortingBottomSheetItemProvider::class) input: Pair<Boolean, SortingBottomSheetItemParameter>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            SortingBottomSheetItem(text = input.second.text, isChecked = input.second.isChecked) {}
        }
    }
}
