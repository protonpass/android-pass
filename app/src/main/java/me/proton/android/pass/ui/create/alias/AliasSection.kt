package me.proton.android.pass.ui.create.alias

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.android.pass.R
import me.proton.android.pass.ui.shared.ProtonTextField
import me.proton.android.pass.ui.shared.ProtonTextTitle
import me.proton.core.compose.theme.ProtonTheme

@Composable
internal fun AliasSection(
    state: BaseAliasViewModel.ModelState,
    canEdit: Boolean,
    onChange: (String) -> Unit,
    onSuffixClick: () -> Unit
) {
    ProtonTextTitle(R.string.field_alias_title)
    ProtonTextField(
        value = state.alias,
        onChange = onChange,
        modifier = Modifier.padding(top = 8.dp),
        editable = canEdit
    )
    AliasSelector(
        state = state,
        modifier = Modifier.padding(top = 8.dp),
        onClick = onSuffixClick
    )
    if (state.aliasToBeCreated != null) {
        Row(modifier = Modifier.padding(top = 4.dp)) {
            Text(
                text = stringResource(R.string.alias_you_are_about_to_create_alias),
                fontSize = 10.sp,
                color = ProtonTheme.colors.textWeak
            )
            Text(
                text = state.aliasToBeCreated,
                fontSize = 10.sp,
                color = ProtonTheme.colors.brandNorm,
                modifier = Modifier.padding(start = 2.dp)
            )
        }
    }
}
