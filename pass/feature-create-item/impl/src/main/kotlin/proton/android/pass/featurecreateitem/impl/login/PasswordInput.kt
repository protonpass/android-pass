package proton.android.pass.featurecreateitem.impl.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import proton.android.pass.composecomponents.impl.form.ProtonTextField
import proton.android.pass.composecomponents.impl.form.ProtonTextTitle
import proton.android.pass.featurecreateitem.impl.R
import me.proton.core.compose.theme.ProtonTheme
import proton.android.pass.commonui.api.ThemedBooleanPreviewProvider

@Composable
internal fun PasswordInput(
    modifier: Modifier = Modifier,
    value: String,
    isEditAllowed: Boolean,
    onChange: (String) -> Unit,
    onGeneratePasswordClick: () -> Unit
) {
    var isVisible: Boolean by rememberSaveable { mutableStateOf(false) }

    val (visualTransformation, icon) = if (isVisible) {
        Pair(
            VisualTransformation.None,
            painterResource(me.proton.core.presentation.R.drawable.ic_proton_eye_slash)
        )
    } else {
        Pair(
            PasswordVisualTransformation(),
            painterResource(me.proton.core.presentation.R.drawable.ic_proton_eye)
        )
    }

    Column(modifier = modifier.padding(top = 28.dp)) {
        ProtonTextTitle(stringResource(id = R.string.field_password_title))
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth(1.0f)
        ) {
            ProtonTextField(
                modifier = Modifier
                    .weight(1.0f)
                    .fillMaxHeight(),
                value = value,
                editable = isEditAllowed,
                onChange = onChange,
                placeholder = stringResource(id = R.string.field_password_hint),
                trailingIcon = {
                    IconButton(
                        onClick = { isVisible = !isVisible }
                    ) {
                        Icon(
                            painter = icon,
                            contentDescription = null,
                            tint = ProtonTheme.colors.iconNorm
                        )
                    }
                },
                visualTransformation = visualTransformation
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            OutlinedButton(
                onClick = onGeneratePasswordClick,
                shape = ProtonTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1.0f)
                    .align(Alignment.CenterVertically)
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

@Preview
@Composable
fun PasswordInputPreview(
    @PreviewParameter(ThemedBooleanPreviewProvider::class) input: Pair<Boolean, Boolean>
) {
    ProtonTheme(isDark = input.first) {
        Surface {
            PasswordInput(
                value = "someValue",
                isEditAllowed = input.second,
                onChange = {},
                onGeneratePasswordClick = {}
            )
        }
    }
}
