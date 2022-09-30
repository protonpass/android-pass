package me.proton.android.pass.ui.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.DropdownMenu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.PopupProperties
import me.proton.core.compose.theme.ProtonTheme

@Composable
fun ItemDropdownMenu(
    modifier: Modifier,
    expanded: Boolean,
    setExpanded: (Boolean) -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { setExpanded(false) },
        properties = PopupProperties(),
        modifier = modifier
            .background(color = ProtonTheme.colors.backgroundNorm)
    ) {
        content()
    }
}
