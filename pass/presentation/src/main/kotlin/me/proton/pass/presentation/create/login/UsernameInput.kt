package me.proton.pass.presentation.create.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import me.proton.pass.commonui.api.ThemePreviewProvider
import me.proton.pass.presentation.R
import me.proton.pass.presentation.components.form.ProtonTextField
import me.proton.pass.presentation.components.form.ProtonTextTitle

@Composable
internal fun UsernameInput(
    modifier: Modifier = Modifier,
    value: String,
    onChange: (String) -> Unit,
    onGenerateAliasClick: () -> Unit
) {
    Column(modifier = modifier) {
        ProtonTextTitle(R.string.field_username_title)

        Row(
            modifier = Modifier
                .fillMaxWidth(1.0f)
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProtonTextField(
                value = value,
                onChange = onChange,
                placeholder = R.string.field_username_hint,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .weight(1.0f)
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            OutlinedButton(
                onClick = onGenerateAliasClick,
                shape = ProtonTheme.shapes.medium,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_alias),
                    contentDescription = null,
                    tint = ProtonTheme.colors.iconNorm,
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun UsernameInputPreview(
    @PreviewParameter(ThemePreviewProvider::class) isDark: Boolean
) {
    ProtonTheme(isDark = isDark) {
        Surface {
            UsernameInput(
                value = "asda",
                onChange = {},
                onGenerateAliasClick = {}
            )
        }
    }
}
