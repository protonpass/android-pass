package me.proton.core.pass.presentation.create.login

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.proton.core.compose.component.ProtonOutlinedButton
import me.proton.core.compose.theme.ProtonTheme
import me.proton.core.pass.presentation.PasswordGenerator
import me.proton.core.pass.presentation.R

@Composable
internal fun GeneratePasswordButton(
    onPasswordGenerated: (String) -> Unit
) {
    ProtonOutlinedButton(onClick = { onPasswordGenerated(PasswordGenerator.generatePassword()) }) {
        Text(
            text = stringResource(R.string.button_generate_password),
            color = ProtonTheme.colors.brandNorm,
            fontSize = 16.sp,
            fontWeight = FontWeight.W400,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        )
    }
}
