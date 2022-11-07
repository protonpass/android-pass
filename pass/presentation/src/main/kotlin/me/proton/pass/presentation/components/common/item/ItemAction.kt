package me.proton.pass.presentation.components.common.item

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import me.proton.pass.presentation.components.model.ItemUiModel

data class ItemAction(
    val onSelect: (ItemUiModel) -> Unit,
    @StringRes val title: Int,
    @DrawableRes val icon: Int,
    val textColor: Color
)
