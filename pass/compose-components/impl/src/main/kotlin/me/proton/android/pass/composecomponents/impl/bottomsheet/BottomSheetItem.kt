package me.proton.android.pass.composecomponents.impl.bottomsheet

import androidx.compose.runtime.Composable

interface BottomSheetItem {
    val title: @Composable () -> Unit
    val subtitle: @Composable (() -> Unit)?
    val icon: @Composable (() -> Unit)?
    val onClick: () -> Unit
}
