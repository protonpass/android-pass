package me.proton.pass.presentation.create.alias

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.form.ProtonFormInput

@Composable
internal fun DisplayAliasSection(
    state: AliasItem
) {
    ProtonFormInput(
        title = R.string.field_alias_title,
        value = state.aliasToBeCreated ?: "",
        onChange = {},
        editable = false,
        modifier = Modifier.padding(top = 8.dp)
    )
}
