package proton.android.pass.featureitemcreate.impl.totp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import proton.android.pass.composecomponents.impl.uievents.IsLoadingState
import proton.android.pass.featureitemcreate.impl.R
import proton.android.pass.featureitemcreate.impl.totp.TotpSpecValidationErrors.BlankSecret
import proton.android.pass.featureitemcreate.impl.totp.TotpSpecValidationErrors.BlankValidTime
import proton.android.pass.totp.api.TotpAlgorithm
import proton.android.pass.totp.api.TotpDigits

@Composable
fun CreateManualTotpContent(
    modifier: Modifier = Modifier,
    totpSpec: TotpSpecUi,
    validationErrors: Set<TotpSpecValidationErrors>,
    isLoadingState: IsLoadingState,
    onAddManualTotp: (TotpSpecUi) -> Unit,
    onCloseManualTotp: () -> Unit,
    onSecretChange: (String) -> Unit,
    onLabelChange: (String) -> Unit,
    onIssuerChange: (String) -> Unit,
    onAlgorithmChange: (TotpAlgorithm) -> Unit,
    onDigitsChange: (TotpDigits) -> Unit,
    onValidPeriodChange: (Int?) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TotpTopBar(
                topBarTitle = R.string.totp_top_bar_manual_title,
                topBarActionName = R.string.totp_top_bar_manual_save_action,
                isLoadingState = isLoadingState,
                onUpClick = { onCloseManualTotp() },
                onSubmit = { onAddManualTotp(totpSpec) }
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
                .padding(16.dp)
        ) {
            TotpSecretInput(
                value = totpSpec.secret,
                onChange = onSecretChange,
                fieldRequiredError = validationErrors.contains(BlankSecret)
            )
            TotpLabelInput(
                value = totpSpec.label,
                onChange = onLabelChange,
                fieldRequiredError = validationErrors.contains(TotpSpecValidationErrors.BlankLabel)
            )
            TotpIssuerInput(value = totpSpec.issuer, onChange = onIssuerChange)
            Spacer(modifier = Modifier.height(5.dp))
            TotpAlgorithmSelector(value = totpSpec.algorithm, onChange = onAlgorithmChange)
            Spacer(modifier = Modifier.height(5.dp))
            TotpDigitsSelector(value = totpSpec.digits, onChange = onDigitsChange)
            TotpValidPeriodInput(
                value = totpSpec.validPeriodSeconds,
                onChange = onValidPeriodChange,
                fieldRequiredError = validationErrors.contains(BlankValidTime)
            )
        }
    }
}
