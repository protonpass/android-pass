package proton.android.pass.featureitemdetail.impl.login.customfield

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.composecomponents.impl.container.RoundedCornersColumn
import proton.android.pass.featureitemdetail.impl.R
import proton.android.pass.featureitemdetail.impl.login.CustomFieldUiContent
import proton.android.pass.featureitemdetail.impl.login.LoginPasswordRow
import me.proton.core.presentation.R as CoreR

@Composable
fun CustomFieldHidden(
    modifier: Modifier = Modifier,
    entry: CustomFieldUiContent.Hidden,
    onToggleVisibility: () -> Unit,
    onCopyValue: () -> Unit
) {
    RoundedCornersColumn(modifier) {
        LoginPasswordRow(
            passwordHiddenState = entry.content,
            label = entry.label,
            iconRes = CoreR.drawable.ic_proton_eye_slash,
            iconContentDescription = stringResource(R.string.custom_field_hidden_icon_description),
            onTogglePasswordClick = onToggleVisibility,
            onCopyPasswordClick = onCopyValue
        )
    }
}
