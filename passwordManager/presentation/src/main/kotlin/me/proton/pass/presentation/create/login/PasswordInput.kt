package me.proton.pass.presentation.create.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.form.ProtonTextField
import me.proton.pass.presentation.components.form.ProtonTextTitle

@Composable
internal fun PasswordInput(
    modifier: Modifier = Modifier,
    value: String,
    onChange: (String) -> Unit,
    onGeneratePasswordClick: () -> Unit
) {
    var isVisible: Boolean by rememberSaveable { mutableStateOf(false) }

    val (visualTransformation, icon) = if (isVisible) {
        Pair(VisualTransformation.None, painterResource(me.proton.core.presentation.R.drawable.ic_proton_eye_slash))
    } else {
        Pair(PasswordVisualTransformation(), painterResource(me.proton.core.presentation.R.drawable.ic_proton_eye))
    }

    Column(modifier = modifier.padding(top = 28.dp)) {
        ProtonTextTitle(R.string.field_password_title)

        Row(
            modifier = Modifier.fillMaxWidth(1.0f).padding(top = 8.dp)
        ) {
            ProtonTextField(
                value = value,
                onChange = onChange,
                placeholder = R.string.field_password_hint,
                trailingIcon = {
                    Icon(
                        painter = icon,
                        contentDescription = null,
                        tint = ProtonTheme.colors.iconNorm,
                        modifier = Modifier.clickable { isVisible = !isVisible }
                    )
                },
                visualTransformation = visualTransformation,
                modifier = Modifier.weight(1.0f)
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            OutlinedButton(
                onClick = onGeneratePasswordClick,
                shape = ProtonTheme.shapes.medium,
                modifier = Modifier.size(48.dp).align(Alignment.CenterVertically)
            ) {
                Icon(
                    painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_arrows_rotate),
                    contentDescription = null,
                    tint = ProtonTheme.colors.iconNorm,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
fun PasswordInputPreview() {
    ProtonTheme {
        PasswordInput(
            value = "someValue",
            onChange = {},
            onGeneratePasswordClick = {}
        )
    }
}
