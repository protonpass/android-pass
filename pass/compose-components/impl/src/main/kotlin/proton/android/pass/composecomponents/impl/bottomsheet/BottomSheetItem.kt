package proton.android.pass.composecomponents.impl.bottomsheet

import androidx.compose.runtime.Composable

interface BottomSheetItem {
    val title: @Composable () -> Unit
    val subtitle: @Composable (() -> Unit)?
    val icon: @Composable (() -> Unit)?
    val onClick: (() -> Unit)?
    val isDivider: Boolean
}

fun BottomSheetDivider(): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = {}
    override val subtitle: (() -> Unit)?
        get() = null
    override val icon: (() -> Unit)?
        get() = null
    override val onClick: (() -> Unit)?
        get() = null
    override val isDivider = true
}
