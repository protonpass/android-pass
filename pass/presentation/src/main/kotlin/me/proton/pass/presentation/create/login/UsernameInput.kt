package me.proton.pass.presentation.create.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
    canUpdateUsername: Boolean,
    onChange: (String) -> Unit,
    onGenerateAliasClick: () -> Unit,
    onAliasOptionsClick: () -> Unit
) {
    val buttonIcon = if (canUpdateUsername) {
        me.proton.core.presentation.R.drawable.ic_proton_alias
    } else {
        me.proton.core.presentation.R.drawable.ic_proton_three_dots_vertical
    }

    Column(modifier = modifier) {
        ProtonTextTitle(R.string.field_username_title)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth(1.0f)
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProtonTextField(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1.0f),
                value = value,
                onChange = onChange,
                editable = canUpdateUsername,
                placeholder = R.string.field_username_hint
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            OutlinedButton(
                onClick = {
                    if (canUpdateUsername) {
                        onGenerateAliasClick()
                    } else {
                        onAliasOptionsClick()
                    }
                },
                shape = ProtonTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1.0f)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    painter = painterResource(buttonIcon),
                    contentDescription = null,
                    tint = ProtonTheme.colors.iconNorm,
                    modifier = Modifier.align(Alignment.CenterVertically)
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
                value = "some value",
                onChange = {},
                onGenerateAliasClick = {},
                onAliasOptionsClick = {},
                canUpdateUsername = true
            )
        }
    }
}
