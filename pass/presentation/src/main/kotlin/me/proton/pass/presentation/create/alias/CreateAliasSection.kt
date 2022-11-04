package me.proton.pass.presentation.create.alias

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.presentation.R

@Composable
internal fun CreateAliasSection(
    state: AliasItem,
    canEdit: Boolean,
    onAliasRequiredError: Boolean,
    onChange: (String) -> Unit,
    onSuffixClick: () -> Unit
) {
    AliasInput(
        value = state.alias,
        onChange = onChange,
        editable = canEdit,
        onAliasRequiredError = onAliasRequiredError
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
