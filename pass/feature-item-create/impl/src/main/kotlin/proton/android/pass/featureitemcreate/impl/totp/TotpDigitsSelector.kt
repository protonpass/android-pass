package proton.android.pass.featureitemcreate.impl.totp

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import proton.android.pass.composecomponents.impl.form.ProtonFormDropdown
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.totp.api.TotpDigits

@Composable
fun TotpDigitsSelector(
    modifier: Modifier = Modifier,
    value: TotpDigits,
    onChange: (TotpDigits) -> Unit,
    enabled: Boolean = true
) {
    val options: List<String> = remember {
        TotpDigits.values()
            .map { it.digits.toString() }
            .toList()
    }
    ProtonFormDropdown(
        modifier = modifier,
        enabled = enabled,
        selectedValue = value.digits.toString(),
        valueList = options,
        label = stringResource(id = R.string.totp_digits_field_title),
        onChange = {
            val totpDigit = when (it) {
                "6" -> TotpDigits.Six
                "8" -> TotpDigits.Eight
                else -> TotpDigits.Six
            }
            onChange(totpDigit)
        }
    )
}
