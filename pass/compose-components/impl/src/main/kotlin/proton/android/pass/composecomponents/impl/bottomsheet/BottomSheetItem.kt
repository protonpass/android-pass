package proton.android.pass.composecomponents.impl.bottomsheet

import androidx.compose.runtime.Composable

interface BottomSheetItem {
    val title: @Composable () -> Unit
    val subtitle: @Composable (() -> Unit)?
    val leftIcon: @Composable (() -> Unit)?
    val endIcon: @Composable (() -> Unit)?
    val onClick: (() -> Unit)?
    val isDivider: Boolean
}

fun bottomSheetDivider(): BottomSheetItem = object : BottomSheetItem {
    override val title: @Composable () -> Unit
        get() = {}
    override val subtitle: (@Composable () -> Unit)?
        get() = null
    override val leftIcon: (@Composable () -> Unit)?
        get() = null
    override val endIcon: (@Composable () -> Unit)?
        get() = null
    override val onClick: (@Composable () -> Unit)?
        get() = null
    override val isDivider = true
}

fun List<BottomSheetItem>.withDividers(): List<BottomSheetItem> =
    this.flatMap { listOf(it, bottomSheetDivider()) }
        .dropLast(1)
