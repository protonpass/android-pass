package proton.android.pass.featurecreateitem.impl.login

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextTitle
import proton.android.pass.featurecreateitem.impl.R

@Composable
internal fun TotpInput(
    modifier: Modifier = Modifier,
    value: String,
    onAddTotpClick: () -> Unit
) {
    Column(modifier = modifier) {
        ProtonTextTitle(stringResource(R.string.totp_create_login_field_title))
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
                onChange = { },
                editable = false,
                placeholder = stringResource(R.string.totp_create_login_field_placeholder)
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            OutlinedButton(
                onClick = { onAddTotpClick() },
                shape = ProtonTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1.0f)
                    .align(Alignment.CenterVertically)
            ) {
                Icon(
                    painter = painterResource(me.proton.core.presentation.R.drawable.ic_proton_plus),
                    contentDescription = null,
                    tint = ProtonTheme.colors.iconNorm,
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        }
    }
}
