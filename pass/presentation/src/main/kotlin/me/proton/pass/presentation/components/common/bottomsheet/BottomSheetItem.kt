package me.proton.pass.presentation.components.common.bottomsheet

import androidx.compose.runtime.Composable

interface BottomSheetItem {
    val title: @Composable () -> Unit
    val subtitle: @Composable (() -> Unit)?
    val icon: @Composable (() -> Unit)?
    val onClick: () -> Unit
}
