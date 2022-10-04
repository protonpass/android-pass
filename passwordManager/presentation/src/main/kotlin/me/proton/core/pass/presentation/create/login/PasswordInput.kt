package me.proton.core.pass.presentation.create.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import me.proton.android.pass.ui.shared.ProtonFormInput
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.presentation.R

@Composable
internal fun PasswordInput(
    value: String,
    onChange: (String) -> Unit
) {
    var isVisible: Boolean by rememberSaveable { mutableStateOf(false) }

    val (visualTransformation, icon) = if (isVisible) {
        Pair(VisualTransformation.None, painterResource(me.proton.core.presentation.R.drawable.ic_proton_eye_slash))
    } else {
        Pair(PasswordVisualTransformation(), painterResource(me.proton.core.presentation.R.drawable.ic_proton_eye))
    }

    ProtonFormInput(
        title = R.string.field_password_title,
        placeholder = R.string.field_password_hint,
        value = value,
        onChange = onChange,
        visualTransformation = visualTransformation,
        modifier = Modifier.padding(top = 28.dp),
        trailingIcon = {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = ProtonTheme.colors.iconNorm,
                modifier = Modifier.clickable { isVisible = !isVisible }
            )
        }
    )
}
