package proton.android.pass.featureitemcreate.impl.totp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.composecomponents.impl.form.ProtonFormDropdown
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.totp.api.TotpAlgorithm

@Composable
fun TotpAlgorithmSelector(
    modifier: Modifier = Modifier,
    value: TotpAlgorithm,
    onChange: (TotpAlgorithm) -> Unit,
    enabled: Boolean = true
) {
    val options: List<String> = remember {
        TotpAlgorithm.values().map { it.name.uppercase() }.toList()
    }
    ProtonFormDropdown(
        modifier = modifier,
        enabled = enabled,
        selectedValue = value.name.uppercase(),
        valueList = options,
        label = stringResource(id = R.string.totp_algorithm_field_title),
        onChange = {
            onChange(TotpAlgorithm.valueOf(it))
        }
    )
}
