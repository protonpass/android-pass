package me.proton.core.pass.presentation.create.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.presentation.R
import me.proton.core.pass.presentation.components.form.ProtonFormInput

@Composable
internal fun UsernameInput(
    value: String,
    onChange: (String) -> Unit,
    onGenerateAliasClick: () -> Unit
) {
    ProtonFormInput(
        title = R.string.field_username_title,
        placeholder = R.string.field_username_hint,
        value = value,
        onChange = onChange,
        modifier = Modifier.padding(top = 8.dp),
        trailingIcon = {
            Icon(
                painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_alias),
                contentDescription = null,
                tint = ProtonTheme.colors.iconNorm,
                modifier = Modifier.clickable { onGenerateAliasClick() }
            )
        }
    )
}
