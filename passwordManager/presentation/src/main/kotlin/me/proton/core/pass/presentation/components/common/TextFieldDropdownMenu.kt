package me.proton.core.pass.presentation.components.common

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize

@Composable
fun TextFieldDropdownMenu(
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit,
    values: List<String>,
    onSelected: (Int) -> Unit,
) {
    val firstValue = values.firstOrNull()
    var selectedIndex by rememberSaveable { mutableStateOf(firstValue?.let { 0 }) }
    var selectedValue by remember {
        val value = selectedIndex?.let { values[it] }
        mutableStateOf(value)
    }
    if (selectedIndex == null && firstValue != null) {
        selectedValue = firstValue
        onSelected(0)
    }
    var isExpanded by rememberSaveable { mutableStateOf(false) }
    var textfieldSize by remember { mutableStateOf(Size.Zero) }

    val icon = if (isExpanded)
        Icons.Filled.ArrowDropUp
    else Icons.Filled.ArrowDropDown

    val textfieldClickDetector = remember { MutableInteractionSource() }
    if (textfieldClickDetector.collectIsPressedAsState().value) {
        isExpanded = !isExpanded
    }

    Column {
        OutlinedTextField(
            value = selectedValue.orEmpty(),
            onValueChange = { },
            label = label,
            readOnly = true,
            modifier = modifier
                .onGloballyPositioned { textfieldSize = it.size.toSize() },
            interactionSource = textfieldClickDetector,
            trailingIcon = {
                IconButton(
                    onClick = { isExpanded = !isExpanded },
                ) {
                    Icon(icon, "drop down")
                }
            }
        )
        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier.width(with(LocalDensity.current) { textfieldSize.width.toDp() })
        ) {
            values.forEachIndexed { index, value ->
                DropdownMenuItem(onClick = {
                    selectedIndex = index
                    selectedValue = value
                    onSelected(index)
                    isExpanded = false
                }) {
                    Text(value)
                }
            }
        }
    }
}
